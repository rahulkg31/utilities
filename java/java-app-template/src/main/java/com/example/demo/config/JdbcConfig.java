package com.example.demo.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.example.demo.enums.DbType;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableConfigurationProperties(JdbcProperties.class)
@ConditionalOnProperty(name = "db.type", havingValue = "jdbc")
public class JdbcConfig {

    private static final Logger LOGGER = LogManager.getLogger(JdbcConfig.class);

    private final JdbcProperties props;

    public JdbcConfig(JdbcProperties props) {
	this.props = props;
    }

    @Bean
    public DataSource dataSource() {

	String jdbcUrl = buildJdbcUrl();
	String driver = resolveDriver();

	LOGGER.info("Connecting to database type={} with URL: {}", props.getType(), jdbcUrl);

	HikariDataSource dataSource = DataSourceBuilder.create().type(HikariDataSource.class).url(jdbcUrl)
		.username(props.getUsername()).password(props.getPassword()).driverClassName(driver).build();

	JdbcProperties.Pool pool = props.getPool();

	dataSource.setPoolName(pool.getPoolName());
	dataSource.setMaximumPoolSize(pool.getMaximumPoolSize());
	dataSource.setMinimumIdle(pool.getMinimumIdle());
	dataSource.setConnectionTimeout(pool.getConnectionTimeoutMs());
	dataSource.setIdleTimeout(pool.getIdleTimeoutMs());
	dataSource.setMaxLifetime(pool.getMaxLifetimeMs());
	dataSource.setLeakDetectionThreshold(pool.getLeakDetectionThresholdMs());

	// Cheap, dialect-agnostic liveness check Hikari runs before handing out
	// a connection.
	dataSource.setConnectionTestQuery("SELECT 1");

	LOGGER.info("Configured Hikari pool '{}': max={}, minIdle={}, connectionTimeoutMs={}", pool.getPoolName(),
		pool.getMaximumPoolSize(), pool.getMinimumIdle(), pool.getConnectionTimeoutMs());

	return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {

	String dialect = resolveDialect();

	LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
	emf.setDataSource(dataSource);
	emf.setPersistenceUnitName("primaryPU");
	emf.setPersistenceProvider(new HibernatePersistenceProvider());

	// Adjust to match wherever your @Entity classes actually live.
	emf.setPackagesToScan("com.example.demo.entity");

	JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
	emf.setJpaVendorAdapter(vendorAdapter);

	Map<String, Object> jpaProperties = new HashMap<>();
	if (dialect != null) {
	    jpaProperties.put("hibernate.dialect", dialect);
	    LOGGER.info("Using Hibernate dialect: {}", dialect);
	} else {
	    LOGGER.info("No Hibernate dialect resolved; relying on Hibernate auto-detection.");
	}
	emf.setJpaPropertyMap(jpaProperties);

	return emf;
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean emf) {
	JpaTransactionManager txManager = new JpaTransactionManager();
	txManager.setEntityManagerFactory(emf.getObject());
	return txManager;
    }

    private String buildJdbcUrl() {
	if (props.getUrl() != null && !props.getUrl().isBlank()) {
	    return props.getUrl();
	}

	DbType type = DbType.fromValue(requireType());

	switch (type) {
	case POSTGRES:
	    return buildPostgresUrl();
	case H2:
	    return buildH2Url();
	default:
	    throw new IllegalStateException("Unknown db.jdbc.type '" + type + "' and no db.jdbc.url provided. "
		    + "Either set db.jdbc.url explicitly (works for any JDBC driver on the classpath) "
		    + "or use a supported db.jdbc.type: postgres, h2.");
	}
    }

    private String buildPostgresUrl() {
	StringBuilder url = new StringBuilder(
		"jdbc:postgresql://" + props.getHost() + ":" + props.getPort() + "/" + props.getDatabase());

	if (props.isSslEnabled()) {
	    url.append("?ssl=true&sslmode=").append(props.getSslMode());

	    if (props.isMtlsEnabled()) {
		url.append("&sslrootcert=").append(props.getRootCert()).append("&sslcert=")
			.append(props.getClientCert()).append("&sslkey=").append(props.getClientKey());
	    }
	    if (props.getExtraParams() != null && !props.getExtraParams().isBlank()) {
		url.append("&").append(props.getExtraParams());
	    }
	} else if (props.getExtraParams() != null && !props.getExtraParams().isBlank()) {
	    url.append("?").append(props.getExtraParams());
	}

	return url.toString();
    }

    private String buildH2Url() {
	// props.database holds the full H2 spec, e.g.:
	// "file:./data/mydb" -> jdbc:h2:file:./data/mydb
	// "mem:mydb" -> jdbc:h2:mem:mydb
	// "tcp://localhost:9092/mydb" -> jdbc:h2:tcp://localhost:9092/mydb
	return "jdbc:h2:" + props.getDatabase() + appendExtra(";");
    }

    private String resolveDriver() {
	if (props.getDriver() != null && !props.getDriver().isBlank()) {
	    return props.getDriver();
	}

	DbType type = DbType.fromValue(requireType());

	switch (type) {
	case POSTGRES:
	    return "org.postgresql.Driver";
	case H2:
	    return "org.h2.Driver";
	default:
	    throw new IllegalStateException(
		    "db.jdbc.driver must be set explicitly for db.jdbc.type '" + props.getType() + "'");
	}
    }

    private String resolveDialect() {
	if (props.getHibernateDialect() != null && !props.getHibernateDialect().isBlank()) {
	    return props.getHibernateDialect();
	}

	if (props.getType() == null || props.getType().isBlank()) {
	    return null; // fully custom URL/driver, no type given -> let Hibernate auto-detect
	}

	DbType type = DbType.fromValue(requireType());

	switch (type) {
	case POSTGRES:
	    return "org.hibernate.dialect.PostgreSQLDialect";
	case H2:
	    return "org.hibernate.dialect.H2Dialect";
	default:
	    return null; // unrecognized type -> let Hibernate auto-detect from the JDBC URL
	}
    }

    private String requireType() {
	if (props.getType() == null || props.getType().isBlank()) {
	    throw new IllegalStateException(
		    "db.jdbc.type must be set, or provide db.jdbc.url + db.jdbc.driver explicitly.");
	}
	return props.getType();
    }

    private String appendExtra(String prefix) {
	return (props.getExtraParams() != null && !props.getExtraParams().isBlank()) ? prefix + props.getExtraParams()
		: "";
    }
}
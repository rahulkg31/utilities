package com.example.dbmigration.config;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Component
@EnableConfigurationProperties(MongoProperties.class)
@ConditionalOnProperty(name = "db.type", havingValue = "mongo")
public class MongoConfig implements ApplicationRunner {

    private static final Logger LOGGER = LogManager.getLogger(MongoConfig.class);

    private final MongoProperties props;
    private final String changeLog;

    public MongoConfig(MongoProperties props,
            @Value("${spring.liquibase.change-log:classpath:db/changelog/db.changelog-master.yaml}") String changeLog) {
        this.props = props;
        this.changeLog = changeLog.replaceFirst("^classpath:", "");
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ResourceAccessor accessor = new ClassLoaderResourceAccessor();
        String uri = buildUrl(props);

        try (Database db = DatabaseFactory.getInstance()
                .openDatabase(uri, null, null, null, null, null, null, accessor);
             Liquibase liquibase = new Liquibase(changeLog, accessor, db)) {

            LOGGER.info("Running Liquibase against MongoDB database '{}'", props.getDatabase());
            liquibase.update(new Contexts(), new LabelExpression());
        }
    }
    
    public String buildUrl(MongoProperties props) {
        if (notBlank(props.getUrl())) return props.getUrl();

        StringBuilder uri = new StringBuilder("mongodb://");
        if (notBlank(props.getUsername())) {
            uri.append(enc(props.getUsername()));
            if (notBlank(props.getPassword())) uri.append(':').append(enc(props.getPassword()));
            uri.append('@');
        }
        uri.append(props.getHost()).append(':').append(props.getPort());
        uri.append('/').append(props.getDatabase() != null ? props.getDatabase() : "");

        String sep = "?";
        if (props.isSslEnabled()) { uri.append(sep).append("tls=true"); sep = "&"; }
        if (notBlank(props.getExtraParams())) uri.append(sep).append(props.getExtraParams());
        return uri.toString();
    }

    private String enc(String s) { return URLEncoder.encode(s, StandardCharsets.UTF_8); }
    private boolean notBlank(String s) { return s != null && !s.isBlank(); }
}
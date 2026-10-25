package com.example.dbmigration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("db.jdbc")
public class JdbcProperties {

	// postgres or h2. Only needed when url/driver/dialect aren't all set explicitly.
	private String type;

	private String host;
	private int port;
	
	// For h2 this is the full spec, e.g. "mem:mydb" or "file:./data/mydb".
	private String database;

	// If set, used verbatim instead of building the URL. Works for any JDBC database.
	private String url;

	private String username;
	private String password;

	private String driver;
	private String hibernateDialect;

	private boolean sslEnabled;
	private boolean mtlsEnabled;
	private String sslMode;
	private String rootCert;
	private String clientCert;
	private String clientKey;

	// Appended verbatim to the URL, e.g. "connectTimeout=10&applicationName=demo"
	private String extraParams;

	private Pool pool = new Pool();

	public String getType() { return type; }
	public void setType(String type) { this.type = type; }

	public String getHost() { return host; }
	public void setHost(String host) { this.host = host; }

	public int getPort() { return port; }
	public void setPort(int port) { this.port = port; }

	public String getDatabase() { return database; }
	public void setDatabase(String database) { this.database = database; }

	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url; }

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public String getDriver() { return driver; }
	public void setDriver(String driver) { this.driver = driver; }

	public String getHibernateDialect() { return hibernateDialect; }
	public void setHibernateDialect(String hibernateDialect) { this.hibernateDialect = hibernateDialect; }

	public boolean isSslEnabled() { return sslEnabled; }
	public void setSslEnabled(boolean sslEnabled) { this.sslEnabled = sslEnabled; }

	public boolean isMtlsEnabled() { return mtlsEnabled; }
	public void setMtlsEnabled(boolean mtlsEnabled) { this.mtlsEnabled = mtlsEnabled; }

	public String getSslMode() { return sslMode; }
	public void setSslMode(String sslMode) { this.sslMode = sslMode; }

	public String getRootCert() { return rootCert; }
	public void setRootCert(String rootCert) { this.rootCert = rootCert; }

	public String getClientCert() { return clientCert; }
	public void setClientCert(String clientCert) { this.clientCert = clientCert; }

	public String getClientKey() { return clientKey; }
	public void setClientKey(String clientKey) { this.clientKey = clientKey; }

	public String getExtraParams() { return extraParams; }
	public void setExtraParams(String extraParams) { this.extraParams = extraParams; }

	public Pool getPool() { return pool; }
	public void setPool(Pool pool) { this.pool = pool; }

	public static class Pool {
		private String poolName = "primary-pool";
		private int maximumPoolSize = 10;
		private int minimumIdle = 2;
		private long connectionTimeoutMs = 30000;    // 30s
		private long idleTimeoutMs = 600000;         // 10m
		private long maxLifetimeMs = 1800000;        // 30m
		private long leakDetectionThresholdMs = 0;   // 0 = disabled

		public String getPoolName() { return poolName; }
		public void setPoolName(String poolName) { this.poolName = poolName; }

		public int getMaximumPoolSize() { return maximumPoolSize; }
		public void setMaximumPoolSize(int maximumPoolSize) { this.maximumPoolSize = maximumPoolSize; }

		public int getMinimumIdle() { return minimumIdle; }
		public void setMinimumIdle(int minimumIdle) { this.minimumIdle = minimumIdle; }

		public long getConnectionTimeoutMs() { return connectionTimeoutMs; }
		public void setConnectionTimeoutMs(long connectionTimeoutMs) { this.connectionTimeoutMs = connectionTimeoutMs; }

		public long getIdleTimeoutMs() { return idleTimeoutMs; }
		public void setIdleTimeoutMs(long idleTimeoutMs) { this.idleTimeoutMs = idleTimeoutMs; }

		public long getMaxLifetimeMs() { return maxLifetimeMs; }
		public void setMaxLifetimeMs(long maxLifetimeMs) { this.maxLifetimeMs = maxLifetimeMs; }

		public long getLeakDetectionThresholdMs() { return leakDetectionThresholdMs; }
		public void setLeakDetectionThresholdMs(long leakDetectionThresholdMs) { this.leakDetectionThresholdMs = leakDetectionThresholdMs; }
	}
}
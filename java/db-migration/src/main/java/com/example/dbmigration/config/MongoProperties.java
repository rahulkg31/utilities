package com.example.dbmigration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("db.mongodb")
public class MongoProperties {

    /**
     * If set, used verbatim (mongodb:// or mongodb+srv://)
     * instead of building from host/port.
     */
    private String url;

    private String host;

    private int port = 27017;

    private String database;

    private String username;

    private String password;

    private boolean sslEnabled;

    private boolean mtlsEnabled;

    /**
     * Appended verbatim to the URI, e.g.
     * "authSource=admin&replicaSet=rs0"
     */
    private String extraParams;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isSslEnabled() { return sslEnabled; }
    public void setSslEnabled(boolean sslEnabled) { this.sslEnabled = sslEnabled; }

    public boolean isMtlsEnabled() { return mtlsEnabled; }
    public void setMtlsEnabled(boolean mtlsEnabled) { this.mtlsEnabled = mtlsEnabled; }

    public String getExtraParams() { return extraParams; }
    public void setExtraParams(String extraParams) { this.extraParams = extraParams; }

    @Override
    public String toString() {
        return "MongoProperties{" +
                "url='" + maskCredentials(url) + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", database='" + database + '\'' +
                ", username='" + username + '\'' +
                ", sslEnabled=" + sslEnabled +
                ", mtlsEnabled=" + mtlsEnabled +
                ", extraParams='" + extraParams + '\'' +
                '}';
    }

    /** mongodb://user:pass@host -> mongodb://***@host, so passwords never reach the logs. */
    private static String maskCredentials(String uri) {
        return uri == null ? null : uri.replaceFirst("//[^/@]+@", "//***@");
    }
}
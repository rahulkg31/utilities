package com.example.demo.config;

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

    /** Path to the client keystore (PKCS12 or JKS). Only used when mtlsEnabled. */
    private String keystore;

    private String keystorePassword;

    /** Path to the truststore (PKCS12 or JKS). If empty, the JVM default trust store is used. */
    private String truststore;

    private String truststorePassword;

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

    public String getKeystore() { return keystore; }
    public void setKeystore(String keystore) { this.keystore = keystore; }

    public String getKeystorePassword() { return keystorePassword; }
    public void setKeystorePassword(String keystorePassword) { this.keystorePassword = keystorePassword; }

    public String getTruststore() { return truststore; }
    public void setTruststore(String truststore) { this.truststore = truststore; }

    public String getTruststorePassword() { return truststorePassword; }
    public void setTruststorePassword(String truststorePassword) { this.truststorePassword = truststorePassword; }

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
                ", keystore='" + keystore + '\'' +
                ", truststore='" + truststore + '\'' +
                ", extraParams='" + extraParams + '\'' +
                '}';
    }

    /** mongodb://user:pass@host -> mongodb://***@host, so passwords never reach the logs. */
    private static String maskCredentials(String uri) {
        return uri == null ? null : uri.replaceFirst("//[^/@]+@", "//***@");
    }
}
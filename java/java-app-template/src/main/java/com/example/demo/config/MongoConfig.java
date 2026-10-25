package com.example.demo.config;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.UuidRepresentation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

/**
 * MongoDB configuration. Active only when app.db-type=mongo.
 */
@Configuration
@EnableConfigurationProperties(MongoProperties.class)
@ConditionalOnProperty(name = "db.type", havingValue = "mongodb")
public class MongoConfig {

    private static final Logger LOGGER = LogManager.getLogger(MongoConfig.class);

    private final MongoProperties props;

    public MongoConfig(MongoProperties props) {
        this.props = props;
    }

    @Bean(name = "mongoClient", destroyMethod = "close")
    public MongoClient mongoClient() {
        LOGGER.info("Initializing MongoDB connection with properties: {}", props);

        MongoClientSettings.Builder settings = MongoClientSettings.builder()
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .applyConnectionString(new ConnectionString(buildUri()))
                .readPreference(ReadPreference.secondaryPreferred());

        if (props.isSslEnabled()) {
            // Applies only to this client (no JVM-wide javax.net.ssl.* system properties).
            SSLContext sslContext = buildSslContext();
            settings.applyToSslSettings(ssl -> ssl.enabled(true).context(sslContext));
            LOGGER.info("TLS enabled (truststore: {}, mTLS: {})", props.getTruststore(), props.isMtlsEnabled());
        }

        return MongoClients.create(settings.build());
    }

    @Bean("mongoTemplate")
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        MongoTemplate template = new MongoTemplate(mongoClient, props.getDatabase());

        // remove the _class field from inserted documents
        MappingMongoConverter converter = (MappingMongoConverter) template.getConverter();
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        converter.afterPropertiesSet();

        return template;
    }

    private String buildUri() {
        if (notBlank(props.getUrl())) {
            return props.getUrl();
        }

        StringBuilder uri = new StringBuilder("mongodb://");
        if (notBlank(props.getUsername())) {
            uri.append(enc(props.getUsername()));
            if (notBlank(props.getPassword())) {
                uri.append(':').append(enc(props.getPassword()));
            }
            uri.append('@');
        }
        uri.append(props.getHost()).append(':').append(props.getPort());
        uri.append('/').append(props.getDatabase() != null ? props.getDatabase() : "");

        String sep = "?";
        if (props.isSslEnabled()) {
            uri.append(sep).append("tls=true");
            sep = "&";
        }
        if (notBlank(props.getExtraParams())) {
            uri.append(sep).append(props.getExtraParams());
        }
        return uri.toString();
    }

    private SSLContext buildSslContext() {
        try {
            TrustManagerFactory tmf = null;
            KeyManagerFactory kmf = null;

            if (notBlank(props.getTruststore())) {
                KeyStore trust = loadKeyStore(props.getTruststore(), props.getTruststorePassword());
                tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(trust);
            }

            if (props.isMtlsEnabled()) {
                if (!notBlank(props.getKeystore())) {
                    throw new IllegalStateException("db.mongodb.keystore is required when mtlsEnabled=true");
                }
                KeyStore keys = loadKeyStore(props.getKeystore(), props.getKeystorePassword());
                kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(keys, toChars(props.getKeystorePassword()));
            }

            // null managers = JVM defaults
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(kmf != null ? kmf.getKeyManagers() : null, tmf != null ? tmf.getTrustManagers() : null, null);
            return ctx;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build MongoDB SSL context: " + e.getMessage(), e);
        }
    }

    private static KeyStore loadKeyStore(String path, String password) throws Exception {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream in = Files.newInputStream(Path.of(path))) {
            ks.load(in, toChars(password));
        }
        return ks;
    }

    private static char[] toChars(String s) {
        return s != null ? s.toCharArray() : null;
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
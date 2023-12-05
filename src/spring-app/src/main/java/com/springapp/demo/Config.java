package com.springapp.demo;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.zaxxer.hikari.HikariDataSource;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

// Config represents configuration for the app.
@Component
@ConfigurationProperties()
public class Config {
    private String otlpEndpoint;
    public String getOtlpEndpoint() {
        return otlpEndpoint;
    }
    public void setOtlpEndpoint(String otlpEndpoint) {
        this.otlpEndpoint = otlpEndpoint;
    }
    @ConfigurationProperties(prefix = "s3")
    @Component
    public class S3Config {
        // Region for the S3 bucket.
        private String region;
        // S3 bucket name to store images.
        private String bucket;
        // S3 endpoint, since we use Minio we must provide
        // a custom endpoint. It should be a DNS of Minio instance.
        private String endpoint;
        // User to access S3 bucket.
        private String user;
        // Secret to access S3 bucket.
        private String secret;
        // Enable path S3 style; we must enable it to use Minio.
        private Boolean pathStyle;

        // s3Connect initializes the S3 client.
        @Bean
        public AmazonS3 s3Client() {
            AwsClientBuilder.EndpointConfiguration endpointConfig = new AwsClientBuilder.EndpointConfiguration(endpoint, "us-west-rack1");
            AWSCredentials credentials = new BasicAWSCredentials(user, secret);

            return AmazonS3ClientBuilder.standard()
                    .withCredentials(new ProfileCredentialsProvider())
                    .withEndpointConfiguration(endpointConfig)
                    .withPathStyleAccessEnabled(true)
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .build();
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Boolean getPathStyle() {
            return pathStyle;
        }

        public void setPathStyle(Boolean pathStyle) {
            this.pathStyle = pathStyle;
        }
    }

    @ConfigurationProperties(prefix = "db")
    @Component
    public static class DbConfig {
        // Database to store images.
        private String database;
        // Host to connect database.
        private String host;
        // User to connect database.
        private String user;
        // Password to connect database.
        private String password;

        // dbConnect creates a connection pool to connect to Postgres.
        @Bean
        public HikariDataSource dataSource() {
            DataSourceBuilder<HikariDataSource> dataSourceBuilder = DataSourceBuilder.create().type(HikariDataSource.class);
            dataSourceBuilder.url("jdbc:postgresql://" + host + ":5432/" + database);
            dataSourceBuilder.username(user);
            dataSourceBuilder.password(password);
            return dataSourceBuilder.build();
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
    @Bean
    public OpenTelemetry openTelemetry() {
        Resource resource = Resource.getDefault().toBuilder().put(ResourceAttributes.SERVICE_NAME, "spring-app").put(ResourceAttributes.SERVICE_VERSION, "0.1.0").build();

        OtlpGrpcSpanExporter otlpGrpcExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint("http://" + otlpEndpoint)
                .build();

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                //.addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
                .addSpanProcessor(BatchSpanProcessor.builder(otlpGrpcExporter).build())
                .setResource(resource)
                .build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setPropagators(ContextPropagators.create(TextMapPropagator.composite(W3CTraceContextPropagator.getInstance(), W3CBaggagePropagator.getInstance())))
                .buildAndRegisterGlobal();

        return openTelemetry;
    }
}

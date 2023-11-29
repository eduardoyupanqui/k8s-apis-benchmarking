package com.quarkusapp;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.supplier.AgroalConnectionFactoryConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalConnectionPoolConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.Produces;

import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class DbConfiguration {
    private final Config.DbConfig dbConfig;
    @Inject
    public DbConfiguration(Config.DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }
    @Produces()
    @Named("customAgroalDataSource")
    private AgroalDataSource dbConnect() throws SQLException {
        // create supplier
        AgroalDataSourceConfigurationSupplier dataSourceConfiguration = new AgroalDataSourceConfigurationSupplier();
        // get reference to connection pool
        AgroalConnectionPoolConfigurationSupplier poolConfiguration = dataSourceConfiguration.connectionPoolConfiguration();
        // get reference to connection factory
        AgroalConnectionFactoryConfigurationSupplier connectionFactoryConfiguration = poolConfiguration.connectionFactoryConfiguration();

        // configure pool
        poolConfiguration
                .initialSize(10)
                .maxSize(10)
                .minSize(10)
                .maxLifetime(Duration.of(5, ChronoUnit.MINUTES))
                .acquisitionTimeout(Duration.of(30, ChronoUnit.SECONDS));

        // configure supplier
        connectionFactoryConfiguration
                .jdbcUrl("jdbc:postgresql://" + dbConfig.host() + ":5432/" + dbConfig.database())
                .credential(new NamePrincipal(dbConfig.user()))
                .credential(new SimplePassword(dbConfig.password()));

        return AgroalDataSource.from(dataSourceConfiguration.get());
    }

}

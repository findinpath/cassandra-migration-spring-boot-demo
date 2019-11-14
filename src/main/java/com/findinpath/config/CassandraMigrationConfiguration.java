package com.findinpath.config;


import com.datastax.driver.core.Cluster;
import org.cognitor.cassandra.migration.MigrationTask;
import org.cognitor.cassandra.migration.spring.CassandraMigrationAutoConfiguration;
import org.cognitor.cassandra.migration.spring.CassandraMigrationConfigurationProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Custom version of the {@link CassandraMigrationAutoConfiguration} which allows the usage of a
 * Cassandra {@link Cluster} instance specially defined only for database cql migration purposes
 * responsible of executing CREATE, ALTER, DROP statements, instead of the application {@link
 * Cluster} instance, which would have been chosen by default and which is entitled to do only
 * SELECT, INSERT, DELETE statements.
 *
 * @see CassandraMigrationCluster
 */
@Configuration
@EnableConfigurationProperties({CassandraMigrationConfigurationProperties.class})
@ConditionalOnClass({Cluster.class})
public class CassandraMigrationConfiguration extends CassandraMigrationAutoConfiguration {

  @Autowired
  public CassandraMigrationConfiguration(CassandraMigrationConfigurationProperties properties) {
    super(properties);
  }


  @Bean(initMethod = "migrate")
  @ConditionalOnMissingBean(MigrationTask.class)
  public MigrationTask migrationTask(
      @CassandraMigrationCluster ObjectProvider<Cluster> migrationClusterProvider) {
    Cluster migrationCluster = migrationClusterProvider.getIfAvailable();
    return super.migrationTask(migrationCluster);
  }


}

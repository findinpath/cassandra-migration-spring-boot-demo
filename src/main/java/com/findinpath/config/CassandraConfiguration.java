package com.findinpath.config;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.PoolingOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;

/**
 * This configuration serves for configuring the client Cassandra clusters that the application is
 * using for interacting with the database.
 *
 * NOTE that there are two cluster instances created:
 *
 * <ul>
 *   <li>applicationCluster</li>
 *   <li>migrationCluster</li>
 * </ul>
 *
 * The application cluster is used for general queries required
 * by the application functionality (`SELECT`, `INSERT`, `DELETE`).
 * The migration cluster on the other hand is used especially for
 * schema migration queries (`CREATE`, `ALTER`, `DROP`).
 *
 * Therefor spring data cassandra repository classes will make use
 * of the application cluster for interacting with Cassandra.
 */
@Configuration
@Profile("!test")
public class CassandraConfiguration {

  @Bean
  @CassandraMigrationCluster
  public CassandraClusterFactoryBean migrationCluster(
      @Value("${cassandra.contact.points}") String contactPoints,
      @Value("${cassandra.migration.username}") String username,
      @Value("${cassandra.migration.password}") String password) {
    PoolingOptions poolingOptions = new PoolingOptions()
        .setConnectionsPerHost(HostDistance.LOCAL, 1, 1)
        .setConnectionsPerHost(HostDistance.REMOTE, 1, 1)
        .setMaxRequestsPerConnection(HostDistance.LOCAL, 1)
        .setMaxRequestsPerConnection(HostDistance.REMOTE, 1);

    return createCassandraClusterFactoryBean(poolingOptions, contactPoints, username, password);
  }

  @Bean(name = "applicationCluster")
  public CassandraClusterFactoryBean applicationCluster(
      @Value("${cassandra.contact.points}") String contactPoints,
      @Value("${cassandra.application.username}") String username,
      @Value("${cassandra.application.password}") String password) {
    PoolingOptions poolingOptions = new PoolingOptions();
    return createCassandraClusterFactoryBean(poolingOptions, contactPoints, username, password);
  }

  private CassandraClusterFactoryBean createCassandraClusterFactoryBean(
      PoolingOptions poolingOptions,
      String contactPoints,
      String username,
      String password) {
    CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();

    cluster.setContactPoints(contactPoints);
    cluster.setPoolingOptions(poolingOptions);

    PlainTextAuthProvider authProvider = new PlainTextAuthProvider(username, password);
    cluster.setAuthProvider(authProvider);

    return cluster;
  }

  @Bean
  public CassandraMappingContext mappingContext() {
    return new CassandraMappingContext();
  }

  @Bean
  public CassandraConverter cassandraConverter() {
    return new MappingCassandraConverter(mappingContext());
  }

  @Bean
  public CassandraSessionFactoryBean cassandraSessionFactoryBean(
      @Qualifier("applicationCluster") CassandraClusterFactoryBean applicationCluster,
      @Value("${cassandra.application.keyspaceName}") String keyspaceName) {

    Cluster cluster = applicationCluster.getObject();
    CassandraSessionFactoryBean session = new CassandraSessionFactoryBean();
    session.setCluster(cluster);
    session.setKeyspaceName(keyspaceName);
    session.setConverter(cassandraConverter());
    session.setSchemaAction(SchemaAction.NONE);

    return session;
  }

  @Bean
  public CassandraOperations cassandraTemplate(
      CassandraSessionFactoryBean cassandraSessionFactoryBean) {
    return new CassandraTemplate(cassandraSessionFactoryBean.getObject());
  }
}
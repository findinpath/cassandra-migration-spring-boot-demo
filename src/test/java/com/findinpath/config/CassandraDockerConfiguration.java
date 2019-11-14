package com.findinpath.config;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import org.testcontainers.containers.CassandraContainer;

/**
 * The counterpart class of {@link CassandraConfiguration} which is used for test purposes.
 *
 * This configuration class doesn't rely on an external database which needs authentication &
 * authorization setup (like in the case of {@link CassandraConfiguration}). Through the help of
 * testcontainers cassandra module there will be spawned a Cassandra database container to which
 * both the application cluster as well as the migration cluster will connect to with superuser
 * permissions.
 */
@Configuration
public class CassandraDockerConfiguration {

  private static final String CASSANDRA_DOCKER_IMAGE_VERSION = ":3.11";
  private static final String KEYSPACE = "demo";
  private static final String CASSANDRA_INIT_SCRIPT = "demo-dataset.cql";

  @Bean
  public CassandraContainer cassandraContainer() {
    var cassandraContainer = new CassandraContainer(CassandraContainer.IMAGE
        + CASSANDRA_DOCKER_IMAGE_VERSION);
    cassandraContainer.start();

    loadTestData(cassandraContainer);
    return cassandraContainer;
  }

  @Bean("applicationCluster")
  public CassandraClusterFactoryBean applicationCluster(CassandraContainer cassandraContainer) {
    final CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();

    cluster.setContactPoints(cassandraContainer.getContainerIpAddress());
    cluster.setPort(cassandraContainer.getFirstMappedPort());
    return cluster;
  }

  @Bean("migrationCluster")
  @CassandraMigrationCluster
  public CassandraClusterFactoryBean migrationCluster(CassandraContainer cassandraContainer) {
    final CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();

    cluster.setContactPoints(cassandraContainer.getContainerIpAddress());
    cluster.setPort(cassandraContainer.getFirstMappedPort());
    return cluster;
  }

  @Bean
  public CassandraMappingContext cassandraMapping() {
    return new CassandraMappingContext();
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
      @Qualifier("applicationCluster") CassandraClusterFactoryBean applicationCluster) {

    Cluster cluster = applicationCluster.getObject();
    CassandraSessionFactoryBean session = new CassandraSessionFactoryBean();
    session.setCluster(cluster);
    session.setKeyspaceName(KEYSPACE);
    session.setConverter(cassandraConverter());
    session.setSchemaAction(SchemaAction.NONE);

    return session;
  }

  @Bean
  public CassandraOperations cassandraTemplate(
      CassandraSessionFactoryBean cassandraSessionFactoryBean) {
    return new CassandraTemplate(cassandraSessionFactoryBean.getObject());
  }

  private void loadTestData(CassandraContainer cassandraContainer) {
    ClassPathCQLDataSet dataSet = new ClassPathCQLDataSet(CASSANDRA_INIT_SCRIPT, KEYSPACE);
    Session session = cassandraContainer.getCluster().connect();
    CQLDataLoader dataLoader = new CQLDataLoader(session);
    dataLoader.load(dataSet);
    session.close();
  }

}
package com.findinpath;

import org.cognitor.cassandra.migration.spring.CassandraMigrationAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;


@SpringBootApplication(exclude = {CassandraDataAutoConfiguration.class,
    CassandraMigrationAutoConfiguration.class})
public class DemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }
}

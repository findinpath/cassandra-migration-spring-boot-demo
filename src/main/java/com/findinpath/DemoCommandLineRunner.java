package com.findinpath;

import com.findinpath.repository.SchemaMigrationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


/**
 * This command line runner is used as a showcase for the schema migrations executed on the
 * Cassandra database at the application startup.
 */
@Component
@Profile("!test")
public class DemoCommandLineRunner implements CommandLineRunner {

  private static Logger LOG = LoggerFactory.getLogger(DemoApplication.class);

  private final SchemaMigrationRepository schemaMigrationRepository;

  private final ConfigurableApplicationContext applicationContext;

  public DemoCommandLineRunner(ConfigurableApplicationContext applicationContext,
      SchemaMigrationRepository schemaMigrationRepository) {
    this.applicationContext = applicationContext;
    this.schemaMigrationRepository = schemaMigrationRepository;
  }


  @Override
  public void run(String... args) {
    try {
      var schemaMigrations = schemaMigrationRepository.findAll();
      LOG.info("Listing the schema migrations");
      schemaMigrations.forEach(schemaMigration ->
          LOG.info("Schema migration applied: " + schemaMigration.getSchemaMigrationKey()
              .isAppliedSuccessful() +
              " version: " + schemaMigration.getSchemaMigrationKey().getVersion() +
              " script name: " + schemaMigration.getScriptName()));

    } finally {
      // make sure to close the Spring application context after the execution of the
      // business logic of the application is done.
      applicationContext.close();
    }
  }
}

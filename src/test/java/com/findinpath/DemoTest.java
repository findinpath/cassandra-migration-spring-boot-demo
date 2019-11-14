package com.findinpath;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.findinpath.repository.SchemaMigrationRepository;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest()
@ActiveProfiles("test")
@TestPropertySource(properties = {"cassandra.migration.keyspaceName = demo",
    "cassandra.migration.scriptLocation: cassandra/migration"})
public class DemoTest {

  @Autowired
  private SchemaMigrationRepository schemaMigrationRepository;

  @Autowired
  private CassandraTemplate cassandraTemplate;

  @Test
  public void demo() {

    var schemaMigrations = StreamSupport
        .stream(schemaMigrationRepository.findAll().spliterator(), false)
        .collect(Collectors.toList());
    assertThat(schemaMigrations, hasSize(2));
    assertThat(schemaMigrations.get(0).getScriptName(), equalTo("0001_create_users_table.cql"));
    assertThat(schemaMigrations.get(1).getScriptName(),
        equalTo("0002_create_user_bookmarks_table.cql"));

    var cql = "SELECT table_name FROM system_schema.tables WHERE keyspace_name='demo'";
    var tableNames = cassandraTemplate.select(cql, String.class);
    // schema_migration and schema_migration_leader are tables created by the
    // cassandra-migration libray to keep track of the CQL migrations
    // already executed on the Cassandra database.
    assertThat(tableNames,
        containsInAnyOrder("schema_migration", "schema_migration_leader", "user_bookmarks",
            "users"));
  }
}

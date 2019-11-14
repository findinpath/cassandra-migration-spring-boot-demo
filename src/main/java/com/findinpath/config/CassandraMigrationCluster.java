package com.findinpath.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Qualifier annotation for a Cassandra Cluster to be injected in the application context for
 * perfoming the Cassandra schema migration. In order to perform cassandra schema migration, the
 * corresponding Cassandra user needs extensive rights (CREATE, DROP) on the database. This is why
 * it is highly important to split the responsibilities between:
 * <ul>
 *   <li>Cassandra user which should make use only of SELECT, INSERT, DELETE statements</li>
 *   <li>Cassandra migration user which should make use only of CREATE, DROP, ALTER statements</li>
 * </ul>
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE,
    ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier
public @interface CassandraMigrationCluster {

}
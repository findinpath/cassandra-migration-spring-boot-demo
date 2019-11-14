package com.findinpath.repository;

import com.findinpath.model.SchemaMigration;
import com.findinpath.model.SchemaMigrationKey;
import org.springframework.data.repository.CrudRepository;

public interface SchemaMigrationRepository extends
    CrudRepository<SchemaMigration, SchemaMigrationKey> {

}
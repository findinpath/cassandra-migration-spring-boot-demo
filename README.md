Demo for Cassandra schema migrations on application startup 
===========================================================

This proof of concept application shows how to perform Cassandra database schema
migrations on application startup.
In the relational database world,  [flyway](https://flywaydb.org/)
is extensively used for such purposes. On the other hand, there is no 
standard tool at the moment for performing this job in the Cassandra world.

This often leads to runtime problems (e.g.: table not found) in the applications 
that use Cassandra to store their data, because during the deployment the engineering team
forgot to execute manually the schema migrations on the Cassandra database.

This demo makes use of the [cassandra-migration](https://github.com/patka/cassandra-migration)
library and integrates it into a [spring-boot](https://spring.io/projects/spring-boot) application
so that on application startup the CQL migration files are applied one after the other on the database.

What this demo is bringing new is the fact that it separates the concern of performing
Cassandra schema migrations (`CREATE`, `ALTER`, `DROP`) from the concern of performing 
Cassandra queries related to the application functionality (`SELECT`, `INSERT`, `DELETE`). 
In this way, there can be used a user with retricted role permissions for performing
the queries needed in covering the application functionality. 


## Setup Cassandra locally

For setting up the Cassandra database locally, there is no need to perform a manual installation
of the database on your local machine.

[Docker](https://www.docker.com/) can be used for creating a Cassandra container instance.
By default, the Cassandra image has no authentication or authorization. 

This is fine for most of the usecases, but this demo makes a clear separation between the
Cassandra user needed for application purposes (mainly `SELECT`, `INSERT`, `DELETE` statements)
and the Cassandra user needed for schema migration 
purposes (mainly `CREATE`, `ALTER`, `DROP` statements).

Having this clear separation of responsibilities makes the application less vulnerable.
In case that the application has a security hole, the attacker will be able to use only the
application user which is much more restricted in terms of permissions than the schema 
migration user.

Due to the fact, that there is no official Cassandra docker image that comes out of the
box with such roles, there will be needed a few manual steps in order to setup the database
which is to be used by the demo application.

*NOTE*: In case you intend only to run the tests from this project, this step is not necessary
because the tests use the default Cassandra image (without authentication or authorization).

```bash
$ docker run  --name cassandra-migration-demo -v $(pwd)/cassandra:/demo -p 9042:9042 -d cassandra:latest -Dcassandra.config=/demo/cassandra.yaml
```

Now we can proceed to create the users and the keyspace for our demo by executing the following command:

```bash
$ docker exec -it cassandra-migration-demo cqlsh -u cassandra -p cassandra -f /demo/demo.cql
```

*NOTE* that the file `/demo/demo.cql` was already copied on the container while it got created. 

There are now two users created :

- `demo_user` WITH PASSWORD `notsoeasy`
- `demo_migration` WITH PASSWORD `rollingstones`

This concludes the setup of the Cassandra database container on the localhost.

When the Cassandra container required by this demo is not needed anymore, it can be removed:

```bash
$ docker rm cassandra-migration-demo
``` 


## Running the DemoApplication

When running the main class `com.findinpath.DemoApplication` the log output of the application
should resemble to the listing below:

```bash
INFO 2080 --- [           main] com.findinpath.DemoApplication           : Listing the schema migrations
INFO 2080 --- [           main] com.findinpath.DemoApplication           : Schema migration applied: true version: 1 script name: 0001_create_users_table.cql
INFO 2080 --- [           main] com.findinpath.DemoApplication           : Schema migration applied: true version: 2 script name: 0002_create_user_bookmarks_table.cql
```

As seen in the listing, the applied migrations correspond to the .cql migration files
from [migrations directory](./src/main/resources/cassandra/migration).


## Running the tests

In order to run the tests, the [Cassandra module](https://www.testcontainers.org/modules/databases/cassandra/)
from the [testcontainers](https://www.testcontainers.org) library is used for using a
throwaway docker container instance of Cassandra database.

The tests don't need any additional manual steps on the testing machine.
At the beginning of the tests a Cassandra docker container will be started, the CQL migrations
will be applied on top of it and then the tests will run. At the end of the tests, the Cassandra
database container will be deleted.
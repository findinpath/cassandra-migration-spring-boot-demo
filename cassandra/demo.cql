CREATE KEYSPACE IF NOT EXISTS demo WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;
CREATE USER demo_user WITH PASSWORD 'notsoeasy';
CREATE USER demo_migration WITH PASSWORD 'rollingstones';

INSERT INTO system_auth.role_permissions (role,resource,permissions) VALUES ('demo_user','data/demo',{'MODIFY','SELECT'});
INSERT INTO system_auth.role_permissions (role,resource,permissions) VALUES ('demo_migration','data/demo',{'ALTER','CREATE','DROP','MODIFY','SELECT'});
INSERT INTO system_auth.role_permissions (role,resource,permissions) VALUES ('demo_migration','data/demo/schema_migration',{'ALTER','AUTHORIZE','DROP','MODIFY','SELECT'});

CREATE KEYSPACE IF NOT EXISTS demo WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

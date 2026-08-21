# BigQuery Migration Service Metadata and Log Dumper

This directory contains the Metadata and Log Dumper, a command line tool for
connecting to an existing database and generating an archive of DDL metadata or
logs. This tool generates archives in a format suitable for consumption by the
[BigQuery Migration Service's][BQMS] Assessment or Translation Service.

The Dumper is a Java tool. **[Download the latest cross-platform release zip `dwh-migration-tools-vX.X.X.zip`.](https://github.com/google/dwh-migration-tools/releases/latest)**

Compiling the Dumper from source requires `Java 8`, running the Dumper requires `Java 8` or higher. To check Java version run the command
`java -version` or refer to Java vendor documentation. Third party JDBC drivers
might impose additional restrictions on Java versions. Refer to the JDBC
driver's manual for details.

To get started using the Dumper, read
[the documentation](https://cloud.google.com/bigquery/docs/generate-metadata).

## Databricks

The `databricks` connector dumps tables registered in Unity Catalog. It can
also dump tables in the legacy `hive_metastore` catalog when a Databricks SQL
warehouse ID is provided with `--warehouse`.

### Unity Catalog only

Without `--warehouse`, the connector uses the Unity Catalog REST API and dumps
Unity Catalog tables and their table metadata, including column definitions:

```bash
./gradlew :dumper:app:run --args="--connector databricks \
	--url https://<workspace-host> \
	--password <databricks-token>"
```

### Unity Catalog and Hive Metastore

With `--warehouse <sql-warehouse-id>`, the connector dumps both Unity Catalog
and Hive Metastore tables. Hive Metastore metadata is collected through the
Databricks SQL Statement Execution API using the configured SQL warehouse.
For each Hive Metastore table, the connector collects column definitions with
`DESCRIBE TABLE` and writes them in the same `meta.columns` format used for
Unity Catalog tables.

```bash
./gradlew :dumper:app:run --args="--connector databricks \
	--url https://<workspace-host> \
	--password <databricks-token> \
	--warehouse <sql-warehouse-id>"
```

The token must be authorized to access the workspace and execute statements on
the SQL warehouse. The output JSONL identifies legacy tables with
`catalog: "hive_metastore"`; for example,
`hive_metastore.default.example_table`.


[BQMS]: https://cloud.google.com/bigquery/docs/migration-intro

# Databricks Unity Catalog prototype

This is a small prototype added on branch `feature/databricks-catalogs-schemas`.

Usage

Run the dumper with the databricks connector (example):

```
./gradlew :dumper:app:run --args="--connector databricks --url https://<WORKSPACE_HOST> --password <PAT> --output /tmp/databricks-dump.zip"
```

Output

- The dumper writes a JSON Lines file `databricks-uc.jsonl` inside the output ZIP. Each line is a JSON object containing basic fields:
  - workspace, catalog, schema, table, table_full, meta

Limitations (prototype)

- Single-workspace, PAT-based only.
- No pagination, retries, or rate-limiting.
- Minimal fields and minimal error handling. Use only for quick inspection and iterate further for production.

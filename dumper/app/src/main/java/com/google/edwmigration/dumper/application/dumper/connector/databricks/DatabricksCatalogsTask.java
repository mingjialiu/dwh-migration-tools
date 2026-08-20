package com.google.edwmigration.dumper.application.dumper.connector.databricks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteSink;
import com.google.edwmigration.dumper.application.dumper.handle.Handle;
import com.google.edwmigration.dumper.application.dumper.task.AbstractTask;
import com.google.edwmigration.dumper.application.dumper.task.TaskRunContext;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class DatabricksCatalogsTask extends AbstractTask<Void> {
  private final ObjectMapper mapper = new ObjectMapper();

  public DatabricksCatalogsTask() {
    super("databricks-uc.jsonl");
  }

  @Override
  protected Void doRun(TaskRunContext context, @Nonnull ByteSink sink, @Nonnull Handle handle)
      throws Exception {
    DatabricksHandle dbHandle = (DatabricksHandle) handle;
    DatabricksClient client = dbHandle.getClient();

    try (Writer writer = sink.asCharSink(StandardCharsets.UTF_8).openBufferedStream()) {
      List<Map<String, Object>> catalogs = client.listCatalogs();
      for (Map<String, Object> catalog : catalogs) {
        String catalogName = String.valueOf(catalog.get("name"));
        if (catalogName == null) continue;
        List<Map<String, Object>> schemas = client.listSchemas(catalogName);
        for (Map<String, Object> schema : schemas) {
          String schemaName = String.valueOf(schema.get("name"));
          if (schemaName == null) continue;
          List<Map<String, Object>> tables = client.listTables(catalogName, schemaName);
          for (Map<String, Object> table : tables) {
            // Build minimal output
            Map<String, Object> out = Map.of(
                "workspace", client.getBaseUrl(),
                "catalog", catalogName,
                "schema", schemaName,
                "table", table.get("name"),
                "table_full", table.getOrDefault("full_name", table.get("name")),
                "meta", table
            );
            writer.write(mapper.writeValueAsString(out));
            writer.write("\n");
          }
        }
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return "Dump Unity Catalog tables to databricks-uc.jsonl";
  }
}

package com.google.edwmigration.dumper.application.dumper.connector.databricks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Minimal Databricks HTTP client for Unity Catalog endpoints. Prototype quality. */
public class DatabricksClient {
  private final String baseUrl;
  private final String token;
  private final ObjectMapper mapper = new ObjectMapper();

  public DatabricksClient(String baseUrl, String token) {
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    this.token = token;
  }

  public List<Map<String, Object>> listCatalogs() throws IOException {
    JsonNode root = getJson("/api/2.1/unity-catalog/catalogs");
    return nodeToListOfMaps(root, "catalogs");
  }

  public List<Map<String, Object>> listSchemas(String catalogName) throws IOException {
    String q = "?catalog_name=" + urlEncode(catalogName);
    JsonNode root = getJson("/api/2.1/unity-catalog/schemas" + q);
    return nodeToListOfMaps(root, "schemas");
  }

  public List<Map<String, Object>> listTables(String catalogName, String schemaName) throws IOException {
    String q = "?catalog_name=" + urlEncode(catalogName) + "&schema_name=" + urlEncode(schemaName);
    JsonNode root = getJson("/api/2.1/unity-catalog/tables" + q);
    return nodeToListOfMaps(root, "tables");
  }

  private String urlEncode(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }

  private JsonNode getJson(String path) throws IOException {
    URL url = new URL(baseUrl + path);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    if (token != null && !token.isEmpty()) {
      conn.setRequestProperty("Authorization", "Bearer " + token);
    }
    conn.setRequestProperty("Accept", "application/json");
    int code = conn.getResponseCode();
    InputStream is = (code >= 200 && code <= 299) ? conn.getInputStream() : conn.getErrorStream();
    try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      String body = r.lines().collect(Collectors.joining("\n"));
      if (code < 200 || code > 299) {
        throw new IOException("Databricks API request failed: " + code + " " + body);
      }
      return mapper.readTree(body);
    } finally {
      conn.disconnect();
    }
  }

  private List<Map<String, Object>> nodeToListOfMaps(JsonNode node, String fieldName) {
    if (node == null || node.isNull()) return Collections.emptyList();
    JsonNode arr = node;
    if (node.has(fieldName)) {
      arr = node.get(fieldName);
    }
    if (arr == null || !arr.isArray()) return Collections.emptyList();
    List<Map<String, Object>> out = new ArrayList<>();
    Iterator<JsonNode> it = arr.elements();
    while (it.hasNext()) {
      JsonNode item = it.next();
      Map<String, Object> map = mapper.convertValue(item, Map.class);
      out.add(map);
    }
    return out;
  }
}

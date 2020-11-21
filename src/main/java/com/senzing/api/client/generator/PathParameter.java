package com.senzing.api.client.generator;

import com.senzing.api.client.generator.schema.ApiDataType;
import com.senzing.util.JsonUtils;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import static java.lang.Boolean.TRUE;

/**
 * Describes a path parameter for a rest operation.
 */
public class PathParameter extends Parameter {
  /**
   * Default constructor.
   */
  public PathParameter() {
    // do nothing
  }

  /**
   * Builds the JSON representation for this object.
   *
   * @param builder The {@link JsonObjectBuilder} for creating the JSON
   *                representation of this object.
   */
  public void buildJson(JsonObjectBuilder builder) {
    builder.add("in", "path");
    builder.add("name", this.getName());
    if (this.getDescription() != null) builder.add("description", this.getDescription());
    builder.add("required", true);
    ApiDataType dataType = this.getDataType();
    if (dataType != null) {
      JsonObjectBuilder job = Json.createObjectBuilder();
      dataType.buildJson(job);
      builder.add("schema", job);
    }
  }
}

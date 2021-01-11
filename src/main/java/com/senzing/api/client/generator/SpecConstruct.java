package com.senzing.api.client.generator;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Map;

import static com.senzing.util.JsonUtils.*;

public interface SpecConstruct {
  /**
   * Populates the specified {@link JsonObjectBuilder} with the properties from
   * this object.
   *
   * @param builder The {@link JsonObjectBuilder} to populate.
   */
  void buildJson(JsonObjectBuilder builder);

  /**
   * Converts this object to a {@link JsonObject}.
   *
   * @return This object represented as a {@link JsonObject}.
   */
  default JsonObject toJson() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    this.buildJson(builder);
    return builder.build();
  }

  /**
   * Converts this object to a handlebars template context map.
   *
   * @return The {@link Map} describing the properties of this object.
   */
  default Map<String, Object> toContextMap() {
    Object normalized = normalizeJsonValue(this.toJson());
    return (Map<String, Object>) normalized;
  }
}

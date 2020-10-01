package com.senzing.api.client.generator.schema;

import com.senzing.util.JsonUtils;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Extends {@link ApiDataType} to represent reference types.
 */
public class BooleanDataType extends ApiDataType {
  /**
   * Default constructor to construct an anonymous instance.
   */
  public BooleanDataType() {
    this(null);
  }

  /**
   * Constructs with a optional name for a named instance.
   *
   * @param name The optional name for the instance, or <tt>null</tt> if
   *             anonymous.
   */
  public BooleanDataType(String name) {
    super(name);
  }

  @Override
  public void buildJson(JsonObjectBuilder builder) {
    super.buildJson(builder);
    builder.add("type", "boolean");
  }

  /**
   * Parses a boolean data type with the specified optional name using the
   * specified {@link JsonObject}.
   *
   * @param name The optional name for the type, or <tt>null</tt> if it is
   *             an anonymous type.
   * @param jsonObject The {@link JsonObject} containing the information for
   *                   the data type.
   */
  public static BooleanDataType parse(String name, JsonObject jsonObject) {
    String typeValue = JsonUtils.getString(jsonObject, "type");
    if (!"boolean".equals(typeValue)) {
      throw new IllegalArgumentException(
          "Not a valid definition for a BooleanDataType: " + typeValue);
    }
    BooleanDataType booleanType = new BooleanDataType(name);

    return booleanType;
  }
}

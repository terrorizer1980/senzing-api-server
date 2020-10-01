package com.senzing.api.client.generator.schema;

import com.senzing.util.JsonUtils;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Extends {@link ApiDataType} to represent a binary data type.
 */
public class BinaryDataType extends ApiDataType {
  /**
   * Default constructor to construct an anonymous instance.
   */
  public BinaryDataType() {
    super();
  }

  /**
   * Constructs a named instances with the specified name.
   *
   * @param name The name for the type.
   */
  public BinaryDataType(String name) {
    super(name);
  }

  @Override
  public void buildJson(JsonObjectBuilder builder) {
    super.buildJson(builder);
    builder.add("type", "string");
    builder.add("format", "binary");
  }

  /**
   * Parses a binary data type with the specified optional name using the
   * specified {@link JsonObject}.
   *
   * @param name The optional name for the type, or <tt>null</tt> if it is
   *             an anonymous type.
   * @param jsonObject The {@link JsonObject} containing the information for
   *                   the data type.
   */
  public static BinaryDataType parse(String name, JsonObject jsonObject) {
    String typeValue = JsonUtils.getString(jsonObject, "type");
    if (!"string".equals(typeValue)) {
      throw new IllegalArgumentException(
          "Not a valid definition for a BinaryDataType: " + typeValue);
    }
    BinaryDataType binType = new BinaryDataType(name);
    String format = JsonUtils.getString(jsonObject, "format");
    if (!"binary".equals(format)) {
      throw new IllegalArgumentException(
          "Not a valid format for an BinaryDataType: " + format);
    }

    return binType;
  }

}

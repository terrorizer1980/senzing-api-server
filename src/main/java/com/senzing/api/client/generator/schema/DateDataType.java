package com.senzing.api.client.generator.schema;

import com.senzing.util.JsonUtils;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Extends {@link ApiDataType} to represent a date data type.
 */
public class DateDataType extends ApiDataType {
  /**
   * Default constructor to construct an anonymous instance.
   */
  public DateDataType() {
    super();
  }

  /**
   * Constructs a named instances with the specified name.
   *
   * @param name The name for the type.
   */
  public DateDataType(String name) {
    super(name);
  }

  @Override
  public void buildJson(JsonObjectBuilder builder) {
    super.buildJson(builder);
    builder.add("type", "string");
    builder.add("format", "date");
  }

  /**
   * Parses a date data type with the specified optional name using the
   * specified {@link JsonObject}.
   *
   * @param name The optional name for the type, or <tt>null</tt> if it is
   *             an anonymous type.
   * @param jsonObject The {@link JsonObject} containing the information for
   *                   the data type.
   */
  public static DateDataType parse(String name, JsonObject jsonObject) {
    String typeValue = JsonUtils.getString(jsonObject, "type");
    if (!"string".equals(typeValue)) {
      throw new IllegalArgumentException(
          "Not a valid definition for a DateDataType: " + typeValue);
    }
    DateDataType dateType = new DateDataType(name);
    String format = JsonUtils.getString(jsonObject, "format");
    if (!"date".equals(format)) {
      throw new IllegalArgumentException(
          "Not a valid format for an DateDataType: " + format);
    }

    return dateType;
  }

}

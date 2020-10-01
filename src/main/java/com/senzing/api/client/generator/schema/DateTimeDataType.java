package com.senzing.api.client.generator.schema;

import com.senzing.util.JsonUtils;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Extends {@link ApiDataType} to represent a date data type.
 */
public class DateTimeDataType extends ApiDataType {
  /**
   * Default constructor to construct an anonymous instance.
   */
  public DateTimeDataType() {
    super();
  }

  /**
   * Constructs a named instances with the specified name.
   *
   * @param name The name for the type.
   */
  public DateTimeDataType(String name) {
    super(name);
  }

  @Override
  public void buildJson(JsonObjectBuilder builder) {
    super.buildJson(builder);
    builder.add("type", "string");
    builder.add("format", "date-time");
  }

  /**
   * Parses a date-time integer data type with the specified optional name using the
   * specified {@link JsonObject}.
   *
   * @param name The optional name for the type, or <tt>null</tt> if it is
   *             an anonymous type.
   * @param jsonObject The {@link JsonObject} containing the information for
   *                   the data type.
   */
  public static DateTimeDataType parse(String name, JsonObject jsonObject) {
    String typeValue = JsonUtils.getString(jsonObject, "type");
    if (!"string".equals(typeValue)) {
      throw new IllegalArgumentException(
          "Not a valid definition for a DateTimeDataType: " + typeValue);
    }
    DateTimeDataType dateType = new DateTimeDataType(name);
    String format = JsonUtils.getString(jsonObject, "format");
    if (!"date-time".equals(format)) {
      throw new IllegalArgumentException(
          "Not a valid format for an DateTimeDataType: " + format);
    }

    return dateType;
  }

}

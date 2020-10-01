package com.senzing.api.client.generator.schema;

import com.senzing.util.JsonUtils;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Extends {@link ApiDataType} to describe an long integer type and its
 * properties.
 */
public class LongDataType extends NumberDataType<Long> {
  /**
   * Default constructor for constructing an anonymous type.
   */
  public LongDataType() {
    super();
  }

  /**
   * Constructs a named type.
   */
  public LongDataType(String name) {
    super(name);
  }

  @Override
  public void buildJson(JsonObjectBuilder builder) {
    super.buildJson(builder);
    builder.add("type", "integer");
    builder.add("format", "int64");
    Long min = this.getMinimum();
    if (min != null) {
      builder.add("minimum", min.longValue());
      if (this.isExclusiveMinimum()) {
        builder.add("minimumExclusive", "true");
      }
    }
    Long max = this.getMaximum();
    if (max != null) {
      builder.add("maximum", max.longValue());
      if (this.isExclusiveMaximum()) {
        builder.add("maximumExclusive", "true");
      }
    }
    Long multi = this.getMultipleOf();
    if (multi != null) {
      builder.add("multipleOf", multi.longValue());
    }
  }

  /**
   * Parses a long integer data type with the specified optional name using the
   * specified {@link JsonObject}.
   *
   * @param name The optional name for the type, or <tt>null</tt> if it is
   *             an anonymous type.
   * @param jsonObject The {@link JsonObject} containing the information for
   *                   the data type.
   */
  public static LongDataType parse(String name, JsonObject jsonObject) {
    String typeValue = JsonUtils.getString(jsonObject, "type");
    if (!"integer".equals(typeValue)) {
      throw new IllegalArgumentException(
          "Not a valid definition for a LongDataType: " + typeValue);
    }
    LongDataType longType = new LongDataType(name);
    String format = JsonUtils.getString(jsonObject, "format");
    if (!"int64".equals(format)) {
      throw new IllegalArgumentException(
          "Not a valid format for an LongDataType: " + format);
    }
    Long minimum    = JsonUtils.getLong(jsonObject, "minimum");
    Boolean minExcl = JsonUtils.getBoolean(jsonObject, "minimumExclusive");
    Long maximum    = JsonUtils.getLong(jsonObject, "maximum");
    Boolean maxExcl = JsonUtils.getBoolean(jsonObject, "maximumExclusive");
    Long multi      = JsonUtils.getLong(jsonObject, "multipleOf");

    if (minExcl != null)  longType.setExclusiveMinimum(minExcl);
    if (minimum != null)  longType.setMinimum(minimum);
    if (maxExcl != null)  longType.setExclusiveMaximum(maxExcl);
    if (maximum != null)  longType.setMaximum(maximum);
    if (multi != null)    longType.setMultipleOf(multi);

    return longType;
  }
}

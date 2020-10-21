package com.senzing.api.client.generator.schema;

import com.senzing.util.JsonUtils;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Extends {@link ApiDataType} to describe an integer type and its properties.
 */
public class IntegerDataType extends NumberDataType<Integer> {
  /**
   * Default constructor for constructing an anonymous type.
   */
  public IntegerDataType() {
    super();
  }

  /**
   * Constructs a named type.
   */
  public IntegerDataType(String name) {
    super(name);
  }

  @Override
  public void buildJson(JsonObjectBuilder builder) {
    super.buildJson(builder);
    builder.add("type", "integer");
    builder.add("format", "int32");
    Integer min = this.getMinimum();
    if (min != null) {
      builder.add("minimum", min.intValue());
      if (this.isExclusiveMinimum()) {
        builder.add("minimumExclusive", "true");
      }
    }
    Integer max = this.getMaximum();
    if (max != null) {
      builder.add("maximum", max.intValue());
      if (this.isExclusiveMaximum()) {
        builder.add("maximumExclusive", "true");
      }
    }
    Integer multi = this.getMultipleOf();
    if (multi != null) {
      builder.add("multipleOf", multi.intValue());
    }
  }

  /**
   * Parses an integer data type with the specified optional name using the
   * specified {@link JsonObject}.
   *
   * @param name The optional name for the type, or <tt>null</tt> if it is
   *             an anonymous type.
   * @param jsonObject The {@link JsonObject} containing the information for
   *                   the data type.
   */
  public static IntegerDataType parse(String name, JsonObject jsonObject) {
    String typeValue = JsonUtils.getString(jsonObject, "type");
    if (typeValue != null && !"integer".equals(typeValue)) {
      throw new IllegalArgumentException(
          "Not a valid definition for a IntegerDataType: " + typeValue);
    }
    IntegerDataType intType = new IntegerDataType(name);

    String format = JsonUtils.getString(jsonObject, "format");
    if (format != null && !"int32".equals(format)) {
      throw new IllegalArgumentException(
          "Not a valid format for an IntegerDataType: " + format);
    }
    Integer minimum = JsonUtils.getInteger(jsonObject, "minimum");
    Boolean minExcl = JsonUtils.getBoolean(jsonObject, "minimumExclusive");
    Integer maximum = JsonUtils.getInteger(jsonObject, "maximum");
    Boolean maxExcl = JsonUtils.getBoolean(jsonObject, "maximumExclusive");
    Integer multi   = JsonUtils.getInteger(jsonObject, "multipleOf");

    if (minimum != null)  intType.setMinimum(minimum);
    if (minExcl != null)  intType.setExclusiveMinimum(minExcl);
    if (maximum != null)  intType.setMaximum(maximum);
    if (maxExcl != null)  intType.setExclusiveMaximum(maxExcl);
    if (multi != null)    intType.setMultipleOf(multi);

    return intType;
  }
}

package com.senzing.api.client.generator.schema;

import com.senzing.util.JsonUtils;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Extends {@link ApiDataType} to describe a floating-point number type and
 * its properties.
 */
public class FloatDataType extends NumberDataType<Float> {
  /**
   * Default constructor for constructing an anonymous type.
   */
  public FloatDataType() {
    super();
  }

  /**
   * Constructs a named type.
   */
  public FloatDataType(String name) {
    super(name);
  }

  @Override
  public void buildJson(JsonObjectBuilder builder) {
    super.buildJson(builder);
    builder.add("type", "number");
    builder.add("format", "float");
    Float min = this.getMinimum();
    if (min != null) {
      builder.add("minimum", min.floatValue());
      if (this.isExclusiveMinimum()) {
        builder.add("minimumExclusive", "true");
      }
    }
    Float max = this.getMaximum();
    if (max != null) {
      builder.add("maximum", max.floatValue());
      if (this.isExclusiveMaximum()) {
        builder.add("maximumExclusive", "true");
      }
    }
    Float multi = this.getMultipleOf();
    if (multi != null) {
      builder.add("multipleOf", multi.floatValue());
    }
  }

  /**
   * Parses a single precision floating-point data type with the specified
   * optional name using the specified {@link JsonObject}.
   *
   * @param name The optional name for the type, or <tt>null</tt> if it is
   *             an anonymous type.
   * @param jsonObject The {@link JsonObject} containing the information for
   *                   the data type.
   */
  public static FloatDataType parse(String name, JsonObject jsonObject) {
    String typeValue = JsonUtils.getString(jsonObject, "type");
    if ("number".equals(typeValue)) {
      throw new IllegalArgumentException(
          "Not a valid definition for a FloatDataType: " + typeValue);
    }
    FloatDataType floatType = new FloatDataType(name);
    String format = JsonUtils.getString(jsonObject, "format");
    if (!"float".equals(format)) {
      throw new IllegalArgumentException(
          "Not a valid format for an FloatDataType: " + format);
    }
    Boolean minExcl = JsonUtils.getBoolean(jsonObject, "minimumExclusive");
    Double  minimum = JsonUtils.getDouble(jsonObject, "minimum");
    Boolean maxExcl = JsonUtils.getBoolean(jsonObject, "maximumExclusive");
    Double  maximum = JsonUtils.getDouble(jsonObject, "maximum");
    Double  multi   = JsonUtils.getDouble(jsonObject, "multipleOf");

    if (maxExcl != null)  floatType.setExclusiveMaximum(maxExcl);
    if (maximum != null)  floatType.setMaximum(maximum.floatValue());
    if (minExcl != null)  floatType.setExclusiveMinimum(minExcl);
    if (minimum != null)  floatType.setMinimum(minimum.floatValue());
    if (multi != null)    floatType.setMultipleOf(multi.floatValue());

    return floatType;
  }

}

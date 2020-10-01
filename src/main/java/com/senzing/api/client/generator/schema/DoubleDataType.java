package com.senzing.api.client.generator.schema;

import com.senzing.util.JsonUtils;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Extends {@link ApiDataType} to describe a double-precision floating-point
 * number type and its properties.
 */
public class DoubleDataType extends NumberDataType<Double> {
  /**
   * Default constructor for constructing an anonymous type.
   */
  public DoubleDataType() {
    super();
  }

  /**
   * Constructs a named type.
   */
  public DoubleDataType(String name) {
    super(name);
  }

  @Override
  public void buildJson(JsonObjectBuilder builder) {
    super.buildJson(builder);
    builder.add("type", "number");
    builder.add("format", "double");
    Double min = this.getMinimum();
    if (min != null) {
      builder.add("minimum", min.doubleValue());
      if (this.isExclusiveMinimum()) {
        builder.add("minimumExclusive", "true");
      }
    }
    Double max = this.getMaximum();
    if (max != null) {
      builder.add("maximum", max.doubleValue());
      if (this.isExclusiveMaximum()) {
        builder.add("maximumExclusive", "true");
      }
    }
    Double multi = this.getMultipleOf();
    if (multi != null) {
      builder.add("multipleOf", multi.doubleValue());
    }
  }

  /**
   * Parses a double precision floating-point data type with the specified
   * optional name using the specified {@link JsonObject}.
   *
   * @param name The optional name for the type, or <tt>null</tt> if it is
   *             an anonymous type.
   * @param jsonObject The {@link JsonObject} containing the information for
   *                   the data type.
   */
  public static DoubleDataType parse(String name, JsonObject jsonObject) {
    String typeValue = JsonUtils.getString(jsonObject, "type");
    if ("number".equals(typeValue)) {
      throw new IllegalArgumentException(
          "Not a valid definition for a DoubleDataType: " + typeValue);
    }
    DoubleDataType doubleType = new DoubleDataType(name);
    String format = JsonUtils.getString(jsonObject, "format");
    if (!"double".equals(format)) {
      throw new IllegalArgumentException(
          "Not a valid format for an DoubleDataType: " + format);
    }
    Double  maximum = JsonUtils.getDouble(jsonObject, "maximum");
    Boolean maxExcl = JsonUtils.getBoolean(jsonObject, "maximumExclusive");
    Double  minimum = JsonUtils.getDouble(jsonObject, "minimum");
    Boolean minExcl = JsonUtils.getBoolean(jsonObject, "minimumExclusive");
    Double  multi   = JsonUtils.getDouble(jsonObject, "multipleOf");

    if (maxExcl != null)  doubleType.setExclusiveMaximum(maxExcl);
    if (maximum != null)  doubleType.setMaximum(maximum);
    if (minExcl != null)  doubleType.setExclusiveMinimum(minExcl);
    if (minimum != null)  doubleType.setMinimum(minimum);
    if (multi != null)    doubleType.setMultipleOf(multi);

    return doubleType;
  }
}

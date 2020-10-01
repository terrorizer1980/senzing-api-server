package com.senzing.api.client.generator.schema;

import com.senzing.util.JsonUtils;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Objects;

/**
 * Extends {@link ApiDataType} to represent a base-64 data type.
 */
public class Base64DataType extends ApiDataType {
  /**
   * The minimum string length.
   */
  private Integer minLength;

  /**
   * The maximum string length.
   */
  private Integer maxLength;

  /**
   * Default constructor to construct an anonymous instance.
   */
  public Base64DataType() {
    super();
  }

  /**
   * Constructs a named instances with the specified name.
   *
   * @param name The name for the type.
   */
  public Base64DataType(String name) {
    super(name);
  }

  /**
   * Gets the minimum length of the string (if any).
   *
   * @return The minimum length of the string or <tt>null</tt> if none.
   */
  public Integer getMinimumLength() {
    return minLength;
  }

  /**
   * Sets the minimum length of the string (if any).
   *
   * @param minLength The minimum length of the string or <tt>null</tt> if none.
   */
  public void setMinimumLength(Integer minLength) {
    this.minLength = minLength;
  }

  /**
   * Gets the maximum length of the string (if any).
   *
   * @return The maximum length of the string or <tt>null</tt> if none.
   */
  public Integer getMaximumLength() {
    return maxLength;
  }

  /**
   * Sets the maximum length of the string (if any).
   *
   * @param maxLength The maximum length of the string or <tt>null</tt> if none.
   */
  public void setMaximumLength(Integer maxLength) {
    this.maxLength = maxLength;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    if (!super.equals(object)) return false;
    Base64DataType that = (Base64DataType) object;
    return Objects.equals(this.getMinimumLength(), that.getMinimumLength()) &&
        Objects.equals(this.getMaximumLength(), that.getMaximumLength());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), 
                        this.getMinimumLength(),
                        this.getMaximumLength());
  }

  @Override
  public void buildJson(JsonObjectBuilder builder) {
    super.buildJson(builder);
    builder.add("type", "string");
    builder.add("format", "byte");
    if (this.getMinimumLength() != null) {
      builder.add("minimumLength", this.getMinimumLength());
    }
    if (this.getMaximumLength() != null) {
      builder.add("maximumLength", this.getMaximumLength());
    }
  }

  /**
   * Parses a base-64-encoded binary data type with the specified optional name
   * using the specified {@link JsonObject}.
   *
   * @param name The optional name for the type, or <tt>null</tt> if it is
   *             an anonymous type.
   * @param jsonObject The {@link JsonObject} containing the information for
   *                   the data type.
   */
  public static Base64DataType parse(String name, JsonObject jsonObject) {
    String typeValue = JsonUtils.getString(jsonObject, "type");
    if (!"string".equals(typeValue)) {
      throw new IllegalArgumentException(
          "Not a valid definition for a Base64DataType: " + typeValue);
    }
    Base64DataType byteType = new Base64DataType(name);
    String format = JsonUtils.getString(jsonObject, "format");
    if (!"byte".equals(format)) {
      throw new IllegalArgumentException(
          "Not a valid format for an Base64DataType: " + format);
    }

    return byteType;
  }

}

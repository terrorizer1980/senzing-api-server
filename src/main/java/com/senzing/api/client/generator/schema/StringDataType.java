package com.senzing.api.client.generator.schema;

import com.senzing.util.JsonUtils;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Objects;

/**
 * Extends {@link ApiDataType} to represent a string data type.
 */
public class StringDataType extends ApiDataType {
  /**
   * The format for the string (if any).
   */
  private String format;

  /**
   * The minimum string length.
   */
  private Integer minLength;

  /**
   * The maximum string length.
   */
  private Integer maxLength;

  /**
   * The pattern to match for the string (if any).
   */
  private String pattern;
  
  /**
   * Default constructor to construct an anonymous instance.
   */
  public StringDataType() {
    super();
  }

  /**
   * Constructs a named instances with the specified name.
   * 
   * @param name The name for the type.
   */
  public StringDataType(String name) {
    super(name);
  }

  /**
   * Gets the format for the string.
   * 
   * @return The format for the string.
   */
  public String getFormat() {
    return format;
  }

  /**
   * Sets the format for the string.
   * 
   * @param format The format for the string.
   */
  public void setFormat(String format) {
    this.format = format;
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

  /**
   * Gets the pattern for the string (if any).
   *
   * @return The pattern for the string or <tt>null</tt> if no pattern.
   */
  public String getPattern() {
    return pattern;
  }

  /**
   * Sets the pattern for the string (if any).
   *
   * @param pattern The pattern for the string or <tt>null</tt> if no pattern.
   */
  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    if (!super.equals(object)) return false;
    StringDataType that = (StringDataType) object;
    return Objects.equals(this.getFormat(), that.getFormat()) &&
        Objects.equals(this.getMinimumLength(), that.getMinimumLength()) &&
        Objects.equals(this.getMaximumLength(), that.getMaximumLength()) &&
        Objects.equals(this.getPattern(), that.getPattern());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), 
                        this.getFormat(),
                        this.getMinimumLength(),
                        this.getMaximumLength(),
                        this.getPattern());
  }

  @Override
  public void buildJson(JsonObjectBuilder builder) {
    super.buildJson(builder);
    builder.add("type", "string");
    if (this.getFormat() != null) {
      builder.add("format", this.getFormat());
    }
    if (this.getPattern() != null) {
      builder.add("pattern", this.getPattern());
    }
    if (this.getMinimumLength() != null) {
      builder.add("minimumLength", this.getMinimumLength());
    }
    if (this.getMaximumLength() != null) {
      builder.add("maximumLength", this.getMaximumLength());
    }
  }

  /**
   * Parses a string type with the specified optional name using the
   * specified {@link JsonObject}.
   *
   * @param name The optional name for the type, or <tt>null</tt> if it is
   *             an anonymous type.
   * @param jsonObject The {@link JsonObject} containing the information for
   *                   the data type.
   */
  public static StringDataType parse(String name, JsonObject jsonObject) {
    StringDataType stringType = new StringDataType(name);

    String  format    = JsonUtils.getString(jsonObject, "format");
    String  pattern   = JsonUtils.getString(jsonObject, "pattern");
    Integer minLength = JsonUtils.getInteger(jsonObject, "minimumLength");
    Integer maxLength = JsonUtils.getInteger(jsonObject, "maximumLength");

    if (format != null)     stringType.setFormat(format);
    if (pattern != null)    stringType.setPattern(pattern);
    if (minLength != null)  stringType.setMinimumLength(minLength);
    if (maxLength != null)  stringType.setMaximumLength(maxLength);

    return stringType;
  }

}

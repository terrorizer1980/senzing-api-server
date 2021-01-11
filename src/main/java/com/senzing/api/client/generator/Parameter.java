package com.senzing.api.client.generator;

import com.senzing.api.client.generator.schema.ApiDataType;
import com.senzing.util.JsonUtils;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Objects;

import static java.lang.Boolean.TRUE;

/**
 * Describes a parameter to a {@link RestOperation}.
 */
public abstract class Parameter implements SpecConstruct {
  /**
   * The referance name for the parameter (if any).
   */
  private String refName;

  /**
   * The name of the parameter.
   */
  private String name = null;

  /**
   * The description for the parameter.
   */
  private String description = null;

  /**
   * The {@link ApiDataType} describing the data type for how to interpret the
   * {@link String} in the query parameters.
   */
  private ApiDataType dataType = null;

  /**
   * Default constructor.
   */
  public Parameter() {
    // do nothing
  }

  /**
   * Gets the reference name if this parameter is defined globally in the
   * <tt>"components/parameters</tt> segment.  This returns <tt>null</tt> if
   * the parameter is defined inline.
   *
   * @return The reference name if this parameter is defined globally in the
   *         <tt>"components/parameters</tt> segment, or <tt>null</tt> if the
   *         parameter is defined inline.
   */
  public String getRefName() {
    return this.refName;
  }

  /**
   * Sets the reference name name for this parameter if it is defined globally
   * in the <tt>"components/parameters</tt> segment.  Leave this as its default
   * <tt>null</tt> value if the parameter is defined inline.
   *
   * @param refName The reference name for this parameter by which it is defined
   *                globally in the <tt>"components/parameters</tt> segment, or
   *                <tt>null</tt> if the parameter is defined inline.
   */
  public void setRefName(String refName) {
    this.refName = refName;
  }

  /**
   * Gets the name of the parameter.
   *
   * @return The name of the parameter.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name of the parameter.
   *
   * @param name The name of the parameter.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the description of the parameter.
   *
   * @return The description of the parameter.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Sets the description of the parameter.
   *
   * @param description The description of the parameter.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets the data type for the parameter.
   *
   * @return The data type for the parameter.
   */
  public ApiDataType getDataType() {
    return this.dataType;
  }

  /**
   * Sets the data type for the parameter.
   *
   * @param dataType The data type for the parameter.
   */
  public void setDataType(ApiDataType dataType) {
    this.dataType = dataType;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    QueryParameter that = (QueryParameter) object;
    return Objects.equals(this.getName(), that.getName()) &&
        Objects.equals(this.getDescription(), that.getDescription()) &&
        Objects.equals(this.getDataType(), that.getDataType());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getName(),
                        this.getDescription(),
                        this.getDataType());
  }

  /**
   * Builds the JSON representation for this object.
   *
   * @param builder The {@link JsonObjectBuilder} for creating the JSON
   *                representation of this object.
   */
  public abstract void buildJson(JsonObjectBuilder builder);

  /**
   * Converts this object to a {@link JsonObject}.
   *
   * @return This object represented as a {@link JsonObject}.
   */
  public JsonObject toJson() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    this.buildJson(builder);
    return builder.build();
  }

  /**
   * Return a JSON {@link String} describing this instance.
   *
   * @return A JSON {@link String} describing this instance.
   */
  public String toString() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    this.buildJson(builder);
    return JsonUtils.toJsonText(builder, true);
  }
}

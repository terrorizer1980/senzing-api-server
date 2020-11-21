package com.senzing.api.client.generator;

import com.senzing.api.client.generator.schema.ApiDataType;
import com.senzing.util.JsonUtils;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Objects;

/**
 * Describes a query parameter for a rest operation.
 */
public class QueryParameter extends Parameter {
  /**
   * Whether or not the parameter is required.
   */
  private boolean required = false;

  /**
   * The default value for the query parameter.
   */
  private String defaultValue = null;

  /**
   * Default constructor.
   */
  public QueryParameter() {
    // do nothing
  }

  /**
   * Checks whether or not the parameter is required.
   *
   * @return <tt>true</tt> if the parameter is required, otherwise
   *         <tt>false</tt>.
   */
  public boolean isRequired() {
    return this.required;
  }

  /**
   * Sets whether or not the parameter is required.
   *
   * @param required <tt>true</tt> if the parameter is required, otherwise
   *                 <tt>false</tt>.
   */
  public void setRequired(boolean required) {
    this.required = required;
  }

  /**
   * Gets the default value for the parameter encoded as a string
   *
   * @return The default value for the parameter encoded as a string.
   */
  public String getDefaultValue() {
    return this.defaultValue;
  }

  /**
   * Sets the default value for the parameter encoded as a string
   *
   * @param defaultValue The default value for the parameter encoded as a
   *                     string.
   */
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    QueryParameter that = (QueryParameter) object;
    return super.equals(object)
        && this.isRequired() == that.isRequired()
        && Objects.equals(this.getDefaultValue(), that.getDefaultValue());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(),
                        this.isRequired(),
                        this.getDefaultValue());
  }

  /**
   * Builds the JSON representation for this object.
   *
   * @param builder The {@link JsonObjectBuilder} for creating the JSON
   *                representation of this object.
   */
  public void buildJson(JsonObjectBuilder builder) {
    builder.add("in", "query");
    builder.add("name", this.getName());
    if (this.getDescription() != null) builder.add("description", this.getDescription());
    builder.add("required", this.isRequired());
    ApiDataType dataType = this.getDataType();
    if (dataType != null) {
      JsonObjectBuilder job = Json.createObjectBuilder();
      dataType.buildJson(job);
      if (this.getDefaultValue() != null) {
        job.add("default", this.getDefaultValue());
      }
      builder.add("schema", job);
    }
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

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
public class QueryParameter {
  /**
   * The name of the parameter.
   */
  private String name = null;

  /**
   * The description for the parameter.
   */
  private String description = null;

  /**
   * Whether or not the parameter is required.
   */
  private boolean required = false;

  /**
   * The {@link ApiDataType} describing the data type for how to interpret the
   * {@link String} in the query parameters.
   */
  private ApiDataType dataType = null;

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
   * Gets the name of the parameter.
   *
   * @return The name of the parameter.
   */
  public String getName() {
    return name;
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
    return description;
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
   * Checks whether or not the parameter is required.
   *
   * @return <tt>true</tt> if the parameter is required, otherwise
   *         <tt>false</tt>.
   */
  public boolean isRequired() {
    return required;
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
   * Gets the data type for the parameter.
   *
   * @return The data type for the parameter.
   */
  public ApiDataType getDataType() {
    return dataType;
  }

  /**
   * Sets the data type for the parameter.
   *
   * @param dataType The data type for the parameter.
   */
  public void setDataType(ApiDataType dataType) {
    this.dataType = dataType;
  }

  /**
   * Gets the default value for the parameter encoded as a string
   *
   * @return The default value for the parameter encoded as a string.
   */
  public String getDefaultValue() {
    return defaultValue;
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
    return this.isRequired() == that.isRequired() &&
        Objects.equals(this.getName(), that.getName()) &&
        Objects.equals(this.getDescription(), that.getDescription()) &&
        Objects.equals(this.getDataType(), that.getDataType()) &&
        Objects.equals(this.getDefaultValue(), that.getDefaultValue());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getName(),
                        this.getDescription(),
                        this.isRequired(),
                        this.getDataType(),
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

  /**
   * Interprets the specified {@link JsonObject} as a {@link QueryParameter}.
   *
   * @param jsonObject The {@link JsonObject} to interpret.
   *
   * @return The {@link QueryParameter} that was created.
   */
  public static QueryParameter parse(JsonObject jsonObject) {
    if (!"query".equals(JsonUtils.getString(jsonObject, "in"))) {
      throw new IllegalArgumentException(
          "The specified JSON object does not describe a query parameter: "
          + "in = " + JsonUtils.getString(jsonObject, "in"));
    }
    String      name      = JsonUtils.getString(jsonObject, "name");
    String      desc      = JsonUtils.getString(jsonObject, "description");
    JsonObject  typeObj   = JsonUtils.getJsonObject(jsonObject, "schema");
    Boolean     required  = JsonUtils.getBoolean(jsonObject, "required");
    ApiDataType dataType  = ApiSpecification.parseDataType(typeObj, jsonObject);
    String      defVal    = JsonUtils.getString(typeObj, "default");

    QueryParameter param = new QueryParameter();
    param.setName(name);
    param.setDescription(desc);
    param.setRequired(required);
    param.setDefaultValue(defVal);
    param.setDataType(dataType);

    return param;
  }
}

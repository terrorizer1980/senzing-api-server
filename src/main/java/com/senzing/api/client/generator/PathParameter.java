package com.senzing.api.client.generator;

import com.senzing.api.client.generator.schema.ApiDataType;
import com.senzing.util.JsonUtils;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Objects;

import static java.lang.Boolean.TRUE;

/**
 * Describes a path parameter for a rest operation.
 */
public class PathParameter {
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
  public PathParameter() {
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
  public void buildJson(JsonObjectBuilder builder) {
    builder.add("in", "path");
    builder.add("name", this.getName());
    if (this.getDescription() != null) builder.add("description", this.getDescription());
    builder.add("required", true);
    ApiDataType dataType = this.getDataType();
    if (dataType != null) {
      JsonObjectBuilder job = Json.createObjectBuilder();
      dataType.buildJson(job);
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
   * Interprets the specified {@link JsonObject} as a {@link PathParameter}.
   *
   * @param jsonObject The {@link JsonObject} to interpret.
   *
   * @return The {@link PathParameter} that was created.
   */
  public static PathParameter parse(JsonObject jsonObject) {
    if (!"path".equals(JsonUtils.getString(jsonObject, "in"))) {
      throw new IllegalArgumentException(
          "The specified JSON object does not describe a path parameter: "
              + "in = " + JsonUtils.getString(jsonObject, "in"));
    }
    if (!TRUE.equals(JsonUtils.getBoolean(jsonObject, "required"))) {
      throw new IllegalArgumentException(
          "The specified JSON object does not describe a path parameter: "
              + "required = " + JsonUtils.getString(jsonObject, "required"));
    }

    String      name      = JsonUtils.getString(jsonObject, "name");
    String      desc      = JsonUtils.getString(jsonObject, "description");
    JsonObject  typeObj   = JsonUtils.getJsonObject(jsonObject, "schema");
    ApiDataType dataType  = ApiSpecification.parseDataType(typeObj, jsonObject);

    PathParameter param = new PathParameter();
    param.setName(name);
    param.setDescription(desc);
    param.setDataType(dataType);

    return param;
  }

}

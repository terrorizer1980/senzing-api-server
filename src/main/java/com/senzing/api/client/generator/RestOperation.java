package com.senzing.api.client.generator;

import com.senzing.api.client.generator.schema.ApiDataType;
import com.senzing.api.model.SzHttpMethod;
import com.senzing.util.JsonUtils;

import javax.json.*;
import java.util.*;

/**
 * Describes a REST operation found in the API specification.
 */
public class RestOperation {
  /**
   * The HTTP method.
   */
  private SzHttpMethod httpMethod;

  /**
   * The path for the operation.
   */
  private String path;

  /**
   * The summary for the method.
   */
  private String summary;

  /**
   * The tags associated with the operation.
   */
  private Set<String> tags = new LinkedHashSet<>();

  /**
   * The operation ID uniquely identifying the operation.
   */
  private String operationId;

  /**
   * The type for the response.
   */
  private ApiDataType responseType;

  /**
   * The {@link Map} of {@link String} parameter names to {@link PathParameter}
   * values.
   */
  private Map<String, PathParameter> pathParameters = new LinkedHashMap<>();

  /**
   * The {@link Map} of {@link String} parameter names to {@link QueryParameter}
   * values.
   */
  private Map<String, QueryParameter> queryParameters = new LinkedHashMap<>();

  /**
   * Default constructor.
   */
  public RestOperation() {
    // do nothing
  }

  /**
   * Gets the HTTP method for the operation.
   *
   * @return The HTTP method for the operation.
   */
  public SzHttpMethod getHttpMethod() {
    return this.httpMethod;
  }

  /**
   * Sets the HTTP method for the operation.
   *
   * @param httpMethod The HTTP method for the operation.
   */
  public void setHttpMethod(SzHttpMethod httpMethod) {
    this.httpMethod = httpMethod;
  }

  /**
   * Gets the path for the operation.
   *
   * @return The path for the operation.
   */
  public String getPath() {
    return this.path;
  }

  /**
   * Sets the path for the operation.
   *
   * @param path The path for the operation.
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Gets the summary for the operation.
   *
   * @return The summary for the operation.
   */
  public String getSummary() {
    return this.summary;
  }

  /**
   * Sets the summary for the operation.
   *
   * @param summary The summary for the operation.
   */
  public void setSummary(String summary) {
    this.summary = summary;
  }

  /**
   * Gets the <b>unmodifiable</b> {@link Set} of tags.
   *
   * @return The <b>unmodifiable</b> {@link Set} of tags
   */
  public Set<String> getTags() {
    return Collections.unmodifiableSet(this.tags);
  }

  /**
   * Adds the specified tag to the set of tags for this operation.
   *
   * @param tag The tag to add to the set of tags for this operation.
   */
  public void addTag(String tag) {
    this.tags.add(tag);
  }

  /**
   * Removes the specified tag from the set of tags for this operation.
   *
   * @param tag The tag to remove from the set of tags for this operation.
   */
  public void removeTag(String tag) {
    this.tags.remove(tag);
  }

  /**
   * Clears the set of all tags for this operation.
   *
   */
  public void clearTags() {
    this.tags.clear();
  }

  /**
   * Gets the operation ID for this operation.
   *
   * @return The operation ID for this operation.
   */
  public String getOperationId() {
    return operationId;
  }

  /**
   * Sets the operation ID for this operation.
   *
   * @param operationId The operation ID for this operation.
   */
  public void setOperationId(String operationId) {
    this.operationId = operationId;
  }

  /**
   * Gets the {@link ApiDataType} for the 200/OK response.
   *
   * @return Gets the {@link ApiDataType} for the 200/OK response.
   */
  public ApiDataType getResponseType() {
    return responseType;
  }

  /**
   * Sets the {@link ApiDataType} for the 200/OK response.
   *
   * @param responseType Gets the {@link ApiDataType} for the 200/OK response.
   */
  public void setResponseType(ApiDataType responseType) {
    this.responseType = responseType;
  }

  /**
   * Adds the specified {@link PathParameter} to the set of path parameters.
   *
   * @param parameter The {@link PathParameter} to add.
   */
  public void addPathParameter(PathParameter parameter) {
    this.pathParameters.put(parameter.getName(), parameter);
  }

  /**
   * Gets the {@link PathParameter} with the specified name.  This returns
   * <tt>null</tt> if there is no path parameter with the specified name.
   *
   * @param name The parameter name for which the {@link PathParameter} is
   *             being requested.
   *
   * @return The {@link PathParameter} object describing the path parameter
   *         with the specified name, or <tt>null</tt> if no path parameter with
   *         the specified name.
   */
  public PathParameter getPathParameter(String name) {
    return this.pathParameters.get(name);
  }

  /**
   * Gets the <b>unmodifiable</b> {@link Collection} of all {@link
   * PathParameter} instances for this operation.
   *
   * @return The <b>unmodifiable</b> {@link Collection} of all {@link
   *         PathParameter} instances for this operation.
   */
  public Collection<PathParameter> getPathParameters() {
    return Collections.unmodifiableCollection(this.pathParameters.values());
  }

  /**
   * Removes the {@link PathParameter} with the specified name.
   *
   * @param name The parameter name for the {@link PathParameter} which is
   *             being removed.
   */
  public void removePathParameter(String name) {
    this.pathParameters.remove(name);
  }

  /**
   * Removes all path parameters associated with this operation.
   */
  public void clearPathParameters() {
    this.pathParameters.clear();
  }

  /**
   * Adds the specified {@link QueryParameter} to the set of query parameters.
   *
   * @param parameter The {@link QueryParameter} to add to the set of query
   *                  parameters.
   */
  public void addQueryParameter(QueryParameter parameter) {
    this.queryParameters.put(parameter.getName(), parameter);
  }

  /**
   * Gets the query parameter with the specified name.  This returns
   * <tt>null</tt> if there are no query parameters with the specified name.
   *
   * @param name The name for the query parameter.
   *
   * @return
   */
  public QueryParameter getQueryParameter(String name) {
    return this.queryParameters.get(name);
  }

  /**
   * Gets the <b>unmodifiable</b> {@link Collection} of {@link QueryParameter}
   * instances describing the query parameters for the operation.
   *
   * @return The <b>unmodifiable</b> {@link Collection} of {@link
   *         QueryParameter} instances describing the query parameters for
   *         the operation.
   */
  public Collection<QueryParameter> getQueryParameters() {
    return Collections.unmodifiableCollection(this.queryParameters.values());
  }

  /**
   * Removes the query parameter with the specified name from this operation.
   *
   * @param name The name for the query parameter to remove.
   */
  public void removeQueryParameter(String name) {
    this.queryParameters.remove(name);
  }

  /**
   * Removes all the query parameters from this operation.
   */
  public void clearQueryParameters() {
    this.queryParameters.clear();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    RestOperation that = (RestOperation) object;
    return getHttpMethod() == that.getHttpMethod() &&
        Objects.equals(getPath(), that.getPath()) &&
        Objects.equals(getSummary(), that.getSummary()) &&
        Objects.equals(getTags(), that.getTags()) &&
        Objects.equals(getOperationId(), that.getOperationId()) &&
        Objects.equals(getResponseType(), that.getResponseType()) &&
        Objects.equals(getPathParameters(), that.getPathParameters()) &&
        Objects.equals(getQueryParameters(), that.getQueryParameters());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getHttpMethod(), getPath(), getSummary(), getTags(), getOperationId(), getResponseType(), getPathParameters(), getQueryParameters());
  }

  /**
   * Builds the JSON representation for this object.
   *
   * @param builder The {@link JsonObjectBuilder} for creating the JSON
   *                representation of this object.
   */
  public void buildJson(JsonObjectBuilder builder) {
    JsonObjectBuilder job = Json.createObjectBuilder();
    if (this.getTags().size() > 0) {
      JsonArrayBuilder jab = Json.createArrayBuilder();
      for (String tag: this.getTags()) {
        jab.add(tag);
      }
      job.add("tags", jab);
    }
    if (this.getSummary() != null) job.add("summary", this.getSummary());
    if (this.getOperationId() != null) {
      job.add("operationId", this.getOperationId());
    }

    // get the parameter count
    int paramCount = this.getPathParameters().size()
        + this.getQueryParameters().size();

    // check if we have any parameters
    if (paramCount > 0) {
      // create the array of parameters
      JsonArrayBuilder jab = Json.createArrayBuilder();

      // add the path parameters
      for (PathParameter param : this.getPathParameters()) {
        JsonObjectBuilder paramBuilder = Json.createObjectBuilder();
        param.buildJson(paramBuilder);
        jab.add(paramBuilder);
      }

      // add the query parameters
      for (QueryParameter param: this.getQueryParameters()) {
        JsonObjectBuilder paramBuilder = Json.createObjectBuilder();
        param.buildJson(paramBuilder);
        jab.add(paramBuilder);
      }

      // add the array builder for the parameters
      job.add("parameters", jab);
    }

    ApiDataType       responseType    = this.getResponseType();
    JsonObjectBuilder respBuilder     = Json.createObjectBuilder();
    JsonObjectBuilder codeBuilder     = Json.createObjectBuilder();
    JsonObjectBuilder contentBuilder  = Json.createObjectBuilder();
    JsonObjectBuilder mimeBuilder     = Json.createObjectBuilder();
    JsonObjectBuilder schemaBuilder   = Json.createObjectBuilder();

    responseType.buildJson(schemaBuilder);
    mimeBuilder.add("schema", schemaBuilder);
    contentBuilder.add("application/json; charset=UTF-8", mimeBuilder);
    contentBuilder.add("application/json", mimeBuilder);
    contentBuilder.add("default", mimeBuilder);

    codeBuilder.add("description", "Successful response");
    codeBuilder.add("content", contentBuilder);

    respBuilder.add("200", codeBuilder);

    job.add("responses", respBuilder);

    builder.add(this.getHttpMethod().toString().toLowerCase(), job);
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
  public static Map<SzHttpMethod, RestOperation> parse(JsonObject jsonObject) {
    Map<SzHttpMethod, RestOperation> result = new LinkedHashMap<>();
    jsonObject.forEach((methodKey, jsonValue) -> {
      SzHttpMethod method = SzHttpMethod.valueOf(methodKey.toUpperCase());
      JsonObject opObject = (JsonObject) jsonValue;

      JsonArray   jsonArr = JsonUtils.getJsonArray(opObject, "tags");
      String      summary = JsonUtils.getString(opObject, "summary");
      String      opId    = JsonUtils.getString(opObject, "operationId");
      JsonArray   parmArr = JsonUtils.getJsonArray(opObject, "parameters");
      JsonObject  respObj = JsonUtils.getJsonObject(opObject, "responses");

      RestOperation restOp = new RestOperation();
      restOp.setHttpMethod(method);
      if (jsonArr != null) {
        for (JsonString tag : jsonArr.getValuesAs(JsonString.class)) {
          restOp.addTag(tag.getString());
        }
      }
      if (summary != null && summary.trim().length() > 0) {
        restOp.setSummary(summary.trim());
      }
      if (opId != null && opId.trim().length() > 0) {
        restOp.setOperationId(opId);
      }
      if (parmArr != null) {
        for (JsonObject paramObj : parmArr.getValuesAs(JsonObject.class)) {
          String paramType = JsonUtils.getString(paramObj, "in");
          switch (paramType) {
            case "path":
              restOp.addPathParameter(PathParameter.parse(paramObj));
              break;
            case "query":
              restOp.addQueryParameter(QueryParameter.parse(paramObj));
              break;
            default:
              throw new IllegalArgumentException(
                  "Parameter not in a recognized scope: " + paramType);
          }
        }
      }

      JsonObject contentObj
          = respObj.getJsonObject("200").getJsonObject("content");
      String[] mimeTypes = {
          "application/json; charset=UTF-8",
          "application/json",
          "default"
      };
      JsonObject typeObject = null;
      for (String mimeType : mimeTypes) {
        typeObject = JsonUtils.getJsonObject(contentObj, mimeType);
        if (typeObject != null) break;
      }

      ApiDataType responseType
          = ApiSpecification.parseDataType(typeObject, jsonObject);
      restOp.setResponseType(responseType);

      result.put(method, restOp);
    });
    return result;
  }

}

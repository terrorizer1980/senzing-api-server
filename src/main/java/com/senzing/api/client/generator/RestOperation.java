package com.senzing.api.client.generator;

import com.senzing.api.client.generator.schema.ApiDataType;
import com.senzing.api.model.SzHttpMethod;
import com.senzing.util.JsonUtils;

import javax.json.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * Describes a REST operation found in the API specification.
 */
public class RestOperation implements SpecConstruct {
  /**
   * The prefix root for ref parameters.
   */
  private static final String REF_PARAMS_PREFIX = "#/components/parameters/";

  /**
   * The HTTP method.
   */
  private SzHttpMethod httpMethod = null;

  /**
   * The path for the operation.
   */
  private String path = null;

  /**
   * The summary for the method.
   */
  private String summary = null;

  /**
   * The tags associated with the operation.
   */
  private Set<String> tags = new LinkedHashSet<>();

  /**
   * The operation ID uniquely identifying the operation.
   */
  private String operationId = null;

  /**
   * The type for the response.
   */
  private ApiDataType responseType = null;

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
   * The {@link RequestBody} if any.  This is <tt>null</tt> if no request body.
   */
  private RequestBody requestBody = null;

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

  /**
   * Gets the {@link RequestBody} describing the request body for the
   * operation.  This returns <tt>null</tt> if the operation has no request
   * body.
   *
   * @return The {@link RequestBody} describing the request body for the
   *         operation, or <tt>null</tt> if the operation has no request body.
   */
  public RequestBody getRequestBody() {
    return this.requestBody;
  }

  /**
   * Sets the {@link RequestBody} describing the request body for the
   * operation.  Set this to <tt>null</tt> if the operation has no request
   * body.
   *
   * @param requestBody The {@link RequestBody} describing the request body for
   *                    the operation, or <tt>null</tt> if the operation has
   *                    no request body.
   */
  public void setRequestBody(RequestBody requestBody) {
    this.requestBody = requestBody;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    RestOperation that = (RestOperation) object;
    return getHttpMethod() == that.getHttpMethod() &&
        Objects.equals(this.getPath(), that.getPath()) &&
        Objects.equals(this.getSummary(), that.getSummary()) &&
        Objects.equals(this.getTags(), that.getTags()) &&
        Objects.equals(this.getOperationId(), that.getOperationId()) &&
        Objects.equals(this.getResponseType(), that.getResponseType()) &&
        Objects.equals(this.getPathParameters(), that.getPathParameters()) &&
        Objects.equals(this.getQueryParameters(), that.getQueryParameters()) &&
        Objects.equals(this.getRequestBody(), that.getRequestBody());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getHttpMethod(),
                        this.getPath(),
                        this.getSummary(),
                        this.getTags(),
                        this.getOperationId(),
                        this.getResponseType(),
                        this.getPathParameters(),
                        this.getQueryParameters(),
                        this.getRequestBody());
  }

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
        if (param.getRefName() != null) {
          paramBuilder.add("$ref", REF_PARAMS_PREFIX + param.getRefName());
        } else {
          param.buildJson(paramBuilder);
        }
        jab.add(paramBuilder);
      }

      // add the query parameters
      for (QueryParameter param: this.getQueryParameters()) {
        JsonObjectBuilder paramBuilder = Json.createObjectBuilder();
        if (param.getRefName() != null) {
          paramBuilder.add("$ref", REF_PARAMS_PREFIX + param.getRefName());
        } else{
          param.buildJson(paramBuilder);
        }
        jab.add(paramBuilder);
      }

      // add the array builder for the parameters
      job.add("parameters", jab);
    }

    // handle the request body
    RequestBody reqBody = this.getRequestBody();
    if (reqBody != null) {
      JsonObjectBuilder bodyBuilder = Json.createObjectBuilder();
      reqBody.buildJson(bodyBuilder);
      job.add("requestBody", bodyBuilder);
    }

    // handle the response type
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
   * @param path The path for the rest operations.
   *
   * @param jsonObject The {@link JsonObject} to interpret.
   *
   * @param paramRefs The {@link JsonObject} describing the root of the
   *                  parameter references.
   *
   * @return The {@link QueryParameter} that was created.
   */
  public static Map<SzHttpMethod, RestOperation> parse(String     path,
                                                       JsonObject jsonObject,
                                                       JsonObject paramRefs)
  {
    Map<SzHttpMethod, RestOperation> result = new LinkedHashMap<>();
    jsonObject.forEach((methodKey, jsonValue) -> {
      SzHttpMethod method = SzHttpMethod.valueOf(methodKey.toUpperCase());
      JsonObject opObject = (JsonObject) jsonValue;

      JsonArray   jsonArr = JsonUtils.getJsonArray(opObject, "tags");
      String      summary = JsonUtils.getString(opObject, "summary");
      String      opId    = JsonUtils.getString(opObject, "operationId");
      JsonArray   parmArr = JsonUtils.getJsonArray(opObject, "parameters");
      JsonObject  respObj = JsonUtils.getJsonObject(opObject, "responses");
      JsonObject  reqBody = JsonUtils.getJsonObject(opObject, "requestBody");

      RestOperation restOp = new RestOperation();
      restOp.setHttpMethod(method);
      restOp.setPath(path);
      if (jsonArr != null) {
        for (JsonString tag : jsonArr.getValuesAs(JsonString.class)) {
          restOp.addTag(tag.getString().trim());
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
          Parameter parameter = ApiSpecification.parseParameter(opId,
                                                                paramObj,
                                                                paramRefs);
          if (parameter instanceof PathParameter) {
            restOp.addPathParameter((PathParameter) parameter);
          } else if (parameter instanceof QueryParameter) {
            restOp.addQueryParameter((QueryParameter) parameter);
          } else {
            throw new IllegalStateException(
                "Unhandled parameter type: " + parameter.getClass().getName());
          }
        }
      }

      // handle the request body
      if (reqBody != null) {
        boolean sse = (restOp.getQueryParameters().stream().anyMatch(param ->
          param.getName().equals("progressPeriod")
        ));
        String  bodyDesc = JsonUtils.getString(reqBody, "description");
        boolean required = JsonUtils.getBoolean(reqBody, "required", false);

        RequestBody requestBody = new RequestBody();
        requestBody.setDescription(bodyDesc);
        requestBody.setRequired(required);
        JsonObject bodyContent = JsonUtils.getJsonObject(reqBody, "content");
        if (!sse) {
          // get the mime type segment
          JsonObject mimeObject = JsonUtils.getJsonObject(
              bodyContent, "application/json; charset=UTF-8");
          if (mimeObject == null) {
            mimeObject = JsonUtils.getJsonObject(bodyContent,
                                                 "application/json");
          }

          // check for problems
          if (mimeObject == null) {
            throw new IllegalStateException(
                "No application/json MIME type found for the request body: "
                + "path=[ " + path + " ], operationId=[ " + opId + " ]");
          }
          // now get the schema under the mime type
          JsonObject bodySchema = JsonUtils.getJsonObject(mimeObject, "schema");

          String bodyName = opId.substring(0, 1).toUpperCase()
              + opId.substring(1) + "Body";
          if (!opId.startsWith("Sz")) bodyName = "Sz" + bodyName;

          ApiDataType bodyType = ApiSpecification.parseDataType(bodyName,
                                                                bodySchema,
                                                                bodyContent);

          requestBody.setBodyType(bodyType);

        } else {
          Set<MediaType> mediaTypes = new LinkedHashSet<>();
          for (String mediaType: bodyContent.keySet()) {
            mediaTypes.add(MediaType.valueOf(mediaType));
          }
          requestBody.setBlobMediaTypes(mediaTypes);
        }

        restOp.setRequestBody(requestBody);
      }

      // handle the responses
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
    return Collections.unmodifiableMap(result);
  }

}

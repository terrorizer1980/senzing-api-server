package com.senzing.api.client.generator;

import com.senzing.api.client.generator.schema.*;
import com.senzing.api.model.SzHttpMethod;
import com.senzing.util.JsonUtils;

import javax.json.*;
import java.util.*;

/**
 * Describes an API specification from OAS specification file.
 */
public class ApiSpecification {
  /**
   * The only allowed properties for an "any type" that might have additional
   * description or allow null values.
   */
  private static final Set<String> ANY_TYPE_PROPS
      = Set.of("nullable", "description");

  /**
   * The list of {@link ApiDataType} instances describing the named types in
   * the schema.
   */
  private Map<String, ApiDataType> schemaTypes;

  /**
   * The {@link List} of {@link RestOperation} instances.
   */
  private List<RestOperation> operations;

  /**
   * The {@link Map} of {@link String} paths to {@link Map} values of {@link
   * SzHttpMethod} keys to {@link RestOperation} values.
   */
  private Map<String, Map<SzHttpMethod, RestOperation>> operationsByPath;

  /**
   * The {@link Map} of {@link String} tags to {@link List} values of {@link
   * RestOperation} instances.
   */
  private Map<String, List<RestOperation>> operationsByTag;

  /**
   * The {@link Map} of {@link String} reference names to {@link PathParameter}
   * instances for the path parameters that are referenced.
   */
  private Map<String, PathParameter> refPathParams;

  /**
   * The {@link Map} of {@link String} reference names to {@link QueryParameter}
   * instances for the query parameters that are referenced.
   */
  private Map<String, QueryParameter> refQueryParams;

  /**
   * The thread-local name of the current named schema type being parsed (which
   * may be null).
   */
  private static final ThreadLocal<String> currentSchemaTypeName
      = new ThreadLocal<>();

  /**
   * Default constructor.
   */
  public ApiSpecification() {
    this.schemaTypes      = new LinkedHashMap<>();
    this.operationsByPath = new LinkedHashMap<>();
    this.operationsByTag  = new LinkedHashMap<>();
    this.operations       = new LinkedList<>();
    this.refPathParams    = new LinkedHashMap<>();
    this.refQueryParams   = new LinkedHashMap<>();
  }

  /**
   * Returns an <b>unmodifiable</b> {@link Collection} of {@link ApiDataType}
   * instances describing the types in the schemas.
   *
   * @return An <b>unmodifiable</b> {@link Collection} of {@link ApiDataType}
   *         instances describing the types in the schemas.
   */
  public Collection<ApiDataType> getSchemaTypes() {
    return Collections.unmodifiableCollection(this.schemaTypes.values());
  }

  /**
   * Gets an <b>unmodifiable</b> {@link List} of all {@link RestOperation}
   * instances for this specification.
   *
   * @return An <b>unmodifiable</b> {@link List} of all {@link RestOperation}
   *         instances for this specification.
   */
  public List<RestOperation> getOperations() {
    return Collections.unmodifiableList(this.operations);
  }

  /**
   * Gets an <b>unmodifiable</b> {@link Map} of operation tag keys to
   * <b>unmodifiable</b> {@link List} values of {@link RestOperation} instances
   * associated with the respective tag.
   *
   * @return An <b>unmodifiable</b> {@link Map} of operation tag keys to
   *         <b>unmodifiable</b> {@link List} values of {@link RestOperation}
   *         instances associated with the respective tag.
   */
  public Map<String, List<RestOperation>> getOperationsByTag() {
    return Collections.unmodifiableMap(this.operationsByTag);
  }

  /**
   * Returns an <b>unmodifiable</b> {@link Map} of {@link SzHttpMethod} keys to
   * the {@link RestOperation} values for the specified path.  This method
   * returns <tt>null</tt> if no operations are associated with the specified
   * path.
   *
   * @return An <b>unmodifiable</b> {@link Map} of {@link SzHttpMethod} keys to
   *         the {@link RestOperation} values for the specified path, or
   *         <tt>null</tt> if no operations are associated with the specified
   *         path.
   */
  public Map<SzHttpMethod, RestOperation> getOperationsForPath(String path) {
    return Collections.unmodifiableMap(this.operationsByPath.get(path));
  }

  /**
   * Adds the components of this API specification to the specified
   * {@link JsonObjectBuilder} to provide a JSON representation of the
   * specification.
   *
   * @param builder The {@link JsonObjectBuilder} to add the specification to.
   *
   */
  public void buildJson(JsonObjectBuilder builder) {
    JsonObjectBuilder compJob = Json.createObjectBuilder();
    JsonObjectBuilder pathsJob = Json.createObjectBuilder();
    this.operationsByPath.forEach((path, restOpMap) -> {
      JsonObjectBuilder pathJob = Json.createObjectBuilder();
      restOpMap.forEach((httpMethod, restOp) -> {
        JsonObjectBuilder methodJob = Json.createObjectBuilder();
        restOp.buildJson(methodJob);
        pathJob.add(httpMethod.toString(), methodJob);
      });
      pathsJob.add(path, pathJob);
    });
    builder.add("paths", pathsJob);

    JsonObjectBuilder schemasJob = Json.createObjectBuilder();
    for (ApiDataType schemaType : this.getSchemaTypes()) {
      JsonObjectBuilder schemaObj = Json.createObjectBuilder();
      schemaType.buildJson(schemaObj);
      String schemaName = schemaType.getName();
      if (schemaName == null) {
        System.err.println("NULL schema name for: ");
        System.err.println(schemaType);
        System.err.println(JsonUtils.toJsonText(schemaObj, true));
      } else {
        schemasJob.add(schemaType.getName(), schemaObj);
      }
    }

    JsonObjectBuilder refParamsObj = Json.createObjectBuilder();
    this.refPathParams.forEach((refName, pathParam) -> {
      JsonObjectBuilder paramJob = Json.createObjectBuilder();
      pathParam.buildJson(paramJob);
      refParamsObj.add(refName, paramJob);
    });
    this.refQueryParams.forEach((refName, queryParam) -> {
      JsonObjectBuilder queryJob = Json.createObjectBuilder();
      queryParam.buildJson(queryJob);
      refParamsObj.add(refName, queryJob);
    });

    compJob.add("parameters", refParamsObj);
    compJob.add("schemas", schemasJob);
    builder.add("components", compJob);

  }

  /**
   * Overridden to convert this instance to a JSON text representation.
   *
   * @return The JSON text representation of this instance.
   */
  @Override
  public String toString() {
    JsonObjectBuilder job = Json.createObjectBuilder();
    this.buildJson(job);
    return JsonUtils.toJsonText(job, true);
  }

  /**
   * Parses the API specification described by the specified {@link JsonObject}.
   *
   * @param jsonObject The {@link JsonObject} describing the specification.
   * @return The {@link ApiSpecification} that was parsed.
   */
  public static ApiSpecification parse(JsonObject jsonObject) {
    ApiSpecification result = new ApiSpecification();

    JsonObject compObj = JsonUtils.getJsonObject(jsonObject, "components");
    JsonObject schemas = JsonUtils.getJsonObject(compObj, "schemas");
    JsonObject params  = JsonUtils.getJsonObject(compObj, "parameters");

    schemas.forEach((key, value) -> {
      String name = key;
      JsonObject schemaObj = (JsonObject) value;

      ApiDataType dataType = parseDataType(name, schemaObj);
      result.schemaTypes.put(name, dataType);
    });

    // get the paths
    JsonObject paths = JsonUtils.getJsonObject(jsonObject, "paths");

    // iterate over the paths
    paths.forEach((key, value) -> {
      // get the next path
      String path = key;
      JsonObject pathObj = (JsonObject) value;

      // parse the operations for the path
      Map<SzHttpMethod, RestOperation> pathOpsMap
          = RestOperation.parse(path, pathObj, params);

      // add the operations
      result.operations.addAll(pathOpsMap.values());

      // store the results
      result.operationsByPath.put(path, pathOpsMap);

      pathOpsMap.values().forEach(restOperation -> {
        Set<String> tags = restOperation.getTags();
        for (String tag: tags) {
          List<RestOperation> list = result.operationsByTag.get(tag);
          if (list == null) {
            list = new LinkedList<>();
            result.operationsByTag.put(tag, list);
          }
          list.add(restOperation);

          // look for reference path parameters
          Collection<PathParameter> pathParams
              = restOperation.getPathParameters();
          for (PathParameter pathParam : pathParams) {
            String refName = pathParam.getRefName();
            if ((refName != null)
                && (!result.refPathParams.containsKey(refName)))
            {
                result.refPathParams.put(refName, pathParam);
            }
          }

          // look for reference query parameters
          Collection<QueryParameter> queryParams
              = restOperation.getQueryParameters();
          for (QueryParameter queryParam : queryParams) {
            String refName = queryParam.getRefName();
            if ((refName != null)
                && (!result.refQueryParams.containsKey(refName)))
            {
              result.refQueryParams.put(refName, queryParam);
            }
          }
        }
      });
    });

    // make the lists unmodifiable
    Map<String, List<RestOperation>> opsByTag = result.operationsByTag;
    for (Map.Entry<String, List<RestOperation>> entry : opsByTag.entrySet()) {
      List<RestOperation> operationList = entry.getValue();
      operationList = Collections.unmodifiableList(operationList);
      entry.setValue(operationList);
    }

    // make the maps unmodifiable
    result.operationsByPath
        = Collections.unmodifiableMap(result.operationsByPath);
    result.operationsByTag
        = Collections.unmodifiableMap(result.operationsByTag);

    return result;
  }

  /**
   * Parses an anonymous data type.
   *
   * @param schemaObj The {@link JsonObject} describing the data type.
   */
  public static ApiDataType parseDataType(JsonObject schemaObj) {
    return parseDataType(null, schemaObj, null);
  }

  /**
   * Parses an anonymous data type.
   *
   * @param schemaObj The {@link JsonObject} describing the data type.
   * @param parentObj The {@link JsonObject} describing the parent object.
   */
  public static ApiDataType parseDataType(JsonObject schemaObj,
                                          JsonObject parentObj) {
    return parseDataType(null, schemaObj, parentObj);
  }

  /**
   * Parses a data type with an optional name.
   *
   * @param name The optional name for the data type, or <tt>null</tt> if an
   *             anonymous name.
   * @param schemaObj The {@link JsonObject} describing the data type.
   */
  public static ApiDataType parseDataType(String      name,
                                          JsonObject  schemaObj) {
    return parseDataType(name, schemaObj, null);
  }

  /**
   * Parses a data type with an optional name.
   *
   * @param name The optional name for the data type, or <tt>null</tt> if an
   *             anonymous name.
   * @param schemaObj The {@link JsonObject} describing the data type.
   * @param parentObj The {@link JsonObject} describing the parent object.
   */
  public static ApiDataType parseDataType(String      name,
                                          JsonObject  schemaObj,
                                          JsonObject  parentObj)
  {
    // check the name and set the thread-local context
    String previousName = currentSchemaTypeName.get();
    if (name != null && name.trim().length() > 0) {
      currentSchemaTypeName.set(name);
    }
    try {
      String description = JsonUtils.getString(schemaObj, "description");
      String specType = JsonUtils.getString(schemaObj, "type");
      JsonArray enumArray = JsonUtils.getJsonArray(schemaObj, "enum");
      JsonArray allOfArray = JsonUtils.getJsonArray(schemaObj, "allOf");
      JsonArray oneOfArray = JsonUtils.getJsonArray(schemaObj, "oneOf");
      JsonArray anyOfArray = JsonUtils.getJsonArray(schemaObj, "anyOf");
      String format = JsonUtils.getString(schemaObj, "format");
      String ref = JsonUtils.getString(schemaObj, "$ref");
      boolean nullable = JsonUtils.getBoolean(schemaObj,
                                              "nullable",
                                              false);

      if (specType == null) {
        if (allOfArray != null) specType = "allOf";
        else if (oneOfArray != null) specType = "oneOf";
        else if (anyOfArray != null) specType = "anyOf";
        else if (ref != null) specType = "ref";
        else if ((schemaObj.size() == 0)
            || ANY_TYPE_PROPS.containsAll(schemaObj.keySet())) {
          // assume an empty object like "{ }" -- likely for
          // "additionalProperties" specification
          specType = "any";
        } else {
          System.err.println("Missing type property for '" + name
                                 + "'.  Defaulting to object.");
          System.err.println(JsonUtils.toJsonText(schemaObj, true));
          System.err.println(JsonUtils.toJsonText(parentObj, true));
          specType = "object";
        }
      } else if (specType.equals("string") && enumArray != null) {
        specType = "enum";
      }

      ApiDataType result = null;
      switch (specType) {
        case "any":
          result = new AnyType();
          break;
        case "boolean":
          result = BooleanDataType.parse(name, schemaObj);
          break;
        case "object":
          result = ObjectDataType.parse(name, schemaObj);
          break;
        case "array":
          result = ArrayDataType.parse(name, schemaObj);
          break;
        case "enum":
          result = EnumerationDataType.parse(name, schemaObj);
          break;
        case "integer":
          if ("int64".equals(format)) {
            result = LongDataType.parse(name, schemaObj);
          } else {
            result = IntegerDataType.parse(name, schemaObj);
          }
          break;
        case "number":
          if ("float".equals(format)) {
            result = FloatDataType.parse(name, schemaObj);
          } else {
            result = DoubleDataType.parse(name, schemaObj);
          }
          break;
        case "string":
          switch (format == null ? "" : format) {
            case "date":
              result = DateDataType.parse(name, schemaObj);
              break;
            case "date-time":
              result = DateTimeDataType.parse(name, schemaObj);
              break;
            case "byte":
              result = Base64DataType.parse(name, schemaObj);
              break;
            case "binary":
              result = BinaryDataType.parse(name, schemaObj);
              break;
            default:
              result = StringDataType.parse(name, schemaObj);
          }
          break;
        case "allOf":
          result = AllOfDataType.parse(name, schemaObj);
          break;
        case "anyOf":
          result = AnyOfDataType.parse(name, schemaObj);
          break;
        case "oneOf":
          result = OneOfDataType.parse(name, schemaObj);
          break;
        case "ref":
          result = RefDataType.parse(name, schemaObj);
          break;
        default:
          throw new IllegalStateException("Unhandled schema type: " + specType);
      }
      result.setDescription(description);
      result.setNullable(nullable);
      return result;
    } finally {
      if (name != null) {
        currentSchemaTypeName.set(previousName);
      }
    }
  }

  /**
   *
   */
  public static Parameter parseParameter(String     operationId,
                                         JsonObject jsonObject,
                                         JsonObject paramRefs)
  {
    String      refPath     = JsonUtils.getString(jsonObject, "$ref");
    String      refName     = (refPath == null) ? null : trimRef(refPath);
    String      typePrefix  = operationId == null ? "" : operationId;
    if (refName != null) {
      jsonObject = JsonUtils.getJsonObject(paramRefs, refName);
      typePrefix = "";
    }

    // get the parameter properties
    String      location  = JsonUtils.getString(jsonObject, "in");
    String      name      = JsonUtils.getString(jsonObject, "name");
    String      desc      = JsonUtils.getString(jsonObject, "description");
    JsonObject  typeObj   = JsonUtils.getJsonObject(jsonObject, "schema");

    // determine the type name for the type
    if (typePrefix.length() < 2) {
      typePrefix = typePrefix.toUpperCase();
    } else {
      typePrefix = typePrefix.substring(0, 1).toUpperCase() + typePrefix.substring(1);
    }
    if (!typePrefix.startsWith("Sz")) typePrefix = "Sz" + typePrefix;

    // append the parameter name
    String typeName = typePrefix + name.substring(0, 1).toUpperCase()
        + name.substring(1);

    ApiDataType dataType  = ApiSpecification.parseDataType(typeName,
                                                           typeObj,
                                                           jsonObject);

    // for query parameters
    Boolean required  = JsonUtils.getBoolean(jsonObject, "required");
    String  defVal    = JsonUtils.getString(typeObj, "default");

    Parameter param = null;
    switch (location) {
      case "path":
        param = new PathParameter();
        break;
      case "query":
        QueryParameter queryParam = new QueryParameter();
        if (required != null) queryParam.setRequired(required);
        queryParam.setDefaultValue(defVal);
        param = queryParam;

        break;

      default:
        throw new IllegalStateException(
            "Unhandled parameter location: " + location);
    }

    // set the parameter fields
    param.setName(name);
    param.setDescription(desc);
    param.setRefName(refName);
    param.setDataType(dataType);

    // return the parameter
    return param;
  }

  /**
   * Returns the thread-local name of the most recent named schema type
   * currently being parsed.  Parsing a data type with a non-null non-empty name
   * sets the value for the duration that the type is being parsed with the
   * exception of during the parsing of nested named types.  This allows type
   * parsers to access the name for purposes such as naming anonymous sub-types
   * with prefixes.
   *
   * @return The thread-local name of the current schema type being parsed, or
   *         <tt>null</tt> if a named type is not currently being parsed.
   */
  public static String getCurrentSchemaTypeName() {
    return currentSchemaTypeName.get();
  }

  /**
   * Resolves the specified {@link ApiDataType} to its actual type.  This
   * provides a means to convert a {@link RefDataType} to the actual data
   * type.  It also resolves a type by a given name to the type that is
   * globally referencable by that name.
   *
   * @param dataType The {@link ApiDataType} to resolve.
   *
   * @return The resolved {@link ApiDataType}.
   */
  public ApiDataType resolveDataType(ApiDataType dataType) {
    // if null, then return null
    if (dataType == null) return null;

    // check for a referenced type
    if (dataType instanceof RefDataType) {
      // as long as we have a referenced type, then dereference
      while (dataType instanceof RefDataType) {
        // cast to a reference type
        RefDataType refDataType = (RefDataType) dataType;

        // get the reference name and look it up
        String refName = trimRef(refDataType.getRef());
        dataType = this.schemaTypes.get(refName);

        // if the reference is not found, return null
        if (dataType == null) return null;
      }

      // return the de-referenced type
      return dataType;

    } else {
      // get the name
      String name = dataType.getName();

      // check if unnamed -- then return as-is
      if (name == null) return dataType;

      // check if the name is recognized -- globally named type in schema
      if (this.schemaTypes.containsKey(name)) {
        // if so, return the type with that name
        dataType = this.schemaTypes.get(name);

        // check if a reference type, and if so, then recurse and resolve
        if (dataType instanceof RefDataType) {
          dataType = this.resolveDataType(dataType);
        }
      }

      // return the data type
      return dataType;
    }
  }

  /**
   * Returns an <b>unmodifiable</b> {@link Map} of {@link String} property name
   * keys to {@link ObjectProperty} values describing all the properties
   * defined for the specified {@link ApiDataType}.  This resolves the specified
   * {@link ApiDataType} and handles {@link ObjectDataType} and {@link
   * AllOfDataType}, returning an empty map for types that don't have object
   * properties.
   *
   * @param dataType The {@link ApiDataType} to get the object properties for.
   * @return The <b>unmodifiable</b> {@link Map} of {@link String} property name
   *         keys to {@link ObjectProperty} values describing all the properties
   *         defined for the specified {@link ApiDataType}.
   */
  public Map<String, ObjectProperty> getResolvedProperties(ApiDataType dataType)
  {
    ApiDataType origType = dataType;
    if (dataType instanceof RefDataType) {
      dataType = this.resolveDataType(dataType);
    }
    String resolvedName = dataType.getName();
    if (dataType instanceof ObjectDataType) {
      return ((ObjectDataType) dataType).getProperties();
    } else if (dataType instanceof AllOfDataType) {
      Map<String, ObjectProperty> result = new LinkedHashMap<>();
      List<ApiDataType> compositeTypes = ((AllOfDataType) dataType).getTypes();
      for (ApiDataType compType : compositeTypes) {
        Map<String, ObjectProperty> props
            = this.getResolvedProperties(compType);
        props.forEach((key, value) -> {
          if (result.containsKey(key)) {
            if (!value.equals(result.get(key))) {
              throw new IllegalStateException(
                  "Object property collision with different property types: "
                      + "type=[ " + resolvedName + " ], property=[ " + key
                      + " ], existing=[ " + result.get(key)
                      + " ], conflicting=[ " + value + " ]");
            }
          } else {
            result.put(key, value);
          }
        });
      }
      return Collections.unmodifiableMap(result);
    } else {
      return Collections.emptyMap();
    }
  }

  /**
   * Internal method trim a reference path to its simple name.
   *
   * @return Return the simple name for a reference path.
   */
  public static String trimRef(String ref) {
    int index = ref.lastIndexOf('/');
    if (index < 0 || index == ref.length() - 1) return ref;
    return ref.substring(index+1);
  }
}

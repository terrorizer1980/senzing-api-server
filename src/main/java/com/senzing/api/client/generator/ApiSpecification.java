package com.senzing.api.client.generator;

import com.senzing.api.client.generator.schema.*;
import com.senzing.api.model.SzHttpMethod;
import com.senzing.util.JsonUtils;

import javax.json.*;
import java.util.*;

/**
 *
 */
public class ApiSpecification {
  /**
   * The list of {@link ApiDataType} instances describing the named types in
   * the schema.
   */
  private Map<String, ApiDataType> schemaTypes;

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
   * Default constructor.
   */
  public ApiSpecification() {
    this.schemaTypes = new LinkedHashMap<>();
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
   *
   */
  public void buildJson(JsonObjectBuilder builder) {
    JsonObjectBuilder schemasBuilder = Json.createObjectBuilder();
    for (ApiDataType dataType : this.getSchemaTypes()) {

    }
  }

  /**
   *
   */
  public String toString() {
    JsonObjectBuilder job = Json.createObjectBuilder();
    JsonObjectBuilder compJob = Json.createObjectBuilder();
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
    compJob.add("schemas", schemasJob);
    job.add("components", compJob);

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

    schemas.forEach((key, value) -> {
      String name = key;
      JsonObject schemaObj = (JsonObject) value;

      ApiDataType dataType = parseDataType(name, schemaObj);
      result.schemaTypes.put(name, dataType);
    });
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
    String      description = JsonUtils.getString(schemaObj, "description");
    String      specType    = JsonUtils.getString(schemaObj, "type");
    JsonArray   enumArray   = JsonUtils.getJsonArray(schemaObj, "enum");
    JsonArray   allOfArray  = JsonUtils.getJsonArray(schemaObj, "allOf");
    JsonArray   oneOfArray  = JsonUtils.getJsonArray(schemaObj, "oneOf");
    JsonArray   anyOfArray  = JsonUtils.getJsonArray(schemaObj, "anyOf");
    String      format      = JsonUtils.getString(schemaObj, "format");
    String      ref         = JsonUtils.getString(schemaObj, "$ref");
    boolean     nullable    = JsonUtils.getBoolean(schemaObj,
                                                   "nullable",
                                                   false);

    if (specType == null) {
      if (allOfArray != null) specType = "allOf";
      else if (oneOfArray != null) specType = "oneOf";
      else if (anyOfArray != null) specType = "anyOf";
      else if (ref != null) specType = "ref";
      else if (schemaObj.size() == 0) {
        // assume an empty object like "{ }" -- likely for
        // "additionalProperties" specification
        specType = "object";
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
    System.out.println(name + " => " + specType);

    ApiDataType result = null;
    switch (specType) {
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
  }

  /**
   *
   */
  public ApiDataType resolveDataType(ApiDataType dataType) {
    ApiDataType origDataType = dataType;
    if (dataType == null) return null;
    String name = dataType.getName();
    if (name == null && (dataType instanceof RefDataType)) {
      name = this.trimRef(((RefDataType) dataType).getRef());
    }
    if (name == null) return dataType;
    dataType = this.schemaTypes.get(name);
    if (dataType == null) return null;
    while (dataType instanceof RefDataType) {
      RefDataType refDataType = (RefDataType) dataType;
      String refName = this.trimRef(refDataType.getRef());
      dataType = this.schemaTypes.get(refName);
      if (dataType == null) return null;
    }
    return dataType;
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
    dataType = this.resolveDataType(dataType);
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
   *
   */
  private String trimRef(String ref) {
    int index = ref.lastIndexOf('/');
    if (index < 0 || index == ref.length() - 1) return ref;
    return ref.substring(index+1);
  }
}

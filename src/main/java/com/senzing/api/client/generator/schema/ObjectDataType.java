package com.senzing.api.client.generator.schema;

import com.senzing.api.client.generator.ApiSpecification;
import com.senzing.util.JsonUtils;

import javax.json.*;
import java.util.*;

/**
 * Extends {@link ApiDataType} to describe an object and its properties.
 */
public class ObjectDataType extends ApiDataType {
  /**
   * The {@link Map} of {@link String} property names to {@link ObjectProperty}
   * values describing the data types for the properties.
   */
  private Map<String, ObjectProperty> properties;

  /**
   * The {@link ApiDataType} for the additional properties associated with
   * the object type.
   */
  private ApiDataType additionalProperties;

  /**
   * Default constructor that constructs an anonymous instance.
   */
  public ObjectDataType() {
    this(null);
  }

  /**
   * Constructs with a name to create a named instance.
   *
   * @param name The name to associated with the data type.
   */
  public ObjectDataType(String name) {
    super(name);
    this.properties = new LinkedHashMap<>();
    this.additionalProperties = null;
  }

  /**
   * Adds a property with the specified name and data type.
   *
   * @param objectProp The data type for the property.
   */
  public void addProperty(ObjectProperty objectProp) {
    this.properties.put(objectProp.getName(), objectProp);
  }

  /**
   * Returns the {@link ObjectProperty} describing the property for the
   * specified name, or <tt>null</tt> if there is no property for the
   * specified name.
   *
   * @param name The name of the property.
   *
   * @return The {@link ObjectProperty} describing the property for the
   *         specified name, or <tt>null</tt> if there is no property for the
   *         specified name.
   */
  public ObjectProperty getProperty(String name) {
    return this.properties.get(name);
  }

  /**
   * Removes the property for the specified name.
   *
   * @param name The name of the property.
   */
  public void removeProperty(String name) {
    this.properties.remove(name);
  }

  /**
   * Removes all properties.
   */
  public void clearProperties() {
    this.properties.clear();
  }

  /**
   * Gets the <b>unmodifiable</b> {@link Map} of {@link String} property name
   * keys to {@link ObjectProperty} values describing all the properties in the
   * object.
   *
   * @return The <b>unmodifiable</b> {@link Map} of {@link String} property name
   *         keys to {@link ObjectProperty} values describing all the properties
   *         in the object.
   */
  public Map<String, ObjectProperty> getProperties() {
    return Collections.unmodifiableMap(this.properties);
  }

  /**
   * Gets the {@link ApiDataType} for the additional property values.  This
   * returns <tt>null</tt> if there are no additional properties.
   *
   * @return The {@link ApiDataType} for the additional property values, or
   *         <tt>null</tt> if there are no additional properties.
   */
  public ApiDataType getAdditionalProperties() {
    return additionalProperties;
  }

  /**
   * Sets the {@link ApiDataType} for the additional property values.  Set this
   * to <tt>null</tt> if there are no additional properties.
   *
   * @param additionalProperties The {@link ApiDataType} for the additional
   *                             property values, or <tt>null</tt> if there are
   *                             no additional properties.
   */
  public void setAdditionalProperties(ApiDataType additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  /**
   * Overridden to return <tt>true</tt> if and only if the specified parameter
   * is a non-null reference to an object of the same class with an equivalent
   * super class and equivalent properties.
   *
   * @param object The object to compare with.
   * @return <tt>true</tt> if the objects are equal, otherwise <tt>false</tt>.
   */
  public boolean equals(Object object) {
    if (!super.equals(object)) return false;
    if (this == object) return true;
    ObjectDataType dataType = (ObjectDataType) object;
    return Objects.equals(this.getProperties(), dataType.getProperties())
        && Objects.equals(this.getAdditionalProperties(),
                          dataType.getAdditionalProperties());
  }

  /**
   * Returns a hash code for this instance based on the super class and the
   * properties of this object.
   *
   * @return The hash code for this instance.
   */
  public int hashCode() {
    return Objects.hash(super.hashCode(),
                        this.getProperties(),
                        this.getAdditionalProperties());
  }

  @Override
  public void buildJson(JsonObjectBuilder builder) {
    super.buildJson(builder);
    builder.add("type", "object");
    JsonObjectBuilder propsBuilder = Json.createObjectBuilder();
    for (ObjectProperty prop : this.getProperties().values()) {
      JsonObjectBuilder job = Json.createObjectBuilder();
      prop.buildJson(job);
      propsBuilder.add(prop.getName(), job);
    }
    builder.add("properties", propsBuilder);

    ApiDataType addlDataType = this.getAdditionalProperties();
    if (addlDataType != null) {
      JsonObjectBuilder job = Json.createObjectBuilder();
      addlDataType.buildJson(job);
      builder.add("additionalProperties", job);
    }
  }

  /**
   *
   */
  public static ObjectDataType parse(String name, JsonObject jsonObject) {
    ObjectDataType objType = new ObjectDataType(name);

    // check for standard properties
    JsonObject props = JsonUtils.getJsonObject(jsonObject, "properties");
    if (props != null) {
      String parentName = (name != null) ? name
          : ApiSpecification.getCurrentSchemaTypeName();
      props.forEach((propName, value) -> {
        JsonObject propObj = (JsonObject) value;
        ObjectProperty prop = ObjectProperty.parse(parentName, propName, propObj);
        objType.addProperty(prop);
      });
    }

    // check for additional properties
    if (jsonObject.containsKey("additionalProperties")) {
      JsonValue jsonValue = jsonObject.get("additionalProperties");
      switch (jsonValue.getValueType()) {
        case TRUE:
          objType.setAdditionalProperties(new AnyType());
          break;
        case OBJECT:
          JsonObject addlProps = (JsonObject) jsonValue;
          ApiDataType addlType
              = ApiSpecification.parseDataType(addlProps, jsonObject);
          objType.setAdditionalProperties(addlType);
          break;
        default:
          throw new IllegalStateException(
              "Unrecognized data type for additional properties: "
              + jsonValue.getValueType());
      }
    }
    return objType;
  }
}

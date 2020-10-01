package com.senzing.api.client.generator.schema;

import com.senzing.api.client.generator.ApiSpecification;
import com.senzing.util.JsonUtils;

import javax.json.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for the types that are composite of multiple types.
 */
public abstract class CompositeDataType extends ApiDataType {
  /**
   * The types that make up this composite type.
   */
  private List<ApiDataType> types;

  /**
   * Default constructor.
   */
  protected CompositeDataType() {
    this(null);
  }

  /**
   * Constructs with a name to create a named instance.
   *
   * @param name The name to associated with the data type.
   */
  protected CompositeDataType(String name) {
    super(name);
    this.types = new LinkedList<>();
  }

  /**
   * Returns an <b>unmodifiable</b> {@link List} of the {@link ApiDataType}
   * instances describing the types that make up this composite type.
   *
   * @return An <b>unmodifiable</b> {@link List} of the {@link ApiDataType}
   *         instances describing the types that make up this composite type.
   */
  public List<ApiDataType> getTypes() {
    return Collections.unmodifiableList(this.types);
  }

  /**
   * Adds the specified non-null {@link ApiDataType} to the list of types that
   * make up this composite type.
   *
   * @param type The non-null {@link ApiDataType} describing the type to add to
   *             the list of types that make up this composite type.
   */
  public void addType(ApiDataType type) {
    Objects.requireNonNull(type, "Type cannot be null");
    this.types.add(type);
  }

  /**
   * Removes all types from the list of types that make up this composite type.
   */
  public void clearTypes() {
    this.types.clear();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    if (!super.equals(object)) return false;
    CompositeDataType that = (CompositeDataType) object;
    return Objects.equals(this.getTypes(), that.getTypes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), this.getTypes());
  }

  /**
   * To be implemented by the derived class to indicate the property for the
   * array of types (e.g.: "oneOf", "anyOf", "allOf", etc...)
   */
  protected abstract String getTypesProperty();

  @Override
  public void buildJson(JsonObjectBuilder builder) {
    super.buildJson(builder);
    String typesProperty = this.getTypesProperty();
    JsonArrayBuilder jab = Json.createArrayBuilder();
    List<ApiDataType> types = this.getTypes();
    for (ApiDataType type: types) {
      JsonObjectBuilder job = Json.createObjectBuilder();
      type.buildJson(job);
      jab.add(job);
    }
    builder.add(typesProperty, jab);
  }

  protected static <T extends CompositeDataType> T parse(Class<T>   typeClass,
                                                         String     name,
                                                         JsonObject jsonObject)
  {
    T result = null;
    try {
      result = typeClass.getConstructor(String.class).newInstance(name);
    } catch (Exception e) {
      throw new IllegalStateException(
          "Failed to construct instance of " + typeClass.getName());
    }
    String    typeProp  = result.getTypesProperty();
    JsonArray jsonArray = JsonUtils.getJsonArray(jsonObject,typeProp);
    for (JsonObject jsonObj: jsonArray.getValuesAs(JsonObject.class)) {
      ApiDataType dataType = ApiSpecification.parseDataType(jsonObj, jsonObject);
      result.addType(dataType);
    }
    return result;
  }
}


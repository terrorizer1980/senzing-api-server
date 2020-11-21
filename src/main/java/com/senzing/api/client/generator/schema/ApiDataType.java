package com.senzing.api.client.generator.schema;

import com.senzing.api.client.generator.ApiSpecification;
import com.senzing.util.JsonUtils;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.Objects;

/**
 * Abstract base class for named and un-named data types.
 */
public abstract class ApiDataType {
  /**
   * The optional name associated with the type.
   */
  private String name;

  /**
   * The description associated with the type.
   */
  private String description;

  /**
   * Whether or not the value can be <tt>null</tt>
   */
  private boolean nullable = false;

  /**
   * Default constructor that constructs an anonymous un-named inline type.
   */
  public ApiDataType() {
    this(null);
  }

  /**
   * Constructs with the optional name
   */
  public ApiDataType(String name) {
    this.name = name;
    this.description = null;
    this.nullable = false;
  }

  /**
   * Gets the name associated with the type.  This returns <tt>null</tt> if this
   * is an anonymous un-named inline type.
   *
   * @returns The name associated with the type or <tt>null</tt> if this is an
   *          anonymous un-named inline type.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Gets the description associated with the type.  This returns <tt>null</tt>
   * if no description has been provided.
   *
   * @returns The description provided for the type, or <tt>null</tt> if none
   *          was provided.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Sets the description associated with the type.
   *
   * @param description The description to associated with the type.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Checks if the type allows for a <tt>null</tt> value.
   *
   * @return <tt>true</tt> if a null value is allowed, otherwise <tt>false</tt>.
   */
  public boolean isNullable() {
    return nullable;
  }

  /**
   * Sets if the type allows for a <tt>null</tt> value.
   *
   * @param nullable <tt>true</tt> if a null value is allowed, otherwise
   *                 <tt>false</tt>.
   */
  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  /**
   * Implemented to compare this instance to the specified object and return
   * <tt>true</tt> if and only if the specified parameter is a non-null
   * reference to an object of the same class with an equivalent name and
   * description.
   *
   * @param object The object to compare with.
   * @return <tt>true</tt> if the objects are equal otherwise <tt>false</tt>.
   */
  public boolean equals(Object object) {
    if (object == null) return false;
    if (this == object) return true;
    if (this.getClass() != object.getClass()) return false;
    ApiDataType dataType = (ApiDataType) object;
    return this.isNullable() == dataType.isNullable()
        && Objects.equals(this.getName(), dataType.getName())
        && Objects.equals(this.getDescription(), dataType.getDescription());
  }

  /**
   * Implemented to return a hash code for this instance based off the class
   * name, name property and description property.
   *
   * @return The hash code for this instance.
   */
  public int hashCode() {
    return Objects.hash(this.getClass().getName(),
                        this.getName(),
                        this.getDescription(),
                        this.isNullable());
  }

  /**
   * Builds the JSON representation for this object.
   *
   * @param builder The {@link JsonObjectBuilder} for creating the JSON
   *                representation of this object.
   */
  public void buildJson(JsonObjectBuilder builder) {
    if (this.isNullable()) builder.add("nullable", true);
    String description = this.getDescription();
    if (description != null && description.length() > 0) {
      builder.add("description", description);
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

package com.senzing.api.client.generator.schema;

import com.senzing.util.JsonUtils;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Objects;

/**
 * Extends {@link ApiDataType} to represent reference types.
 */
public class RefDataType extends ApiDataType {
  /**
   * The reference type.
   */
  private String ref;

  /**
   * Default constructor to construct an anonymous instance.
   */
  public RefDataType() {
    this(null);
  }

  /**
   * Constructs with a optional name for a named instance.
   *
   * @param name The optional name for the instance, or <tt>null</tt> if
   *             anonymous.
   */
  public RefDataType(String name) {
    super(name);
    this.ref = null;
  }

  /**
   * Gets the ref value.
   *
   * @return The ref value.
   */
  public String getRef() {
    return this.ref;
  }

  /**
   * Sets the ref value.
   *
   * @param ref The ref value.
   */
  public void setRef(String ref) {
    this.ref = ref;
  }

  /**
   * Implemented to return <tt>true</tt> if and only if the specified parameter
   * is a non-null reference to an instance of the same class with an equivalent
   * name, description and reference value.
   *
   * @param object The object to compare with.
   * @return <tt>true</tt> if and only if the objects compare equal, otherwise
   *         <tt>false</tt>.
   */
  public boolean equals(Object object) {
    if (!super.equals(object)) return false;
    if (this == object) return true;
    RefDataType refType = (RefDataType) object;
    return Objects.equals(this.getRef(), refType.getRef());
  }

  /**
   * Implemented to return a hash code for this instance based off the super
   * class hash code and the hash code of the reference.
   *
   * @return The hash code for this instance.
   */
  public int hashCode() {
    return Objects.hash(super.hashCode(), this.getRef());
  }

  /**
   * Return a diagnostic {@link String} describing this instance.
   *
   * @return A diagnostic {@link String} describing this instance.
   */
  public String toString() {
    return super.toString() + ", ref=[ " + this.getRef() + " ]";
  }

  @Override
  public void buildJson(JsonObjectBuilder builder) {
    super.buildJson(builder);
    builder.add("$ref", this.getRef());
  }

  /**
   * Parses a ref data type with the specified optional name using the
   * specified {@link JsonObject}.
   *
   * @param name The optional name for the type, or <tt>null</tt> if it is
   *             an anonymous type.
   * @param jsonObject The {@link JsonObject} containing the information for
   *                   the data type.
   */
  public static RefDataType parse(String name, JsonObject jsonObject) {
    String typeValue = JsonUtils.getString(jsonObject, "type");
    if (typeValue != null) {
      throw new IllegalArgumentException(
          "Not a valid definition for a RefDataType: " + typeValue);
    }
    RefDataType refType = new RefDataType(name);

    String ref = JsonUtils.getString(jsonObject, "$ref");

    refType.setRef(ref);

    return refType;
  }
}

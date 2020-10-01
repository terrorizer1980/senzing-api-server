package com.senzing.api.client.generator.schema;

import com.senzing.api.client.generator.ApiSpecification;
import com.senzing.util.JsonUtils;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Objects;

/**
 * Describes an object property.
 */
public class ObjectProperty {
  /**
   * The name of the property.
   */
  private String name;

  /**
   * The {@link ApiDataType} describing the data type for the property.
   */
  private ApiDataType dataType;

  /**
   * Whether or not the property is read-only.
   */
  private boolean readOnly = false;

  /**
   * Whether or not the property is write-only.
   */
  private boolean writeOnly = false;

  /**
   * Whether or not the property can be null.
   */
  private boolean nullable = true;

  /**
   * Default constructor.
   */
  public ObjectProperty() {
    this(null, null);
  }

  /**
   * Constructs with the name and {@link ApiDataType}.
   *
   * @param name The name of the property.
   * @param dataType The {@link ApiDataType} for the data type.
   */
  public ObjectProperty(String name, ApiDataType dataType) {
    this.name     = name;
    this.dataType = dataType;
  }

  /**
   * Gets the name for the property.
   *
   * @return The name for the property.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Sets the name for the property.
   *
   * @param name The name for the property.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the {@link ApiDataType} describing the data type for the property.
   *
   * @return The {@link ApiDataType} describing the data type for the property.
   */
  public ApiDataType getDataType() {
    return this.dataType;
  }

  /**
   * Sets the {@link ApiDataType} describing the data type for the property.
   *
   * @param dataType The {@link ApiDataType} describing the data type for the
   *                 property.
   */
  public void setDataType(ApiDataType dataType) {
    this.dataType = dataType;
  }

  /**
   * Checks if the property is read-only.  If never set, the read-only flag
   * defaults to <tt>false</tt>.
   *
   * @return <tt>true</tt> if the property is read-only, otherwise
   *         <tt>false</tt>.
   */
  public boolean isReadOnly() {
    return this.readOnly;
  }

  /**
   * Sets whether or not the property is read-only.  If never set, the
   * read-only flag defaults to <tt>false</tt>.
   *
   * @param readOnly <tt>true</tt> if the property is read-only, otherwise
   *                 <tt>false</tt>.
   */
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  /**
   * Checks whether or not the property is write-only.  if never set, the
   * write-only flag defaults to <tt>false</tt>.
   *
   * @return <tt>true</tt> if the property is write-only, otherwise
   *         <tt>false</tt>
   */
  public boolean isWriteOnly() {
    return writeOnly;
  }

  /**
   * Sets whether or not the property is write-only.  If never set, the
   * write-only flag defaults to <tt>false</tt>.
   *
   * @param writeOnly <tt>true</tt> if the property is write-only, otherwise
   *                  <tt>false</tt>
   */
  public void setWriteOnly(boolean writeOnly) {
    this.writeOnly = writeOnly;
  }

  /**
   * Checks if the property is nullable.  If never set, the nullable flag
   * defaults to <tt>true</tt>.
   *
   * @return <tt>true</tt> if the property is nullable, otherwise
   *         <tt>false</tt>.
   */
  public boolean isNullable() {
    return this.nullable;
  }

  /**
   * Sets whether or not the property is nullable.  If never set, the
   * nullable flag defaults to <tt>true</tt>.
   *
   * @param nullable <tt>true</tt> if the property is nullable, otherwise
   *                 <tt>false</tt>.
   */
  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    ObjectProperty that = (ObjectProperty) object;
    return this.isReadOnly() == that.isReadOnly()
        && this.isWriteOnly() == that.isWriteOnly()
        && this.isNullable() == that.isNullable()
        && Objects.equals(this.getName(), that.getName())
        && Objects.equals(this.getDataType(), that.getDataType());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getName(),
                        this.getDataType(),
                        this.isReadOnly(),
                        this.isWriteOnly(),
                        this.isNullable());
  }

  @Override
  public String toString() {
    return "class=[ " + this.getClass().getName()
        + " ], name=[ " + this.getName()
        + " ], dataType=[ " + this.getDataType()
        + " ], readOnly=[ " + this.isReadOnly()
        + " ], writeOnly=[ " + this.isWriteOnly()
        + " ], nullable=[ " + this.isNullable()
        + " ]";
  }

  /**
   * Builds the JSON representation for this object.
   *
   * @param builder The {@link JsonObjectBuilder} for creating the JSON
   *                representation of this object.
   */
  public void buildJson(JsonObjectBuilder builder) {
    ApiDataType dataType = this.getDataType();
    if (dataType != null) dataType.buildJson(builder);
    if (this.isNullable()) builder.add("nullable", true);
    if (this.isReadOnly()) builder.add("readOnly", true);
    if (this.isWriteOnly()) builder.add("writeOnly", true);
  }

  /**
   *
   */
  public static ObjectProperty parse(String name, JsonObject jsonObject)
  {
    ApiDataType propType = ApiSpecification.parseDataType(jsonObject);
    boolean readOnly = JsonUtils.getBoolean(
        jsonObject, "readOnly", false);
    boolean writeOnly = JsonUtils.getBoolean(
        jsonObject, "writeOnly", false);
    boolean propNullable = JsonUtils.getBoolean(
        jsonObject, "nullable", false);
    ObjectProperty prop = new ObjectProperty(name, propType);
    prop.setReadOnly(readOnly);
    prop.setWriteOnly(writeOnly);
    prop.setNullable(propNullable);
    return prop;
  }
}

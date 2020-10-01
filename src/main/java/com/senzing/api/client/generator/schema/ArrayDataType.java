package com.senzing.api.client.generator.schema;

import com.senzing.api.client.generator.ApiSpecification;
import com.senzing.util.JsonUtils;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Objects;

/**
 * Extends {@link ApiDataType} to describe an array.
 */
public class ArrayDataType extends ApiDataType {
  /**
   * The type of the items in the array.
   */
  private ApiDataType itemType = null;

  /**
   * The minimum number of items (or <tt>null</tt> if there is no minimum).
   */
  private Integer minItems = null;

  /**
   * The maximum number of items (or <tt>null</tt> if there is no maximum).
   */
  private Integer maxItems;

  /**
   * Whether or not the items are unique.
   */
  private boolean unique = false;

  /**
   * Default constructor to construct an anonymous instance.
   */
  public ArrayDataType() {
    super();
  }

  /**
   * Constructs with the specified name to construct a named instance.
   *
   * @param name The name for the type.
   */
  public ArrayDataType(String name) {
    super(name);
  }

  /**
   * Gets the item type for the type of items in the array as an {@link
   * ApiDataType}.
   *
   * @return The {@link ApiDataType} describing the type of items in the array.
   */
  public ApiDataType getItemType() {
    return this.itemType;
  }

  /**
   * Sets the item type for the type of items in the array as an {@link
   * ApiDataType}.
   *
   * @param type The {@link ApiDataType} describing the type of items in the
   *             array.
   */
  public void setItemType(ApiDataType type) {
    this.itemType = type;
  }

  /**
   * Gets the minimum number of items for the array (if any).
   *
   * @return The minimum number of items for the array, or <tt>null</tt> if
   *         there is no minimum.
   */
  public Integer getMinimumItems() {
    return this.minItems;
  }

  /**
   * Sets the minimum number of items for the array (if any).
   *
   * @param minItems The minimum number of items for the array, or
   *                 <tt>null</tt> if there is no minimum.
   */
  public void setMinimumItems(Integer minItems) {
    this.minItems = minItems;
  }

  /**
   * Gets the maximum number of items for the array (if any).
   *
   * @return The maximum number of items for the array, or <tt>null</tt> if
   *         there is no maximum.
   */
  public Integer getMaximumItems() {
    return this.maxItems;
  }

  /**
   * Sets the maximum number of items for the array (if any).
   *
   * @param maxItems The maximum number of items for the array, or
   *                 <tt>null</tt> if there is no maximum.
   */
  public void setMaximumItems(Integer maxItems) {
    this.maxItems = maxItems;
  }

  /**
   * Checks whether or not the array items should be unique.
   *
   * @return <tt>true</tt> if the array items should be unique, or
   *         <tt>false</tt> if not.
   */
  public boolean isUnique() {
    return this.unique;
  }

  /**
   * Sets whether or not the array items should be unique.
   *
   * @param unique <tt>true</tt> if the array items should be unique, or
   *               <tt>false</tt> if not.
   */
  public void setUniqueItems(boolean unique) {
    this.unique = unique;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    if (!super.equals(object)) return false;
    ArrayDataType that = (ArrayDataType) object;
    return this.isUnique() == that.isUnique()
        && Objects.equals(this.getItemType(), that.getItemType())
        && Objects.equals(this.getMinimumItems(), that.getMaximumItems())
        && Objects.equals(this.getMinimumItems(), that.getMaximumItems());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(),
                        this.getItemType(),
                        this.getMinimumItems(),
                        this.getMaximumItems(),
                        this.isUnique());
  }

  @Override
  public void buildJson(JsonObjectBuilder builder) {
    super.buildJson(builder);
    builder.add("type", "array");
    ApiDataType itemType = this.getItemType();
    if (itemType != null) {
      JsonObjectBuilder job = Json.createObjectBuilder();
      itemType.buildJson(job);
      builder.add("items", job);
    }
    if (this.getMinimumItems() != null) {
      builder.add("minItems", this.getMinimumItems());
    }
    if (this.getMaximumItems() != null) {
      builder.add("maxItems", this.getMaximumItems());
    }
    if (this.isUnique()) {
      builder.add("uniqueItems", this.isUnique());
    }
  }

  /**
   * Parses an array type with the specified optional name using the
   * specified {@link JsonObject}.
   *
   * @param name The optional name for the type, or <tt>null</tt> if it is
   *             an anonymous type.
   * @param jsonObject The {@link JsonObject} containing the information for
   *                   the data type.
   */
  public static ArrayDataType parse(String name, JsonObject jsonObject) {
    ArrayDataType arrType = new ArrayDataType(name);
    JsonObject  itemObj   = JsonUtils.getJsonObject(jsonObject, "items");
    ApiDataType itemType  = ApiSpecification.parseDataType(null, itemObj, jsonObject);
    Integer     minItems  = JsonUtils.getInteger(jsonObject, "minItems");
    Integer     maxItems  = JsonUtils.getInteger(jsonObject, "maxItems");
    Boolean     unique    = JsonUtils.getBoolean(jsonObject, "uniqueItems");

    arrType.setItemType(itemType);
    if (minItems != null) arrType.setMinimumItems(minItems);
    if (maxItems != null) arrType.setMaximumItems(maxItems);
    if (unique != null)   arrType.setUniqueItems(unique);

    return arrType;
  }
}

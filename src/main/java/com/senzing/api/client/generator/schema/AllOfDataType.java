package com.senzing.api.client.generator.schema;

import javax.json.JsonObject;

/**
 * Represents an all-of data type.
 */
public class AllOfDataType extends CompositeDataType {
  /**
   * Default constructor.
   */
  public AllOfDataType() {
    // do nothing
  }

  /**
   * Constructs with a name to create a named instance.
   *
   * @param name The name to associated with the data type.
   */
  public AllOfDataType(String name) {
    super(name);
  }

  @Override
  protected String getTypesProperty() {
    return "allOf";
  }

  /**
   * Parses an all-of data type with the specified optional name using the
   * specified {@link JsonObject}.
   *
   * @param name The optional name for the type, or <tt>null</tt> if it is
   *             an anonymous type.
   * @param jsonObject The {@link JsonObject} containing the information for
   *                   the data type.
   */
  public static AllOfDataType parse(String name, JsonObject jsonObject) {
    return parse(AllOfDataType.class, name, jsonObject);
  }
}

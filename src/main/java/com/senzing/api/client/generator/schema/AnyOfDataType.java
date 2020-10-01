package com.senzing.api.client.generator.schema;

import javax.json.JsonObject;

/**
 * Represents an any-of data type.
 */
public class AnyOfDataType extends CompositeDataType {
  /**
   * Default constructor.
   */
  public AnyOfDataType() {
    // do nothing
  }

  /**
   * Constructs with a name to create a named instance.
   *
   * @param name The name to associated with the data type.
   */
  public AnyOfDataType(String name) {
    super(name);
  }

  @Override
  protected String getTypesProperty() {
    return "anyOf";
  }

  /**
   * Parses an any-of data type with the specified optional name using the
   * specified {@link JsonObject}.
   *
   * @param name The optional name for the type, or <tt>null</tt> if it is
   *             an anonymous type.
   * @param jsonObject The {@link JsonObject} containing the information for
   *                   the data type.
   */
  public static AnyOfDataType parse(String name, JsonObject jsonObject) {
    return parse(AnyOfDataType.class, name, jsonObject);
  }

}

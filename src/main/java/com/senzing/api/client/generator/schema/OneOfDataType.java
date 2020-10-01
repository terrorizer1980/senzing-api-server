package com.senzing.api.client.generator.schema;

import javax.json.JsonObject;

/**
 * Represents an one-of data type.
 */
public class OneOfDataType extends CompositeDataType {
  /**
   * Default constructor.
   */
  public OneOfDataType() {
    // do nothing
  }

  /**
   * Constructs with a name to create a named instance.
   *
   * @param name The name to associated with the data type.
   */
  public OneOfDataType(String name) {
    super(name);
  }

  @Override
  protected String getTypesProperty() {
    return "oneOf";
  }

  /**
   * Parses an one-of data type with the specified optional name using the
   * specified {@link JsonObject}.
   *
   * @param name The optional name for the type, or <tt>null</tt> if it is
   *             an anonymous type.
   * @param jsonObject The {@link JsonObject} containing the information for
   *                   the data type.
   */
  public static OneOfDataType parse(String name, JsonObject jsonObject) {
    return parse(OneOfDataType.class, name, jsonObject);
  }
}

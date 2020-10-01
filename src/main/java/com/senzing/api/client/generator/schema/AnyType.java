package com.senzing.api.client.generator.schema;

import javax.json.JsonObjectBuilder;

/**
 * Represents a type that can be any type.
 */
public class AnyType extends ApiDataType {
  /**
   * Default constructor for an anonymous type.
   */
  public AnyType() {
    super();
  }

  /**
   * Named constructor for a named type.
   *
   * @param name The name for the type.
   */
  public AnyType(String name) {
    super(name);
  }

  @Override
  public void buildJson(JsonObjectBuilder builder) {
    if (!this.isNullable() && this.getDescription() == null) {
      // return an empty builder object
      return;
    }
    super.buildJson(builder);
  }
}

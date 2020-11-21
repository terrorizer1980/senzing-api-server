package com.senzing.api.client.generator.schema;

import com.senzing.api.client.generator.ApiSpecification;

import javax.json.JsonObjectBuilder;

/**
 * Represents a type that can be any type.
 */
public class AnyType extends ApiDataType {
  /**
   * Default constructor.
   */
  public AnyType() {
    this(null);
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

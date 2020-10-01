package com.senzing.api.client.generator.schema;

import com.senzing.util.JsonUtils;

import javax.json.*;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents an enumerated data type.
 */
public class EnumerationDataType extends ApiDataType {
  /**
   * The possible enumerated values.
   */
  public Set<String> values;

  /**
   * Default constructor.
   */
  public EnumerationDataType() {
    this(null);
  }

  /**
   * Constructs with a name to create a named instance.
   *
   * @param name The name to associated with the data type.
   */
  public EnumerationDataType(String name) {
    super(name);
    this.values = new LinkedHashSet<>();
  }

  /**
   * Gets the <b>unmodifiable</b> {@link Set} of possible values.
   *
   * @return The <b>unmodifiable</b> {@link Set} of possible values.
   */
  public Set<String> getValues() {
    return Collections.unmodifiableSet(this.values);
  }

  /**
   * Adds the specified value to the set of possible values for the
   * enumeration.
   *
   * @param value The value to add to the set of enumerated values
   */
  public void addValue(String value) {
    Objects.requireNonNull(value, "The enum value cannot be null");
    this.values.add(value);
  }

  /**
   * Removes all the enumerated values from the set of enumerated values.
   */
  public void clearValues() {
    this.values.clear();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    if (!super.equals(object)) return false;
    EnumerationDataType that = (EnumerationDataType) object;
    return Objects.equals(this.getValues(), that.getValues());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), this.getValues());
  }

  @Override
  public void buildJson(JsonObjectBuilder builder) {
    super.buildJson(builder);
    builder.add("type", "string");
    JsonArrayBuilder jab = Json.createArrayBuilder();
    for (String value : this.getValues()) {
      jab.add(value);
    }
    builder.add("enum", jab);
  }

  /**
   * Parses an enumeration data type with the specified optional name using the
   * specified {@link JsonObject}.
   *
   * @param name The optional name for the type, or <tt>null</tt> if it is
   *             an anonymous type.
   * @param jsonObject The {@link JsonObject} containing the information for
   *                   the data type.
   */
  public static EnumerationDataType parse(String name, JsonObject jsonObject)
  {
    EnumerationDataType enumType = new EnumerationDataType(name);
    JsonArray enumArray = JsonUtils.getJsonArray(jsonObject, "enum");
    enumArray.getValuesAs(JsonString.class).forEach(jsonString -> {
      enumType.addValue(jsonString.getString());
    });
    return enumType;
  }
}

package com.senzing.api.client.generator.java;

import com.senzing.api.client.generator.HandlebarsModelTypeHandler;
import com.senzing.api.client.generator.LanguageAdapter;
import com.senzing.api.client.generator.schema.ApiDataType;
import com.senzing.api.client.generator.schema.EnumerationDataType;

/**
 * A handler that produces Java enumeration classes from
 * {@link EnumerationDataType} instances.
 */
public class EnumerationHandler extends HandlebarsModelTypeHandler {
  /**
   * Default constructor.
   */
  public EnumerationHandler() {
    super("EnumTemplate.java.hbs");
  }

  /**
   * Implemented to <tt>true</tt> if and only if the specified {@link
   * ApiDataType} is an instance of {@link EnumerationDataType}.
   *
   * @param dataType The {@link ApiDataType} to check if supported.
   * @return <tt>true</tt> if supported, otherwise <tt>false</tt>.
   */
  public boolean isSupported(ApiDataType      dataType,
                             LanguageAdapter  langAdapter)
  {
    return (dataType instanceof EnumerationDataType);
  }
}

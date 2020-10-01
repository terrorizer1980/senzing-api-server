package com.senzing.api.client.generator;

import com.senzing.api.client.generator.schema.ApiDataType;

public interface ModelTypeHandler {
  /**
   *
   */
  boolean isSupported(ApiDataType dataType, LanguageAdapter langAdapter);

  /**
   *
   */
  void generateModelType(ApiDataType      dataType,
                         ApiSpecification apiSpec,
                         LanguageAdapter  langAdapter);

  /**
   *
   */

}

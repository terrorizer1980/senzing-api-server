package com.senzing.api.client.generator;

import com.senzing.api.client.generator.schema.ApiDataType;

import java.util.Collections;
import java.util.List;

public interface ModelTypeHandler {
  /**
   *
   */
  boolean isSupported(ApiDataType dataType, LanguageAdapter langAdapter);

  /**
   *
   */
  void generateModelType(ApiDataType       dataType,
                         ApiSpecification  apiSpec,
                         LanguageAdapter   langAdapter);

  /**
   *
   */
  default List<ApiDataType> getAnonymousSubTypes(
      ApiDataType     dataType,
      LanguageAdapter langAdapter)
  {
    return Collections.emptyList();
  }

}

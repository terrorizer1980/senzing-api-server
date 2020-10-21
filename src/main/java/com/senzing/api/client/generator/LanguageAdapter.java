package com.senzing.api.client.generator;

import com.senzing.api.client.generator.schema.ApiDataType;

import java.io.File;
import java.util.Set;

public interface LanguageAdapter {
  /**
   *
   */
  ApiSpecification getApiSpecification();

  /**
   *
   */
  File getFileForModelType(ApiDataType dataType);

  /**
   *
   */
  String getTypeName(ApiDataType dataType);

  /**
   *
   */
  Set<String> getNativeTypeNames(ApiDataType dataType);

  /**
   *
   */
  String getNativeInitialValue(ApiDataType dataType);

  /**
   *
   */
  String getModelPath(ApiDataType dataType);

  /**
   *
   */
  Set<String> getDependencies(ApiDataType dataType);
}

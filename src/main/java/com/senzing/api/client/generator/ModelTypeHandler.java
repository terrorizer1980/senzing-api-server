package com.senzing.api.client.generator;

import com.senzing.api.client.generator.schema.ApiDataType;

import java.util.Collections;
import java.util.List;

/**
 * The base interface for generating a language-specific model representation.
 */
public interface ModelTypeHandler {
  /**
   * Checks if the specified {@link ApiDataType} and {@link LanguageAdapter}
   * are supported by this handler.
   *
   * @param dataType The {@link ApiDataType} to check.
   * @param apiSpec The {@link ApiSpecification} that the specified {@link
   *                ApiDataType} is associated with.
   * @param langAdapter The {@link LanguageAdapter} to check.
   *
   * @return <tt>true</tt> if supported, otherwise <tt>false</tt>.
   */
  boolean isSupported(ApiDataType       dataType,
                      ApiSpecification  apiSpec,
                      LanguageAdapter   langAdapter);

  /**
   * Generates the language-specific model representation for the specified
   * {@link ApiDataType} using the specified {@link LanguageAdapter}.
   *
   * @param dataType The {@link ApiDataType} for which to generate the model
   *                 representation.
   *
   * @param apiSpec The {@link ApiSpecification} that the specified {@link
   *                ApiDataType} is associated with.
   *
   * @param langAdapter The {@link LanguageAdapter} to use for generation.
   */
  void generateModelType(ApiDataType       dataType,
                         ApiSpecification  apiSpec,
                         LanguageAdapter   langAdapter);

  /**
   * Obtains the anonymous sub-types for the specified {@Link ApiDataType}.
   * The default implementation returns an <b>unmodifiable</b> empty {@link
   * List} since most implementations of {@link ApiDataType} cannot have any
   * anonymous sub-types.
   *
   * @param dataType The {@link ApiDataType} for which to get the anonymous
   *                 sub-types.
   * @param apiSpec The {@link ApiSpecification} that the specified {@link
   *                ApiDataType} is associated with.
   * @param langAdapter The {@link LanguageAdapter} to use.
   *
   * @return The {@link List} of {@link ApiDataType} instances.
   */
  default List<ApiDataType> getAnonymousSubTypes(
      ApiDataType       dataType,
      ApiSpecification  apiSpec,
      LanguageAdapter   langAdapter)
  {
    return Collections.emptyList();
  }

}

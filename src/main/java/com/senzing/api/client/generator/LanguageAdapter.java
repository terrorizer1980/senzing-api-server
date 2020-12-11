package com.senzing.api.client.generator;

import com.senzing.api.client.generator.schema.ApiDataType;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * An interface for handling generating client code for a specific language.
 */
public interface LanguageAdapter {
  /**
   * Gets the {@link File} representing the file location where the model
   * representation for the specified {@link ApiDataType} will be generated.
   * This method returns <tt>null</tt> for types that are {@linkplain
   * #isBasicType(ApiDataType, ApiSpecification) basic}.
   *
   * @param dataType The {@link ApiDataType} for which the file is being
   *                 requested.
   *
   * @param apiSpec The {@link ApiSpecification} that the specified {@link
   *                ApiDataType} is associated with.
   *
   * @return The {@link File} for the model representation of the specified
   *         {@link ApiDataType}, or <tt>null</tt> if the specified type is a
   *         basic type.
   */
  File getFileForModelType(ApiDataType      dataType,
                           ApiSpecification apiSpec);

  /**
   * Gets the {@link File} representing the file location where the service
   * tag representation for the specified service tag will be generated.
   * This method returns <tt>null</tt> for tags that are not present in the
   * {@link ApiSpecification}.
   *
   * @param tag The service tag for which the file is being requested.
   *
   * @param apiSpec The {@link ApiSpecification} that has the specified tag.
   *
   * @return The {@link File} for the representation of the specified service
   *         tag, or <tt>null</tt> if the specified tag is not present.
   */
  File getFileForService(String tag, ApiSpecification apiSpec);

  /**
   * Gets the {@link File} representing the file location where the operation
   * representation for the specified {@link RestOperation} will be generated.
   * This method returns <tt>null</tt> for operations that are not present in
   * the {@link ApiSpecification}.
   *
   * @param op The {@link RestOperation} for which the file is being requested.
   *
   * @param apiSpec The {@link ApiSpecification} that has the specified
   *                {@link RestOperation}.
   *
   * @return The {@link File} for the representation of the specified {@link
   *         RestOperation}, or <tt>null</tt> if the specified {@link
   *         RestOperation} is not present in the spec.
   */
  File getFileForOperation(RestOperation op, ApiSpecification apiSpec);

  /**
   * Returns the generated model type name associated with the specified type,
   * or <tt>null</tt> if the specified {@link ApiDataType} is considered to be
   * a {@linkplain #isBasicType(ApiDataType, ApiSpecification) basic data type}.
   *
   * @param dataType The {@link ApiDataType} for which the name is desired.
   *
   * @param apiSpec The {@link ApiSpecification} that the specified {@link
   *                ApiDataType} is associated with.
   *
   * @return The name associated with the {@link ApiDataType} or <tt>null</tt>
   *         if the specified type is a basic type.
   */
  String getTypeName(ApiDataType      dataType,
                     ApiSpecification apiSpec);

  /**
   * Checks if the specified {@link ApiDataType} is considered a basic
   * built-in type for the language (i.e.: no model class needs to be
   * generated).
   *
   * @param dataType The {@link ApiDataType} to check.
   *
   * @param apiSpec The {@link ApiSpecification} that the specified {@link
   *                ApiDataType} is associated with.
   *
   * @return <tt>true</tt> if the specified type is a basic built-in type for
   *         language, otherwise <ttr>false</ttr>.
   */
  boolean isBasicType(ApiDataType       dataType,
                      ApiSpecification  apiSpec);

  /**
   * Generates the model types for the specified {@link ApiSpecification}.
   *
   * @param apiSpec The {@link ApiSpecification} for which to generate the
   *                model types.
   */
  void generateModelTypes(ApiSpecification apiSpec);

  /**
   * Generates the service representations for the specified {@link
   * ApiSpecification}.
   *
   * @param apiSpec The {@link ApiSpecification} for which to generate the
   *                service representations.
   */
  void generateServices(ApiSpecification apiSpec);

  /**
   * Returns the native type names associated with the specified {@link
   * ApiDataType} using the specified {@link ApiSpecification}.  Most types
   * map to a single native type name, but some may map to multiple possible
   * representations.
   *
   * @param dataType The {@link ApiDataType} to get the type names for.
   *
   * @param apiSpec The {@link ApiSpecification} that the specified {@link
   *                ApiDataType} is associated with.
   *
   * @return The {@link Map} of native type name keys that represent the
   *         specified {@link ApiDataType} to {@link Map} values that
   *         represent the language-specific additional information for that
   *         type.
   */
  Map<String, Map<String,Object>> getNativeTypeNames(
      ApiDataType      dataType,
      ApiSpecification apiSpec);

  /**
   * Returns the native-language representation of the initial value for the
   * specified {@link ApiDataType}.
   *
   * @param dataType The {@link ApiDataType} to get the initial value for.
   *
   * @param apiSpec The {@link ApiSpecification} that the specified {@link
   *                ApiDataType} is associated with.
   *
   * @return The naitve-language representation of the initial value for the
   *         specified {@link ApiDataType}.
   */
  String getNativeInitialValue(ApiDataType      dataType,
                               ApiSpecification apiSpec);

  /**
   * Returns the sub-path where the model representation for the specified
   * {@link ApiDataType} will be generated.  It is likely that the returned
   * value will be the same for all data types.  This method returns
   * <tt>null</tt> for {@linkplain #isBasicType(ApiDataType,ApiSpecification)
   * basic types}.
   *
   * @param dataType The {@link ApiDataType} for which the model path is
   *                 being requested.
   *
   * @param apiSpec The {@link ApiSpecification} that the specified {@link
   *                ApiDataType} is associated with.
   *
   * @return The model path for the specified {@link ApiDataType}, or
   *         <tt>null</tt> if the specified type is a basic type.
   */
  String getModelSubPath(ApiDataType       dataType,
                         ApiSpecification  apiSpec);

  /**
   * Returns the sub-path where the representation of the specified service tag
   * will be generated.
   *
   * @param tag The service tag for which the file is being requested.
   *
   * @param apiSpec The {@link ApiSpecification} that has the specified tag.
   *
   *
   * @return The sub-path where the representation of the specified service tag
   *         will be generated.
   */
  String getServiceSubPath(String tag, ApiSpecification apiSpec);

  /**
   * Returns the sub-path where the representation of the specified {@link
   * RestOperation} will be generated.
   *
   * @param op The {@link RestOperation} for which the file is being requested.
   *
   * @param apiSpec The {@link ApiSpecification} that has the specified
   *                {@link RestOperation}.
   *
   * @return The sub-path where the representation of the specified
   *         {@link RestOperation} will be generated.
   */
  String getOperationSubPath(RestOperation op, ApiSpecification apiSpec);

  /**
   * Returns the {@link Set} of dependencies for the specified {@link
   * ApiDataType}.  This returns an empty set for basic types.
   *
   * @param dataType The {@link ApiDataType} for which the dependencies are
   *                 being requested.
   *
   * @param apiSpec The {@link ApiSpecification} that the specified {@link
   *                ApiDataType} is associated with.
   *
   * @return The {@link Set} of dependencies for the specified {@link
   *         ApiDataType}.
   */
  Set<String> getDependencies(ApiDataType       dataType,
                              ApiSpecification  apiSpec);
}

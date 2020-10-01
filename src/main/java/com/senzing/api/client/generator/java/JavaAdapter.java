package com.senzing.api.client.generator.java;

import com.senzing.api.client.generator.*;
import com.senzing.api.client.generator.schema.*;

import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * Provides a {@link LanguageAdapter} for the Java programming language.
 */
public class JavaAdapter extends AbstractLanguageAdapter {
  /**
   * The map of {@link ApiDataType} classes to the <b>unmodifiable</b>
   * {@link Set} of basic Java types used to represent them.
   */
  private static Map<Class<? extends ApiDataType>, Set<Class>> BASIC_TYPE_MAP;

  /**
   * The map of {@link ApiDataType} classes to the <b>unmodifiable</b>
   * {@link Set} of required Java imports.
   */
  private static Map<Class<? extends ApiDataType>, Set<String>> BASIC_IMPORT_MAP;

  /**
   * The {@link Map} of {@link ApiDataType} classes to {@link ModelTypeHandler}
   * instances for generating code for that type.
   */
  private static Map<Class<? extends ApiDataType>, ModelTypeHandler>
      HANDLER_MAP;

  static {
    Map<Class<? extends ApiDataType>, Set<Class>> map = new LinkedHashMap<>();
    map.put(AnyType.class, Set.of(Object.class));
    map.put(Base64DataType.class, Set.of(byte[].class));
    map.put(BinaryDataType.class, Set.of(File.class, InputStream.class));
    map.put(BooleanDataType.class, Set.of(Boolean.class));
    map.put(DateDataType.class, Set.of(Date.class));
    map.put(DoubleDataType.class, Set.of(Double.class));
    map.put(FloatDataType.class, Set.of(Float.class));
    map.put(IntegerDataType.class, Set.of(Integer.class));
    map.put(LongDataType.class, Set.of(Long.class));
    map.put(StringDataType.class, Set.of(String.class));

    BASIC_TYPE_MAP = Collections.unmodifiableMap(map);

    Map<Class<? extends ApiDataType>, ModelTypeHandler> handlerMap
        = new LinkedHashMap<>();

    handlerMap.put(EnumerationDataType.class, new EnumerationHandler());
    handlerMap.put(ObjectDataType.class, new ObjectHandler());
    handlerMap.put(AllOfDataType.class, new AllOfHandler());

    HANDLER_MAP = Collections.unmodifiableMap(handlerMap);

    Map<Class<? extends ApiDataType>, Set<String>> importMap
        = new LinkedHashMap<>();

    importMap.put(BinaryDataType.class,
                  Set.of("java.io.File", "java.io.InputStream"));
  }

  /**
   *
   */
  private String modelPackage;

  /**
   *
   */
  private File baseDirectory;

  /**
   *
   */
  public JavaAdapter(String            modelPackage,
                     File              baseDirectory,
                     ApiSpecification  apiSpec)
  {
    super(apiSpec);
    this.modelPackage   = modelPackage;
    this.baseDirectory  = baseDirectory;
  }

  /**
   *
   */
  public void generateModelTypes() {
    for (ApiDataType dataType : this.apiSpec.getSchemaTypes()) {
      if (BASIC_TYPE_MAP.containsKey(dataType)) continue;

      this.generateModelType(dataType,null);
    }
  }

  /**
   *
   */
  private void generateModelType(ApiDataType dataType,
                                 ApiDataType parentType)
  {
    ModelTypeHandler handler = HANDLER_MAP.get(dataType.getClass());
    if (handler == null) return;
    handler.generateModelType(dataType, this.apiSpec, this);
  }

  /**
   *
   */
  public File getFileForModelType(ApiDataType dataType) {
    String path = this.getModelPath(dataType);
    if (path == null) return null;
    File modelDir = new File(this.baseDirectory, path);
    modelDir.mkdirs();
    String name = this.getTypeName(dataType);
    File sourceFile = new File(modelDir, name + ".java");
    return sourceFile;
  }

  /**
   *
   */
  public String getModelPath(ApiDataType dataType) {
    if (BASIC_TYPE_MAP.containsKey(dataType)) return null;
    return this.modelPackage.replace('.', '/');
  }

  /**
   *
   */
  public Set<String> getDependencies(ApiDataType dataType) {
    if (BASIC_TYPE_MAP.containsKey(dataType)) {
      Set<String> deps = BASIC_IMPORT_MAP.get(dataType);
      if (deps != null) return deps;
      return Collections.emptySet();
    }
    // check if we have a composite data type
    if (dataType instanceof CompositeDataType) {
      // cast to a composite data type and create the return set
      CompositeDataType compDataType  = (CompositeDataType) dataType;
      Set<String>       deps          = new LinkedHashSet<>();

      // loop over the sub types
      for (ApiDataType subType : compDataType.getTypes()) {

        // get the type name
        String typeName = this.getTypeName(subType);

        // check for the model package
        if (this.modelPackage != null && this.modelPackage.trim().length() > 0)
        {
          typeName = modelPackage + "." + typeName;
        }

        // add to the set
        deps.add(typeName);
      }

      // return an unmodifiable set
      return Collections.unmodifiableSet(deps);

    } else if (dataType instanceof ObjectDataType) {
      return null;
    }
    return null;
  }
}

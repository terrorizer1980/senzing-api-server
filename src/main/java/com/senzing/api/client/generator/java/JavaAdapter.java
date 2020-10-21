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

    importMap.put(DateDataType.class, Set.of("java.util.Date"));
    importMap.put(BinaryDataType.class,
                  Set.of("java.io.File", "java.io.InputStream"));

    BASIC_IMPORT_MAP = Collections.unmodifiableMap(importMap);
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
    this.modelPackage   = modelPackage.replaceAll("/", ".");
    this.baseDirectory  = baseDirectory;
  }

  /**
   *
   */
  public void generateModelTypes() {
    Set<String> generatedTypes = new HashSet<>();
    for (ApiDataType dataType : this.apiSpec.getSchemaTypes()) {
      if (BASIC_TYPE_MAP.containsKey(dataType)) continue;
      this.generateModelType(dataType);

    }
  }

  /**
   *
   */
  private void generateModelType(ApiDataType dataType)
  {
    ModelTypeHandler handler = HANDLER_MAP.get(dataType.getClass());
    if (handler == null) return;
    List<ApiDataType> subTypes = handler.getAnonymousSubTypes(dataType,this);
    for (ApiDataType subType: subTypes) {
      this.generateModelType(subType);
    }
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
   * Convenience method to get the fully-qualified type name with the model
   * package for the specified data type.  This returns <tt>null</tt> if the
   * base type name is <tt>null</tt> (i.e.: for basic types).
   *
   * @param dataType The {@link ApiDataType} that the type name is wanted for.
   *
   * @return The fully-qualfiied type name or <tt>null</tt> if no type name.
   */
  public String getFullTypeName(ApiDataType dataType) {
    // get the type name
    String typeName = this.getTypeName(dataType);

    if (typeName == null) return null;

    // check for the model package
    if (this.modelPackage != null && this.modelPackage.trim().length() > 0) {
      typeName = this.modelPackage + "." + typeName;
    }

    return typeName;
  }

  /**
   *
   */
  public Set<String> getNativeTypeNames(ApiDataType dataType) {
    ApiSpecification apiSpec = this.getApiSpecification();

    // first off we need to resolve the type
    dataType = apiSpec.resolveDataType(dataType);

    String fullTypeName = this.getTypeName(dataType);
    if (fullTypeName != null) return Set.of(fullTypeName);

    // check if we have a basic map
    if (dataType.getClass() == ObjectDataType.class) {
      ObjectDataType objDataType = (ObjectDataType) dataType;
      if (objDataType.getProperties().size() == 0) {
        ApiDataType addlPropsType = objDataType.getAdditionalProperties();
        if (addlPropsType == null) return Set.of("Map<String, ?>");
        addlPropsType = apiSpec.resolveDataType(addlPropsType);

        Set<String> valueTypes = this.getNativeTypeNames(addlPropsType);
        Set<String> result = new LinkedHashSet<>();
        for (String valueType: valueTypes) {
          result.add("Map<String, " + valueType + ">");
        }
        return Collections.unmodifiableSet(result);
      }
    }

    // get the types from the basic map
    Set<Class> basicClasses = BASIC_TYPE_MAP.get(dataType.getClass());
    if (basicClasses != null) {
      Set<String> result = new LinkedHashSet<>();
      for (Class c : basicClasses) {
        result.add(c.getSimpleName());
      }
      return Collections.unmodifiableSet(result);
    }

    throw new IllegalStateException(
        "Unable to determine native types for ApiDataType: " + dataType);
  }

  /**
   *
   */
  public String getNativeInitialValue(ApiDataType dataType) {
    if (dataType == null) return null;
    if (dataType instanceof NumberDataType) {
      // check if a number and if nullable
      NumberDataType numDataType = (NumberDataType) dataType;
      if (numDataType.isNullable()) return "null";

      // handle long integers
      if (dataType instanceof LongDataType) {
        LongDataType longDataType = (LongDataType) numDataType;
        if (longDataType.getMinimum() != null) {
          return longDataType.getMinimum() + "L";
        } else {
          return "0L";
        }
      }

      // handle integers
      if (dataType instanceof IntegerDataType) {
        IntegerDataType intDataType = (IntegerDataType) numDataType;
        if (intDataType.getMinimum() != null) {
          return String.valueOf(intDataType.getMinimum());
        } else {
          return "0";
        }
      }

      // handle single-precision floating point
      if (dataType instanceof FloatDataType) {
        FloatDataType floatDataType = (FloatDataType) numDataType;
        if (floatDataType.getMinimum() != null) {
          return floatDataType.getMinimum() + "f";
        } else {
          return "0.0f";
        }
      }

      // handle double-precision floating point
      if (dataType instanceof DoubleDataType) {
        DoubleDataType dblDataType = (DoubleDataType) numDataType;
        if (dblDataType.getMinimum() != null) {
          return String.valueOf(dblDataType.getMinimum());
        } else {
          return "0.0";
        }
      }
    } else if (dataType instanceof BooleanDataType) {
      BooleanDataType boolDataType = (BooleanDataType) dataType;
      if (boolDataType.isNullable()) return "null";
      return String.valueOf(Boolean.FALSE);

    }

    // for all other types we return null
    return "null";
  }

  /**
   *
   */
  public Set<String> getDependencies(ApiDataType dataType)
  {
    dataType = this.getApiSpecification().resolveDataType(dataType);

    System.err.println();
    System.err.println("----------------------------------------");
    System.err.println("DATA TYPE: " + dataType.getClass().getSimpleName()
                           + " / " + dataType.getName());

    // check if we have a basic type
    if (BASIC_TYPE_MAP.containsKey(dataType)) {
      System.err.println("*** BASIC TYPE");
      Set<String> deps = BASIC_IMPORT_MAP.get(dataType);
      System.err.println("DEPS: " + deps);
      if (deps != null) return deps;
      return Collections.emptySet();
    }

    // create the result set
    Set<String> deps = new LinkedHashSet<>();

    // check if we have a composite data type
    if ((dataType instanceof AllOfDataType) && dataType.getName() != null) {
      System.err.println("*** ALL-OF TYPE");
      String typeName = this.getFullTypeName(dataType);

      // check if we succeeded in getting a type name
      if (typeName != null) {
        deps.add(typeName);
        System.err.println("DEPS: " + typeName);
        return Collections.unmodifiableSet(deps);
      }

    } else if (dataType instanceof CompositeDataType) {
      System.err.println("*** COMPOSITE TYPE");
      // cast to a composite data type and create the return set
      CompositeDataType compDataType  = (CompositeDataType) dataType;

      // loop over the sub types
      for (ApiDataType subType : compDataType.getTypes()) {
        deps.addAll(this.getDependencies(subType));
      }

      System.err.println("DEPS: " + deps);

      // return an unmodifiable set
      return Collections.unmodifiableSet(deps);

    } else if (dataType instanceof ObjectDataType) {
      ObjectDataType objectDataType = (ObjectDataType) dataType;
      System.err.println("*** OBJECT TYPE");
      String typeName = this.getFullTypeName(dataType);

      // check if we succeeded in getting a type name
      if (typeName != null) {
        deps.add(typeName);
        System.err.println("DEPS: " + typeName);
        return Collections.unmodifiableSet(deps);
      }

      // check if we maybe have a basic map
      if (objectDataType.getProperties().size() == 0) {
        // check for additional properties
        ApiDataType addlPropsType = objectDataType.getAdditionalProperties();
        if (addlPropsType != null) {
          // resolve the additional properties type
          addlPropsType = apiSpec.resolveDataType(addlPropsType);

          // add the map type
          deps.add("java.util.Map");

          deps.addAll(this.getDependencies(addlPropsType));

          System.err.println("DEPS: " + deps);
          return Collections.unmodifiableSet(deps);
        }
      }

      System.err.println("NO DEPS");
      return Collections.emptySet();

    } else if (dataType instanceof ArrayDataType) {
      System.err.println("*** ARRAY TYPE");
      ArrayDataType arrayDataType = (ArrayDataType) dataType;

      // check if we have a set or a list
      if (arrayDataType.isUnique()) {
        deps.add("java.util.Set");
      } else {
        deps.add("java.util.List");
      }

      // get the dependencies for the item type
      deps.addAll(this.getDependencies(arrayDataType.getItemType()));

      System.err.println("DEPS: " + deps);
      return Collections.unmodifiableSet(deps);

    } else if (dataType instanceof EnumerationDataType) {
      System.err.println("*** ENUM TYPE");
      String typeName = this.getTypeName(dataType);
      // check for the model package
      if (this.modelPackage != null && this.modelPackage.trim().length() > 0)
      {
        typeName = this.modelPackage + "." + typeName;
      }
      deps.add(typeName);
      deps.add("static " + typeName + ".*");
      System.err.println("DEPS: " + deps);
      return Collections.unmodifiableSet(deps);
    }
    System.err.println("NO DEPS");
    return Collections.emptySet();
  }
}

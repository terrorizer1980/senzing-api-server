package com.senzing.api.client.generator.java;

import com.senzing.api.client.generator.*;
import com.senzing.api.client.generator.schema.*;
import com.senzing.api.model.SzHttpMethod;

import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * Provides a {@link LanguageAdapter} for the Java programming language.
 */
public class JavaAdapter extends AbstractLanguageAdapter {
  /**
   * The template file to use for generating services.
   */
  private static final String SERVICE_TEMPLATE = "Service.java.hbs";

  /**
   * The template file to use for generating operations.
   */
  private static final String OPERATION_TEMPLATE = "Operation.java.hbs";

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
    map.put(ArrayDataType.class, Set.of(List.class));

    BASIC_TYPE_MAP = Collections.unmodifiableMap(map);

    Map<Class<? extends ApiDataType>, ModelTypeHandler> handlerMap
        = new LinkedHashMap<>();

    handlerMap.put(EnumerationDataType.class, new EnumerationHandler());
    handlerMap.put(ObjectDataType.class, new ObjectHandler());
    handlerMap.put(AllOfDataType.class, new AllOfHandler());
    handlerMap.put(OneOfDataType.class, new OneOfHandler());

    HANDLER_MAP = Collections.unmodifiableMap(handlerMap);

    Map<Class<? extends ApiDataType>, Set<String>> importMap
        = new LinkedHashMap<>();

    importMap.put(DateDataType.class, Set.of("java.util.Date"));
    importMap.put(BinaryDataType.class,
                  Set.of("java.io.File", "java.io.InputStream"));

    BASIC_IMPORT_MAP = Collections.unmodifiableMap(importMap);
  }

  /**
   * The package name for storing the model classes.
   */
  private String modelPackage;

  /**
   * The package that will hold the services classes.
   */
  private String servicePackage;

  /**
   * The package name for the
   */
  private String servicesPackage;

  /**
   * The base directory for output.
   */
  private File baseDirectory;

  /**
   * Constructs with the model package, base directory and API specification.
   *
   */
  public JavaAdapter(String modelPackage,
                     String servicePackage,
                     File   baseDirectory)
  {
    super(SERVICE_TEMPLATE, OPERATION_TEMPLATE);
    this.modelPackage   = modelPackage.replaceAll("/", ".");
    this.servicePackage = servicePackage.replaceAll("/", ".");
    this.baseDirectory  = baseDirectory;
  }

  /**
   * Implemented to return the Java-specific {@link ModelTypeHandler} for the
   * specified {@link ApiDataType}.
   */
  @Override
  protected ModelTypeHandler getModelTypeHandler(ApiDataType      dataType,
                                                 ApiSpecification apiSpec)
  {
    return HANDLER_MAP.get(dataType.getClass());
  }

  /**
   * Implemented to return the Java file name for the specified {@link
   * ApiDataType}.
   */
  @Override
  public File getFileForModelType(ApiDataType       dataType,
                                  ApiSpecification  apiSpec)
  {
    String path = this.getModelSubPath(dataType, apiSpec);
    if (path == null) return null;
    File modelDir = new File(this.baseDirectory, path);
    modelDir.mkdirs();
    String name = this.getTypeName(dataType, apiSpec);
    File sourceFile = new File(modelDir, name + ".java");
    return sourceFile;
  }

  /**
   * Implemented to return the Java file name for the specified service tag.
   */
  @Override
  public File getFileForService(String tag, ApiSpecification apiSpec)
  {
    String path = this.getServiceSubPath(tag, apiSpec);
    if (path == null) return null;
    File serviceDir = new File(this.baseDirectory, path);
    serviceDir.mkdirs();
    tag = (tag.length() > 1)
        ? tag.substring(0, 1) + tag.substring(1) : tag.toUpperCase();
    String name = tag + "Services";
    File sourceFile = new File(serviceDir, name + ".java");
    return sourceFile;
  }

  /**
   * Implemented to return the Java file name for the specified {@link
   * RestOperation}.
   */
  @Override
  public File getFileForOperation(RestOperation     restOp,
                                  ApiSpecification  apiSpec)
  {
    String path = this.getOperationSubPath(restOp, apiSpec);
    if (path == null) return null;
    File serviceDir = new File(this.baseDirectory, path);
    serviceDir.mkdirs();
    String opId = restOp.getOperationId();
    String name = (opId.length() > 1)
        ? opId.substring(0, 1) + opId.substring(1) : opId.toUpperCase();
    File sourceFile = new File(serviceDir, name + ".java");
    return sourceFile;
  }

  /**
   * Implemented to return the model sub-path by converting the Java package
   * name into a file path with forward slashes.
   */
  @Override
  public String getModelSubPath(ApiDataType      dataType,
                                ApiSpecification apiSpec)
  {
    if (BASIC_TYPE_MAP.containsKey(dataType.getClass())) return null;
    return this.modelPackage.replace('.', '/');
  }

  /**
   * Implemented to return the service tag sub-path by converting the Java
   * package name into a file path with forward slashes.
   */
  @Override
  public String getServiceSubPath(String tag, ApiSpecification apiSpec)
  {
    if (!apiSpec.getOperationsByTag().containsKey(tag)) return null;
    return this.servicePackage.replace('.', '/');
  }

  /**
   * Implemented to return the {@link RestOperation} sub-path by converting the
   * Java package name into a file path with forward slashes.
   */
  @Override
  public String getOperationSubPath(RestOperation     restOp,
                                    ApiSpecification  apiSpec)
  {
    String path = restOp.getPath();
    Map<SzHttpMethod, RestOperation> map = apiSpec.getOperationsForPath(path);
    if (!restOp.equals(map.get(restOp.getHttpMethod()))) return null;
    return this.servicePackage.replace('.', '/');
  }

  /**
   * Implemented to return <tt>true</tt> if the specified type is a basic
   * type for Java.
   */
  @Override
  public boolean isBasicType(ApiDataType dataType, ApiSpecification apiSpec) {
    // if (dataType.getName() != null) return false; -- not sure we need this
    return (BASIC_TYPE_MAP.containsKey(dataType.getClass()));
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
  public String getFullTypeName(ApiDataType       dataType,
                                ApiSpecification  apiSpec) {
    // get the type name
    String typeName = this.getTypeName(dataType, apiSpec);

    if (typeName == null) return null;

    // check for the model package
    if (this.modelPackage != null && this.modelPackage.trim().length() > 0) {
      typeName = this.modelPackage + "." + typeName;
    }

    return typeName;
  }

  /**
   * Returns the {@link Set} of Java type names for the specified {@link
   * ApiDataType}.
   */
  @Override
  public Set<String> getNativeTypeNames(ApiDataType       dataType,
                                        ApiSpecification  apiSpec) {
    // first off we need to resolve the type
    dataType = apiSpec.resolveDataType(dataType);

    String fullTypeName = this.getTypeName(dataType, apiSpec);
    if (fullTypeName != null) return Set.of(fullTypeName);

    // check if we have an array
    if (dataType.getClass() == ArrayDataType.class) {
      ApiDataType itemType = ((ArrayDataType) dataType).getItemType();
      itemType = apiSpec.resolveDataType(itemType);

      Set<String> nativeItemTypes = this.getNativeTypeNames(itemType, apiSpec);
      Set<String> result = new LinkedHashSet<>();
      for (String nativeItemType: nativeItemTypes) {
        result.add("List<" + nativeItemType + ">");
      }
      return Collections.unmodifiableSet(result);
    }

    // check if we have a basic map
    if (dataType.getClass() == ObjectDataType.class) {
      ObjectDataType objDataType = (ObjectDataType) dataType;
      if (objDataType.getProperties().size() == 0) {
        ApiDataType addlPropsType = objDataType.getAdditionalProperties();
        if (addlPropsType == null) return Set.of("Map<String, ?>");
        addlPropsType = apiSpec.resolveDataType(addlPropsType);

        Set<String> valueTypes = this.getNativeTypeNames(addlPropsType,
                                                         apiSpec);
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
   * Gets the text for the initial value for a property having the specified
   * {@link ApiDataType}.
   *
   * @param dataType The {@link ApiDataType} for which the na
   */
  @Override
  public String getNativeInitialValue(ApiDataType       dataType,
                                      ApiSpecification  apiSpec)
  {
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
  @Override
  public Set<String> getDependencies(ApiDataType      dataType,
                                     ApiSpecification apiSpec)
  {
    dataType = apiSpec.resolveDataType(dataType);

    // check if we have a basic type
    if (BASIC_TYPE_MAP.containsKey(dataType.getClass())) {
      Set<String> deps = BASIC_IMPORT_MAP.get(dataType.getClass());
      if (deps != null) return deps;
      return Collections.emptySet();
    }

    // create the result set
    Set<String> deps = new LinkedHashSet<>();

    // check if we have a composite data type
    if ((dataType instanceof AllOfDataType) && dataType.getName() != null) {
      String typeName = this.getFullTypeName(dataType, apiSpec);

      // check if we succeeded in getting a type name
      if (typeName != null) {
        deps.add(typeName);
        return Collections.unmodifiableSet(deps);
      }

    } else if (dataType instanceof CompositeDataType) {
      // cast to a composite data type and create the return set
      CompositeDataType compDataType = (CompositeDataType) dataType;

      // loop over the sub types
      for (ApiDataType subType : compDataType.getTypes()) {
        deps.addAll(this.getDependencies(subType, apiSpec));
      }

      // return an unmodifiable set
      return Collections.unmodifiableSet(deps);

    } else if (dataType instanceof ObjectDataType) {
      ObjectDataType objectDataType = (ObjectDataType) dataType;
      String typeName = this.getFullTypeName(dataType, apiSpec);

      // check if we succeeded in getting a type name
      if (typeName != null) {
        deps.add(typeName);
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

          deps.addAll(this.getDependencies(addlPropsType, apiSpec));

          return Collections.unmodifiableSet(deps);
        }
      }

      return Collections.emptySet();

    } else if (dataType instanceof ArrayDataType) {
      ArrayDataType arrayDataType = (ArrayDataType) dataType;

      // check if we have a set or a list
      if (arrayDataType.isUnique()) {
        deps.add("java.util.Set");
      } else {
        deps.add("java.util.List");
      }

      // get the dependencies for the item type
      deps.addAll(this.getDependencies(arrayDataType.getItemType(), apiSpec));

      return Collections.unmodifiableSet(deps);

    } else if (dataType instanceof EnumerationDataType) {
      String typeName = this.getTypeName(dataType, apiSpec);
      // check for the model package
      if (this.modelPackage != null && this.modelPackage.trim().length() > 0) {
        typeName = this.modelPackage + "." + typeName;
      }
      deps.add(typeName);
      deps.add("static " + typeName + ".*");
      return Collections.unmodifiableSet(deps);
    }
    return Collections.emptySet();
  }

  /**
   * Sorts the imports for cosmetic purposes.
   */
  protected static Set<String> sortImports(Set<String> importSet) {
    List<String> importList = new LinkedList<>();
    importList.addAll(importSet);
    Collections.sort(importList);
    importSet = new LinkedHashSet<>();
    for (String imported : importList) {
      if (imported.startsWith("java.")) importSet.add(imported);
    }
    for (String imported : importList) {
      if (imported.startsWith("org.")) importSet.add(imported);
    }
    for (String imported : importList) {
      if (imported.startsWith("com.")) importSet.add(imported);
    }
    for (String imported : importList) {
      if ((!imported.startsWith("static ")) && !importSet.contains(imported)) {
        importSet.add(imported);
      }
    }
    for (String imported : importList) {
      if (imported.startsWith("static ")) importSet.add(imported);
    }
    return importSet;
  }
}

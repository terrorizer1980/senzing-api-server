package com.senzing.api.client.generator.java;

import com.github.jknack.handlebars.Context;
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
   * The native type info map key for getting the {@link ApiDataType} describing
   * the type.
   */
  public static final String TYPE_KEY = "type";

  /**
   * The native type info map key for getting the type declaration including
   * any parameter types.
   */
  public static final String TYPE_DECLARATION_KEY = "typeDeclaration";

  /**
   * The native type info map key for getting the type name (this will be sans
   * the parameter types for type erasure purposes).
   */
  public static final String TYPE_NAME_KEY = "typeName";

  /**
   * The native type info map key for getting the type description in
   * camel case.
   */
  public static final String TYPE_DESCRIPTION_KEY = "typeDescription";

  /**
   * The native type info map key for getting the type description in
   * upper case.
   */
  public static final String TYPE_CONSTANT_KEY = "typeConstant";

  /**
   * The native type info map key for getting the list of classes used to
   * define the type.
   */
  public static final String TYPE_PARAMS_KEY = "typeParameters";

  /**
   * The native type info map key for getting the camel case type description
   * of the array element type when dealing with array types.
   */
  public static final String ITEM_DESCRIPTION_KEY = "itemDescription";

  /**
   * The native type info map key for getting the camel case type description
   * of the map value element type when dealing with basic map types.
   */
  public static final String VALUE_DESCRIPTION_KEY = "valueDescription";

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
    map.put(DateTimeDataType.class, Set.of(Date.class));
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
    handlerMap.put(OneOfDataType.class, new OneOfHandler());

    HANDLER_MAP = Collections.unmodifiableMap(handlerMap);

    Map<Class<? extends ApiDataType>, Set<String>> importMap
        = new LinkedHashMap<>();

    importMap.put(DateDataType.class, Set.of("java.util.Date"));
    importMap.put(DateTimeDataType.class, Set.of("java.util.Date"));
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
    String name = tagToClassName(tag) + "Service";
    File sourceFile = new File(serviceDir, name + ".java");
    return sourceFile;
  }

  /**
   * Converts a tag into a camel case representation.
   *
   * @param tag The tag to convert.
   * @return The camel case representation of the tag.
   */
  protected static String tagToClassName(String tag) {
    StringBuilder sb = new StringBuilder();
    for (String token: tag.split("\\s+")) {
      token = token.trim();
      if (token.length() == 0) continue;
      if (token.length() == 1) {
        sb.append(token.toUpperCase());
        continue;
      }
      sb.append(token.substring(0, 1).toUpperCase());
      sb.append(token.substring(1));
    }
    return sb.toString();
  }

  /**
   * Creates the handlebars template context for generating the service.
   *
   * @param tag The tag for the service.
   * @param apiSpec The associated {@link ApiSpecification}.
   * @return The {@link Context} that was created.
   */
  @Override
  protected Map<String, Object> createServiceTemplateContext(
      String            tag,
      ApiSpecification  apiSpec)
  {
    Map<String, Object> contextMap
        = super.createServiceTemplateContext(tag, apiSpec);

    String packageName = this.servicePackage.replace('/', '.');

    String className   = tagToClassName(tag) + "Service";

    contextMap.put("packageName", packageName);
    contextMap.put("className", className);
    contextMap.put("fullClassName", packageName + "." + className);

    return contextMap;
  }

  /**
   * Creates the handlebars template context for generating the operation.
   *
   * @param restOp The {@link RestOperation} to generate the code for.
   * @param apiSpec The associated {@link ApiSpecification}.
   * @return The {@link Map} representing the content for the {@link Context}.
   */
  protected Map<String, Object> createOperationTemplateContext(
      RestOperation    restOp,
      ApiSpecification apiSpec)
  {
    Map<String, Object> context
        = super.createOperationTemplateContext(restOp, apiSpec);

    // add Java-specific properties
    String packageName = this.servicePackage.replace('/', '.');
    String opId        = restOp.getOperationId().trim();
    String className   = (opId.length() > 1)
        ? opId.substring(0, 1).toUpperCase() + opId.substring(1)
        : opId.toUpperCase();

    context.put("packageName", packageName);
    context.put("className", className);
    context.put("fullClassName", packageName + "." + className);

    // return the new context
    return context;
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
    String opId = restOp.getOperationId().trim();
    String name = (opId.length() > 1)
        ? opId.substring(0, 1).toUpperCase() + opId.substring(1)
        : opId.toUpperCase();
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
    if (this.isBasicType(dataType, apiSpec)) return null;
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
    if (dataType instanceof ArrayDataType) {
      return true;
    }
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
   * Returns the {@link Map} of Java type names to optional {@link Map} instances
   * containing the additional type-specific info for that type name for
   * the specified {@link ApiDataType}.
   */
  @Override
  public Map<String, Map<String, Object>> getNativeTypeNames(
      ApiDataType       dataType,
      ApiSpecification  apiSpec)
  {
    // first off we need to resolve the type
    ApiDataType resolvedType = apiSpec.resolveDataType(dataType);

    String typeName = this.getTypeName(resolvedType, apiSpec);
    if (typeName != null) {
      Map<String, Object> infoMap = new LinkedHashMap<>();
      infoMap.put(TYPE_KEY, resolvedType);
      infoMap.put(TYPE_NAME_KEY, typeName);
      infoMap.put(TYPE_DECLARATION_KEY, typeName);
      infoMap.put(TYPE_DESCRIPTION_KEY, stripPrefix(typeName));
      infoMap.put(TYPE_CONSTANT_KEY, toConstantCase(stripPrefix(typeName)));
      infoMap.put(TYPE_PARAMS_KEY, Collections.singletonList(typeName));
      Map<String, Map<String, Object>> result = new LinkedHashMap<>();
      result.put(typeName, infoMap);
      return result;
    }

    // check if we have an array
    if (resolvedType.getClass() == ArrayDataType.class) {
      ArrayDataType arrayDataType = (ArrayDataType) resolvedType;
      ApiDataType itemType = arrayDataType.getItemType();
      itemType = apiSpec.resolveDataType(itemType);

      Map<String, Map<String, Object>> nativeItemTypes
          = this.getNativeTypeNames(itemType, apiSpec);

      Map<String, Map<String, Object>> result = new LinkedHashMap<>();

      nativeItemTypes.forEach((nativeItemType,  itemInfoMap) -> {
        List<String> itemTypeParams
            = (List<String>) itemInfoMap.get(TYPE_PARAMS_KEY);
        String itemTypeDesc = (String) itemInfoMap.get(TYPE_DESCRIPTION_KEY);

        List<String> typeParams = new ArrayList<>(itemTypeParams.size()+1);

        String collectionType = (arrayDataType.isUnique()) ? "Set" : "List";

        typeParams.add(collectionType);
        typeParams.addAll(itemTypeParams);
        Map<String, Object> infoMap = new LinkedHashMap<>();
        String typeDesc = collectionType + "Of" + stripPrefix(itemTypeDesc);
        String typeDeclaration = collectionType + "<" + nativeItemType + ">";
        infoMap.put(TYPE_KEY, resolvedType);
        infoMap.put(TYPE_NAME_KEY, collectionType);
        infoMap.put(TYPE_DECLARATION_KEY, typeDeclaration);
        infoMap.put(TYPE_DESCRIPTION_KEY, stripPrefix(typeDesc));
        infoMap.put(TYPE_CONSTANT_KEY, toConstantCase(stripPrefix(typeDesc)));
        infoMap.put(TYPE_PARAMS_KEY, typeParams);
        infoMap.put(ITEM_DESCRIPTION_KEY, itemTypeDesc);
        result.put(typeDeclaration, infoMap);
      });

      return result;
    }

    // check if we have a basic map
    if (resolvedType.getClass() == ObjectDataType.class) {
      ObjectDataType objDataType = (ObjectDataType) resolvedType;
      if (objDataType.getProperties().size() == 0) {
        ApiDataType addlPropsType = objDataType.getAdditionalProperties();
        if (addlPropsType == null) {
          Map<String, Object> infoMap = new LinkedHashMap<>();
          String typeDeclaration = "Object";
          infoMap.put(TYPE_KEY, resolvedType);
          infoMap.put(TYPE_DECLARATION_KEY, typeDeclaration);
          infoMap.put(TYPE_NAME_KEY, typeDeclaration);
          infoMap.put(TYPE_DESCRIPTION_KEY, typeDeclaration);
          infoMap.put(TYPE_CONSTANT_KEY, toConstantCase(typeDeclaration));
          infoMap.put(TYPE_PARAMS_KEY, List.of(typeDeclaration));
          Map<String, Map<String, Object>> result = new LinkedHashMap<>();
          result.put(typeDeclaration, infoMap);
          return result;
        }
        addlPropsType = apiSpec.resolveDataType(addlPropsType);

        Map<String, Map<String, Object>> valueTypes
            = this.getNativeTypeNames(addlPropsType, apiSpec);

        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        valueTypes.forEach((valueType, valueInfoMap) -> {
          List<String> valueTypeParams
              = (List<String>) valueInfoMap.get(TYPE_PARAMS_KEY);

          String valueTypeDesc
              = (String) valueInfoMap.get(TYPE_DESCRIPTION_KEY);

          List<String> typeParams = new ArrayList<>(valueTypeParams.size()+1);

          typeParams.add("Map");
          typeParams.add("String");
          typeParams.addAll(valueTypeParams);
          Map<String, Object> infoMap = new LinkedHashMap<>();
          String typeDesc = "MapOf" + stripPrefix(valueTypeDesc);
          String typeDeclaration = "Map<String, " + valueType + ">";
          infoMap.put(TYPE_KEY, resolvedType);
          infoMap.put(TYPE_DECLARATION_KEY, typeDeclaration);
          infoMap.put(TYPE_NAME_KEY, "Map");
          infoMap.put(TYPE_DESCRIPTION_KEY, stripPrefix(typeDesc));
          infoMap.put(TYPE_CONSTANT_KEY, toConstantCase(stripPrefix(typeDesc)));
          infoMap.put(TYPE_PARAMS_KEY, typeParams);
          infoMap.put(VALUE_DESCRIPTION_KEY, valueTypeDesc);

          result.put(typeDeclaration, infoMap);
        });
        return result;
      }
    }

    // get the types from the basic type map
    Set<Class> basicClasses = BASIC_TYPE_MAP.get(resolvedType.getClass());
    if (basicClasses != null) {
      Map<String, Map<String, Object>> result = new LinkedHashMap<>();
      for (Class c : basicClasses) {
        Map<String, Object> infoMap = new LinkedHashMap<>();
        String typeDesc = c.getSimpleName();
        infoMap.put(TYPE_KEY, resolvedType);
        infoMap.put(TYPE_DECLARATION_KEY, typeDesc);
        infoMap.put(TYPE_NAME_KEY, typeDesc);
        infoMap.put(TYPE_DESCRIPTION_KEY, stripPrefix(typeDesc));
        infoMap.put(TYPE_CONSTANT_KEY, toConstantCase(stripPrefix(typeDesc)));
        infoMap.put(TYPE_PARAMS_KEY, Collections.singletonList(typeDesc));
        result.put(typeDesc, infoMap);
      }
      return result;
    }

    throw new IllegalStateException(
        "Unable to determine native types for ApiDataType: " + resolvedType);
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
   * Gets the literal text for the default value of the specified query
   * parameter.  This returns <tt>null</tt> if no default value.
   *
   * @param queryParam The {@link QueryParameter} to get the default value for.
   * @param apiSpec The associated {@link ApiSpecification} for the query
   *                parameter.
   * @return The {@link String} value for the default literal value.
   */
  @Override
  public String getNativeDefaultValue(QueryParameter    queryParam,
                                      ApiSpecification  apiSpec)
  {
    String defaultValue = queryParam.getDefaultValue();
    if (defaultValue == null) return null;
    ApiDataType dataType        = queryParam.getDataType();
    String      defaultLiteral  = defaultValue;
    if (dataType instanceof EnumerationDataType) {
      defaultLiteral = this.getTypeName(dataType, apiSpec)
          + "." + defaultLiteral;
    } else if (dataType instanceof StringDataType) {
      defaultLiteral = "\"" + defaultLiteral + "\"";
    } else if (dataType instanceof LongDataType) {
      defaultLiteral = defaultLiteral + "L";
    } else if (dataType instanceof FloatDataType) {
      defaultLiteral = defaultLiteral + "f";
    }
    return defaultLiteral;
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

  /**
   *
   */
  private static String stripPrefix(String text) {
    if (text == null) return null;
    if (text.length() < 3) return text;
    if (text.length() > 3 && text.startsWith("SZ_")) {
      return text.substring(3);
    }
    if (text.length() > 2 && (text.startsWith("Sz") || text.startsWith("SZ"))) {
      return text.substring(2);
    }
    return text;
  }
}

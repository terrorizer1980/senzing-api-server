package com.senzing.api.client.generator;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.URLTemplateSource;
import com.senzing.api.client.generator.schema.ApiDataType;
import com.senzing.api.client.generator.schema.ObjectDataType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.*;

import static com.senzing.io.IOUtilities.UTF_8;

/**
 * Provides a base abstract implementation of {@link LanguageAdapter}.
 */
public abstract class AbstractLanguageAdapter implements LanguageAdapter {
  /**
   * The {@link Handlebars} instance to use for compiling templates.
   */
  protected static final Handlebars HANDLEBARS = new Handlebars();

  /**
   * The {@link Map} of {@link ApiDataType} class keys to integer counts of the
   * number of names have been generated for anonymous {@link ApiDataType}
   * instances.
   */
  protected Map<Class<? extends ApiDataType>, Integer> anonymousNameCounts;

  /**
   * The map of type name keys to {@link Map} values containing {@link
   * ApiDataType} keys to actual name values.
   */
  protected Map<String, Map<ApiDataType, String>> typeNameMap;

  /**
   * The {@link Map} of {@link ApiDataType} instances to type name values.
   */
  protected Map<ApiDataType, String> anonymousTypeNames;

  /**
   * The template path for generating a service representation.
   */
  protected String serviceTemplatePath = null;

  /**
   * The template path for generating an operation representation.
   */
  protected String operationTemplatePath = null;

  /**
   * Constructs with the specified service template path and operation template
   * path.
   *
   * @param serviceTemplatePath The Handlebars resource template path for
   *                            generating a service representation.
   * @param operationTemplatePath The Handlebars resource template path for
   *                              generating an operation representation.
   */
  protected AbstractLanguageAdapter(String serviceTemplatePath,
                                    String operationTemplatePath)
  {
    this.typeNameMap            = new LinkedHashMap<>();
    this.anonymousNameCounts    = new LinkedHashMap<>();
    this.anonymousTypeNames     = new LinkedHashMap<>();
    this.serviceTemplatePath    = serviceTemplatePath;
    this.operationTemplatePath  = operationTemplatePath;
  }

  /**
   * Generates all the model types contained in the specified {@link
   * ApiSpecification}.
   *
   * @param apiSpec The {@link ApiSpecification} containing the model type
   *                definitions for generating the model types.
   */
  @Override
  public void generateModelTypes(ApiSpecification apiSpec) {
    Set<String> generatedTypes = new HashSet<>();
    for (ApiDataType dataType : apiSpec.getSchemaTypes()) {
      if (this.isBasicType(dataType, apiSpec)) continue;
      this.generateModelType(dataType, apiSpec);
    }

    // loop through the inline types in the operations
    List<RestOperation> operations = apiSpec.getOperations();
    operations.forEach((restOp) -> {
      // get the path parameters
      restOp.getPathParameters().forEach(param -> {
        ApiDataType dataType = param.getDataType();
        if (this.isBasicType(dataType, apiSpec)) return;
        this.generateModelType(dataType, apiSpec);
      });

      // get the query parameters
      restOp.getQueryParameters().forEach(param -> {
        ApiDataType dataType = param.getDataType();
        if (this.isBasicType(dataType, apiSpec)) return;
        this.generateModelType(dataType, apiSpec);
      });

      // get the request body
      RequestBody requestBody = restOp.getRequestBody();
      if (requestBody != null) {
        ApiDataType bodyType = requestBody.getBodyType();
        if (bodyType != null) {
          this.generateModelType(bodyType, apiSpec);
        }
      }
    });
  }

  /**
   * Generates the model representation for the specified {@link ApiDataType}.
   *
   * @param dataType The {@link ApiDataType} for which to generate the model
   *                 model representation.
   *
   * @param apiSpec The {@link ApiSpecification} that the specified {@link
   *                ApiDataType} is associated with.
   */
  protected void generateModelType(ApiDataType      dataType,
                                   ApiSpecification apiSpec)
  {
    ModelTypeHandler handler = this.getModelTypeHandler(dataType, apiSpec);
    if (handler == null) return;
    List<ApiDataType> subTypes
        = handler.getAnonymousSubTypes(dataType, apiSpec, this);
    for (ApiDataType subType: subTypes) {
      this.generateModelType(subType, apiSpec);
    }
    handler.generateModelType(dataType, apiSpec,this);
  }

  /**
   * Generates the services for the specified {@link ApiSpecification}.
   *
   * @param apiSpec The {@link ApiSpecification} for which to generate the
   *                services.
   */
  @Override
  public void generateServices(ApiSpecification apiSpec) {
    Map<String, List<RestOperation>> map = apiSpec.getOperationsByTag();

    IdentityHashMap<RestOperation, Boolean> handled = new IdentityHashMap<>();

    map.keySet().forEach((tag) -> {
      this.generateService(tag, apiSpec);
    });

    apiSpec.getOperations().forEach(restOp -> {
      this.generateOperation(restOp, apiSpec);
    });
  }

  /**
   * Generates the service and associated {@link RestOperation} instances for
   * the specified tag and {@link ApiSpecification}.
   *
   * @param tag The tag for the service.
   * @param apiSpec The {@link ApiSpecification} for the service.
   */
  protected void generateService(String              tag,
                                 ApiSpecification    apiSpec) {
    File file = this.getFileForService(tag, apiSpec);
    try (FileOutputStream fos = new FileOutputStream(file);
         OutputStreamWriter osw = new OutputStreamWriter(fos, UTF_8))
    {
      String templatePath = this.getServiceTemplate();

      URL url = this.getClass().getResource(templatePath);

      URLTemplateSource templateSource
          = new URLTemplateSource(templatePath, url);

      Template template = HANDLEBARS.compile(templateSource);

      Context context = Context.newContext(
          this.createServiceTemplateContext(tag, apiSpec));

      template.apply(context, osw);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Generates the service and associated {@link RestOperation} instances for
   * the specified tag and {@link ApiSpecification}.
   *
   * @param restOp The {@link RestOperation} for which to generate code.
   * @param apiSpec The {@link ApiSpecification} for the service.
   */
  protected void generateOperation(RestOperation    restOp,
                                   ApiSpecification apiSpec)
  {
    File file = this.getFileForOperation(restOp, apiSpec);
    try (FileOutputStream fos = new FileOutputStream(file);
         OutputStreamWriter osw = new OutputStreamWriter(fos, UTF_8))
    {
      String templatePath = this.getOperationTemplate();

      URL url = this.getClass().getResource(templatePath);

      URLTemplateSource templateSource
          = new URLTemplateSource(templatePath, url);

      Template template = HANDLEBARS.compile(templateSource);

      Context context = Context.newContext(
          this.createOperationTemplateContext(restOp, apiSpec));

      System.out.println("OPERATION CONTEXT: \n" + context);
      template.apply(context, osw);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates the handlebars template context for the service associated with
   * the specified tag.
   * @param tag The tag for the service being generated.
   * @param apiSpec The associated {@link ApiSpecification}.
   * @return The {@link Map} representing the content for the {@link Context}.
   */
  protected Map<String, Object> createServiceTemplateContext(
      String            tag,
      ApiSpecification  apiSpec)
  {
    Map<String, Object> contextMap = new LinkedHashMap<>();
    contextMap.put("tag", tag);
    List<RestOperation> restOps   = apiSpec.getOperationsByTag().get(tag);
    if (restOps == null) restOps  = Collections.emptyList();
    List<Map<String, Object>> list = new ArrayList<>(restOps.size());
    for (RestOperation restOp: restOps) {
      Map<String, Object> opMap
          = this.createOperationTemplateContext(restOp, apiSpec);
      list.add(opMap);
    }
    contextMap.put("operations", list);
    return contextMap;
  }

  /**
   * Creates the handlebars template context for generating code relative to the
   * specified {@link RestOperation}.
   *
   * @param restOp The {@link RestOperation} to create the context for.
   * @param apiSpec The associated {@link ApiSpecification}.
   * @return The {@link Map} representing the content for the {@link Context}.
   */
  protected Map<String, Object> createOperationTemplateContext(
      RestOperation     restOp,
      ApiSpecification  apiSpec)
  {
    Map<String, Object> contextMap = new LinkedHashMap<>();
    contextMap.putAll(restOp.toContextMap());

    Collection<PathParameter> pathParams = restOp.getPathParameters();
    List<Map<String, Object>> pathList = new ArrayList<>(pathParams.size());
    for (PathParameter param: pathParams) {
      Map<String, Object> paramMap
          = this.createPathParamTemplateContext(param, apiSpec);
      pathList.add(paramMap);
    }

    Collection<QueryParameter> queryParams = restOp.getQueryParameters();
    List<Map<String, Object>> queryList = new ArrayList<>(queryParams.size());
    for (QueryParameter param: queryParams) {
      Map<String, Object> paramMap
          = this.createQueryParamTemplateContext(param, apiSpec);
      queryList.add(paramMap);
    }

    contextMap.put("pathParams", pathList);
    contextMap.put("queryParams", queryList);

    List<Map<String, Object>> allParameters
        = new ArrayList<>(pathList.size() + queryList.size());
    allParameters.addAll(pathList);
    allParameters.addAll(queryList);

    contextMap.put("params", allParameters);

    return contextMap;
  }

  /**
   * Internal support method for populating common context properties in
   * the specified {@link Map} for the specified {@link Parameter}.
   *
   * @param contextMap The {@link Map} to populate.
   * @param param The {@link Parameter} to populate properties for.
   * @param apiSpec The associated {@link ApiSpecification}.
   */
  protected void populateParameterContext(Map<String, Object> contextMap,
                                          Parameter           param,
                                          ApiSpecification    apiSpec)
  {
    // add Java-specific properties
    ApiDataType dataType = apiSpec.resolveDataType(param.getDataType());

    Map<String, Map<String, Object>> nativeTypes
        = this.getNativeTypeNames(dataType, apiSpec);

    contextMap.putAll(param.toContextMap());
    contextMap.put("types", nativeTypes);
    contextMap.put("type", nativeTypes.values().iterator().next());

    String name = param.getName();
    contextMap.put("Name", toUpperFirst(name));
    contextMap.put("NAME", toConstantCase(name));
    System.out.println("NATIVE TYPE: " + contextMap.get("type"));

    contextMap.put("initialValue", this.getNativeInitialValue(dataType, apiSpec));
  }

  /**
   * Creates the handlebars template context for generating code relative to the
   * specified {@link PathParameter}.
   *
   * @param param The {@link PathParameter} to create the context for..
   * @param apiSpec The associated {@link ApiSpecification}.
   * @return The {@link Map} representing the content for the {@link Context}.
   */
  protected Map<String, Object> createPathParamTemplateContext(
      PathParameter    param,
      ApiSpecification apiSpec)
  {
    Map<String, Object> contextMap = new LinkedHashMap<>();
    this.populateParameterContext(contextMap, param, apiSpec);
    return contextMap;
  }

  /**
   * Creates the handlebars template context for generating code relative to the
   * specified {@link QueryParameter}.
   *
   * @param param The {@link QueryParameter} to create the context for..
   * @param apiSpec The associated {@link ApiSpecification}.
   * @return The {@link Map} representing the content for the {@link Context}.
   */
  protected Map<String, Object> createQueryParamTemplateContext(
      QueryParameter    param,
      ApiSpecification  apiSpec)
  {
    Map<String, Object> contextMap = new LinkedHashMap<>();

    this.populateParameterContext(contextMap, param, apiSpec);
    if (param.getDefaultValue() != null) {
      contextMap.put("hasDefault", true);
      contextMap.put("defaultLiteral",
                     this.getNativeDefaultValue(param, apiSpec));
    } else {
      contextMap.put("hasDefault", false);
    }

    // return the new context
    return contextMap;
  }

  /**
   * Provides the template path for the handle bars template to use for
   * generating an API service representation.
   *
   * @return The template resource path for generating a service.
   */
  protected String getServiceTemplate() {
    return this.serviceTemplatePath;
  }

  /**
   * Provides the template path for the handle bars template to use for
   * generating a service operation representation.
   *
   * @return The template resource path for generating an operation.
   */
  protected String getOperationTemplate() {
    return this.operationTemplatePath;
  }

  /**
   * Returns the {@link ModelTypeHandler} to handle the specified {@link
   * ApiDataType}.
   *
   * @param dataType The {@link ApiDataType} for which to get the
   *                 {@link ModelTypeHandler}.
   *
   * @param apiSpec The {@link ApiSpecification} that the specified {@link
   *                ApiDataType} is associated with.
   *
   * @return The {@link ModelTypeHandler} for the specified {@link ApiDataType}.
   */
  protected abstract ModelTypeHandler getModelTypeHandler(
      ApiDataType       dataType,
      ApiSpecification  apiSpec);

  /**
   * Implemented to resolve the specified {@link ApiDataType} using the
   * specified {@link ApiSpecification} and obtain the associated type name.
   *
   * @param dataType The {@link ApiDataType} for which the name is being
   *                 requested.
   *
   * @param apiSpec The {@link ApiSpecification} that the specified {@link
   *                ApiDataType} is associated with.
   *
   * @return The type name for the specified {@link ApiDataType}.
   */
  @Override
  public String getTypeName(ApiDataType       dataType,
                            ApiSpecification  apiSpec)
  {
    // resolve the data type
    dataType = apiSpec.resolveDataType(dataType);
    if (dataType == null) return null;

    // check if it not a anonymous
    String name = dataType.getName();
    if (name != null && name.trim().length() > 0) {
      // get the type map for the name
      Map<ApiDataType, String> typeMap = this.typeNameMap.get(name);
      if (typeMap == null) {
        typeMap = new LinkedHashMap<>();
        this.typeNameMap.put(name, typeMap);
      }
      // check if we have seen this data type with this name before
      if (typeMap.containsKey(dataType)) {
        return typeMap.get(dataType);
      }
      // check if we already have a different type with this name
      if (typeMap.size() > 0) {
        name = name + (typeMap.size() + 1);
      }
      typeMap.put(dataType, name);
      return name;
    }

    // check if we have a generic map object
    if (dataType.getClass() == ObjectDataType.class) {
      ObjectDataType objDataType = (ObjectDataType) dataType;
      if (objDataType.getProperties().size() == 0) {
        ApiDataType addlPropsType = objDataType.getAdditionalProperties();
        // this type can be represented by a basic map
        if (addlPropsType != null) return null;
      }
    }

    // check if we have a basic type
    if (this.isBasicType(dataType, apiSpec)) return null;

    // check if we have already generated an anonymous name for this one
    name = this.anonymousTypeNames.get(dataType);
    if (name != null) {
      return name;
    }

    // generate a new anonymous name
    Class c = dataType.getClass();
    Integer count = this.anonymousNameCounts.get(c);
    if (count == null) {
      count = 0;
      this.anonymousNameCounts.put(c, count);
    }
    count = count + 1;
    this.anonymousNameCounts.put(c, count);
    name = c.getSimpleName() + count;
    this.anonymousTypeNames.put(dataType, name);

    // return the name
    return name;
  }

  /**
   *
   */
  protected static String toUpperFirst(String text) {
    if (text == null) return null;
    text = text.trim();
    if (text.length() < 2) return text.toUpperCase();
    return text.substring(0, 1).toUpperCase() + text.substring(1);
  }

  /**
   *
   */
  protected static String toConstantCase(String text) {
    StringBuilder sb = new StringBuilder();
    boolean prevLower = false;
    for (char c: text.toCharArray()) {
      boolean upper = Character.isUpperCase(c);
      if (upper && prevLower) sb.append("_");
      sb.append(Character.toUpperCase(c));
      prevLower = (!upper);
    }
    return sb.toString();
  }


}

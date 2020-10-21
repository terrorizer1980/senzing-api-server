package com.senzing.api.client.generator.java;

import com.github.jknack.handlebars.Context;
import com.senzing.api.client.generator.ApiSpecification;
import com.senzing.api.client.generator.LanguageAdapter;
import com.senzing.api.client.generator.schema.*;

import java.util.*;

public class AllOfHandler extends ObjectHandler {
  /**
   * Default constructor.
   */
  public AllOfHandler() {
    super();
  }

  /**
   * Implemented to <tt>true</tt> if and only if the specified {@link
   * ApiDataType} is an instance of {@link AllOfDataType} and all of the
   * composite types are of type {@link ObjectDataType}.
   *
   * @param dataType The {@link ApiDataType} to check if supported.
   * @return <tt>true</tt> if supported, otherwise <tt>false</tt>.
   */
  public boolean isSupported(ApiDataType      dataType,
                             LanguageAdapter  langAdapter)
  {
    ApiSpecification apiSpec = langAdapter.getApiSpecification();
    if (!(dataType instanceof AllOfDataType)) return false;
    List<ApiDataType> compositeTypes = ((AllOfDataType) dataType).getTypes();
    for (ApiDataType compType: compositeTypes) {
      compType = apiSpec.resolveDataType(compType);
      if (compType instanceof ObjectDataType) return true;
      if (compType instanceof AllOfDataType) return true;
      return false;
    }
    return true;
  }

  /**
   * Sets up the template {@link Context} to use for populating the template.
   *
   * @param dataType The {@link ApiDataType} to produce the enum class for.
   * @param apiSpec The {@link ApiSpecification} that the type belongs to.
   * @param langAdapter The {@link LanguageAdapter} to leverage.
   */
  protected Context createTemplateContext(ApiDataType       dataType,
                                          ApiSpecification  apiSpec,
                                          LanguageAdapter   langAdapter)
  {
    AllOfDataType allOfDataType = (AllOfDataType) dataType;
    List<ApiDataType> compositeTypes = allOfDataType.getTypes();
    if (compositeTypes.size() == 0) {
      throw new IllegalArgumentException(
          "Cannot have AllOfDataType with no composite types: " + dataType);
    }

    Set<String> imports     = new LinkedHashSet<>();

    ApiDataType extendsType = null;
    String      extendsName = null;
    String      extendsPkg  = null;

    if (compositeTypes.get(0) instanceof RefDataType) {
      extendsType = apiSpec.resolveDataType(compositeTypes.get(0));
      extendsName = langAdapter.getTypeName(extendsType);
      extendsPkg  = langAdapter.getModelPath(extendsType).replace('/', '.');
      imports.add(extendsPkg + "." + extendsName);
    }

    Map<String, ObjectProperty> extendsProps = (extendsType == null)
        ? Collections.emptyMap() : apiSpec.getResolvedProperties(extendsType);

    Map<String, ObjectProperty> allProps
        = apiSpec.getResolvedProperties(allOfDataType);

    Map<String, ObjectProperty> derivedProps = new LinkedHashMap<>(allProps);
    derivedProps.keySet().removeAll(extendsProps.keySet());

    this.populatePropertyImports(derivedProps.values(), imports, langAdapter);

    Map<String, Object> paramMap = new LinkedHashMap<>();

    String name = langAdapter.getTypeName(dataType);
    String path = langAdapter.getModelPath(dataType);
    String pkg  = path.replace('/','.');

    paramMap.put("extends", extendsName);
    paramMap.put("imports", imports);
    paramMap.put("packageName", pkg);
    paramMap.put("className", name);
    paramMap.put("fullClassName", pkg + "." + name);

    List<Map<String,String>> propertyMaps
        = this.getPropertyParams(derivedProps.values(), langAdapter);
    paramMap.put("props", propertyMaps);

    return Context.newContext(dataType).combine(paramMap);
  }

  /**
   *
   * @param dataType
   * @param langAdapter
   * @return
   */
  public List<ApiDataType> getAnonymousSubTypes(
      ApiDataType      dataType,
      LanguageAdapter  langAdapter)
  {
    ApiSpecification  apiSpec         = langAdapter.getApiSpecification();
    AllOfDataType     allOfDataType   = (AllOfDataType) dataType;
    List<ApiDataType> compositeTypes  = allOfDataType.getTypes();
    List<ApiDataType> result          = new LinkedList<>();

    ApiDataType                 extendsType   = null;
    Map<String, ObjectProperty> extendsProps  = null;
    if (compositeTypes.get(0) instanceof RefDataType) {
      extendsType   = apiSpec.resolveDataType(compositeTypes.get(0));
      extendsProps  = apiSpec.getResolvedProperties(extendsType);
    }
    if (extendsProps == null) extendsProps = Collections.emptyMap();

    Map<String, ObjectProperty> allProps
        = apiSpec.getResolvedProperties(allOfDataType);

    Map<String, ObjectProperty> derivedProps = new LinkedHashMap<>(allProps);
    derivedProps.keySet().removeAll(extendsProps.keySet());

    for (ObjectProperty prop: derivedProps.values()) {
      ApiDataType propType = prop.getDataType();
      if (!this.isAnonymousSubType(propType)) continue;
      result.add(propType);
    }

    return result;
  }

}

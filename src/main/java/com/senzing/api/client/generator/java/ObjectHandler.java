package com.senzing.api.client.generator.java;

import com.github.jknack.handlebars.Context;
import com.senzing.api.client.generator.ApiSpecification;
import com.senzing.api.client.generator.HandlebarsModelTypeHandler;
import com.senzing.api.client.generator.LanguageAdapter;
import com.senzing.api.client.generator.schema.ApiDataType;
import com.senzing.api.client.generator.schema.ObjectDataType;
import com.senzing.api.client.generator.schema.ObjectProperty;
import com.senzing.api.client.generator.schema.RefDataType;

import java.util.*;

/**
 * A handler that produces Java classes from {@link ObjectDataType} instances.
 */
public class ObjectHandler extends HandlebarsModelTypeHandler {
  /**
   * Default constructor.
   */
  public ObjectHandler() {
    super("ObjectTemplate.java.hbs");
  }

  /**
   * Implemented to <tt>true</tt> if and only if the specified {@link
   * ApiDataType} is an instance of {@link ObjectDataType}.
   *
   * @param dataType The {@link ApiDataType} to check if supported.
   * @return <tt>true</tt> if supported, otherwise <tt>false</tt>.
   */
  public boolean isSupported(ApiDataType      dataType,
                             LanguageAdapter  langAdapter)
  {
    return (dataType instanceof ObjectDataType);
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
    Context baseContext = super.createTemplateContext(dataType,
                                                      apiSpec,
                                                      langAdapter);

    ObjectDataType              objectDataType  = (ObjectDataType) dataType;
    Map<String, ObjectProperty> props           = objectDataType.getProperties();
    Set<String>                 imports         = new LinkedHashSet<>();

    this.populatePropertyImports(props.values(), imports, langAdapter);

    // sort the imports for cosmetic reasons
    imports = sortImports(imports);

    // handle the
    Map<String, Object> paramMap = new LinkedHashMap<>();

    String name = langAdapter.getTypeName(dataType);
    String pkg  = langAdapter.getModelPath(dataType).replace('/','.');

    paramMap.put("imports", imports);
    paramMap.put("className", name);
    paramMap.put("packageName", pkg);
    paramMap.put("fullClassName", pkg + "." + name);

    List<Map<String,String>> propertyMaps
        = this.getPropertyParams(props.values(), langAdapter);
    paramMap.put("props", propertyMaps);
    return Context.newContext(dataType).combine(paramMap);
  }

  /**
   *
   */
  protected List<Map<String,String>> getPropertyParams(
      Collection<ObjectProperty> props,
      LanguageAdapter            langAdapter)
  {
    List<Map<String,String>> propertyMaps = new LinkedList<>();
    for (ObjectProperty prop: props) {
      Map<String,String> propMap = new HashMap<>();
      String propName = prop.getName();
      String upperName = propName.substring(0, 1).toUpperCase() + propName.substring(1);
      ApiDataType propType = prop.getDataType();
      Set<String> typeNames = langAdapter.getNativeTypeNames(propType);
      String propTypeName = typeNames.iterator().next();

      propMap.put("name", propName);
      propMap.put("Name", upperName);
      propMap.put("type", propTypeName);
      propMap.put("description", propType.getDescription());
      propMap.put("initialValue", langAdapter.getNativeInitialValue(propType));

      propertyMaps.add(propMap);
    }
    return propertyMaps;
  }

  /**
   * Sorts the imports for cosmetic purposes.
   */
  private static Set<String> sortImports(Set<String> importSet) {
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
  protected void populatePropertyImports(Collection<ObjectProperty> props,
                                         Set<String>                imports,
                                         LanguageAdapter            langAdapter)
  {
    for (ObjectProperty prop: props) {
      ApiDataType propType = prop.getDataType();
      if (propType == null) {
        System.err.println("OBJECT PROPERTY: " + prop);
      }
      Set<String> dependencies = langAdapter.getDependencies(propType);
      imports.addAll(dependencies);
    }
  }

  /**
   *
   */
  protected boolean isAnonymousSubType(ApiDataType dataType) {
    if (dataType instanceof RefDataType) return false;
    if (dataType instanceof ObjectDataType) {
      ObjectDataType objType = (ObjectDataType) dataType;
      if (objType.getProperties().size() == 0
          && objType.getAdditionalProperties() != null) {
        // this is a basic map
        return false;
      }
    }
    return true;
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
    List<ApiDataType>           result      = new LinkedList<>();
    ObjectDataType              objDataType = (ObjectDataType) dataType;
    ApiSpecification            apiSpec     = langAdapter.getApiSpecification();
    Map<String, ObjectProperty> props       = objDataType.getProperties();

    for (ObjectProperty prop: props.values()) {
      ApiDataType propType = prop.getDataType();
      if (!this.isAnonymousSubType(propType)) continue;
      result.add(propType);
    }

    return result;
  }

}

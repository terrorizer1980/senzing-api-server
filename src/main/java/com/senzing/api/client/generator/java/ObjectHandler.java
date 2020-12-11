package com.senzing.api.client.generator.java;

import com.github.jknack.handlebars.Context;
import com.senzing.api.client.generator.ApiSpecification;
import com.senzing.api.client.generator.HandlebarsModelTypeHandler;
import com.senzing.api.client.generator.LanguageAdapter;
import com.senzing.api.client.generator.schema.*;

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
   * @param dataType    The {@link ApiDataType} to check if supported.
   * @param apiSpec     The {@link ApiSpecification} that the specified {@link
   *                    ApiDataType} is associated with.
   * @param langAdapter The {@link LanguageAdapter} to leverage.
   * @return <tt>true</tt> if supported, otherwise <tt>false</tt>.
   */
  public boolean isSupported(ApiDataType dataType,
                             ApiSpecification apiSpec,
                             LanguageAdapter langAdapter) {
    return (dataType instanceof ObjectDataType);
  }

  /**
   * Sets up the template {@link Context} to use for populating the template.
   *
   * @param dataType    The {@link ApiDataType} to produce the enum class for.
   * @param apiSpec     The {@link ApiSpecification} that the type belongs to.
   * @param langAdapter The {@link LanguageAdapter} to leverage.
   */
  protected Context createTemplateContext(ApiDataType dataType,
                                          ApiSpecification apiSpec,
                                          LanguageAdapter langAdapter) {
    Context baseContext = super.createTemplateContext(dataType,
                                                      apiSpec,
                                                      langAdapter);

    ObjectDataType objectDataType = (ObjectDataType) dataType;
    Map<String, ObjectProperty> props = objectDataType.getProperties();
    Set<String> imports = new LinkedHashSet<>();

    this.populatePropertyImports(props.values(), imports, apiSpec, langAdapter);

    // sort the imports for cosmetic reasons
    imports = JavaAdapter.sortImports(imports);

    // handle the
    Map<String, Object> paramMap = new LinkedHashMap<>();

    String name = langAdapter.getTypeName(dataType, apiSpec);
    String pkg = langAdapter.getModelSubPath(dataType, apiSpec)
        .replace('/', '.');

    paramMap.put("imports", imports);
    paramMap.put("className", name);
    paramMap.put("packageName", pkg);
    paramMap.put("fullClassName", pkg + "." + name);

    List<Map<String, String>> propertyMaps
        = this.getPropertyParams(props.values(), apiSpec, langAdapter);
    paramMap.put("props", propertyMaps);
    return Context.newContext(dataType).combine(paramMap);
  }

  /**
   * Gets the list of {@link Map} values representing the handlebars context
   * parameters describing the properties for the template.
   *
   * @param props       The {@link Collection} of {@link ObjectProperty} instances.
   * @param apiSpec     The {@link ApiSpecification} that the specified {@link
   *                    ApiDataType} is associated with.
   * @param langAdapter The {@link LanguageAdapter} to use for getting the
   *                    native type names and initial property values.
   */
  protected List<Map<String, String>> getPropertyParams(
      Collection<ObjectProperty> props,
      ApiSpecification apiSpec,
      LanguageAdapter langAdapter) {
    List<Map<String, String>> propertyMaps = new LinkedList<>();
    for (ObjectProperty prop : props) {
      Map<String, String> propMap = new HashMap<>();
      String propName = prop.getName();
      String upperName = propName.substring(0, 1).toUpperCase()
          + propName.substring(1);
      ApiDataType propType = prop.getDataType();
      Map<String, Map<String, Object>> typeNames
          = langAdapter.getNativeTypeNames(propType, apiSpec);
      String propTypeName = typeNames.keySet().iterator().next();

      propMap.put("name", propName);
      propMap.put("Name", upperName);
      propMap.put("type", propTypeName);
      propMap.put("description", propType.getDescription());
      propMap.put("initialValue",
                  langAdapter.getNativeInitialValue(propType, apiSpec));

      propertyMaps.add(propMap);
    }
    return propertyMaps;
  }

  /**
   * Adds the imports required for the specified {@link Collection} of
   * {@link ObjectProperty} to the specified {@link Set} of imports using the
   * specified {@link LanguageAdapter}.
   *
   * @param props       The {@link Collection} of {@link ObjectProperty} instances to
   *                    get the imports for.
   * @param imports     The {@link Set} to add the imports to.
   * @param apiSpec     The {@link ApiSpecification} that the specified {@link
   *                    ApiDataType} is associated with.
   * @param langAdapter The {@link LanguageAdapter} for getting the
   *                    dependencies.
   */
  protected void populatePropertyImports(Collection<ObjectProperty> props,
                                         Set<String> imports,
                                         ApiSpecification apiSpec,
                                         LanguageAdapter langAdapter) {
    for (ObjectProperty prop : props) {
      ApiDataType propType = prop.getDataType();
      Set<String> dependencies = langAdapter.getDependencies(propType, apiSpec);
      imports.addAll(dependencies);
    }
  }

  /**
   * Internal method to check if the specified {@link ApiDataType} is an
   * anonymous sub-type.  This will return <tt>false</tt> for instances of
   * {@link RefDataType} or "basic maps" (i.e.: {@link ObjectDataType} with no
   * properties, but only additional properties).
   *
   * @param dataType The {@link ApiDataType} to check.
   * @param apiSpec  The {@link ApiSpecification} that the specified {@link
   *                 ApiDataType} is associated with.
   * @return <tt>true</tt> if the specified {@link ApiDataType} qualifies as an
   * anonymous sub-type, otherwise <tt>false</tt>.
   */
  protected boolean isAnonymousSubType(ApiDataType dataType,
                                       ApiSpecification apiSpec) {
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
   * Overridden to handle getting the anonymous sub-types for an instance of
   * {@link ObjectDataType}.  If there are anonymous sub-types for an
   * {@link ObjectDataType} then they are contained in the associated
   * {@link ObjectProperty} instances.
   *
   * @param dataType    The {@link ApiDataType} for which to get the anonymous
   *                    sub-types.  This implementation requires an instance of
   *                    {@link ObjectDataType}.
   * @param apiSpec     The {@link ApiSpecification} that the specified {@link
   *                    ApiDataType} is associated with.
   * @param langAdapter The {@link JavaAdapter} to use.
   * @return The {@link List} of {@link ApiDataType} instances.
   */
  @Override
  public List<ApiDataType> getAnonymousSubTypes(
      ApiDataType dataType,
      ApiSpecification apiSpec,
      LanguageAdapter langAdapter) {
    List<ApiDataType> result = new LinkedList<>();
    ObjectDataType objDataType = (ObjectDataType) dataType;
    Map<String, ObjectProperty> props = objDataType.getProperties();

    for (ObjectProperty prop : props.values()) {
      ApiDataType propType = prop.getDataType();
      if (!this.isAnonymousSubType(propType, apiSpec)) continue;
      result.add(propType);
    }

    return result;
  }

  /**
   * Overridden to handle suppression of generating types that are basic maps.
   */
  @Override
  public void generateModelType(ApiDataType dataType,
                                ApiSpecification apiSpec,
                                LanguageAdapter langAdapter)
  {
    if (!this.isSupported(dataType, apiSpec, langAdapter)) {
      throw new IllegalArgumentException(
          "The specified data type is not supported by this handler: "
              + dataType);
    }
    if ((dataType instanceof ObjectDataType)
        && (((ObjectDataType)dataType).getProperties().size() == 0))
    {
      // do not generate classes for basic maps or basic objects
      return;
    }
    super.generateModelType(dataType, apiSpec, langAdapter);
  }
}

package com.senzing.api.client.generator.java;

import com.github.jknack.handlebars.Context;
import com.senzing.api.client.generator.ApiSpecification;
import com.senzing.api.client.generator.HandlebarsModelTypeHandler;
import com.senzing.api.client.generator.LanguageAdapter;
import com.senzing.api.client.generator.schema.ApiDataType;
import com.senzing.api.client.generator.schema.ObjectDataType;
import com.senzing.api.client.generator.schema.ObjectProperty;

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

    ObjectDataType objectDataType = (ObjectDataType) dataType;
    Map<String, ObjectProperty> props = objectDataType.getProperties();
    Set<String> imports = new LinkedHashSet<>();
    this.populatePropertyImports(props.values(), imports, langAdapter);

    Map<String, Object> paramMap = new LinkedHashMap<>();

    String name = langAdapter.getTypeName(dataType);
    String pkg  = langAdapter.getModelPath(dataType).replace('/','.');

    paramMap.put("imports", imports);
    paramMap.put("className", name);
    paramMap.put("packageName", pkg);
    paramMap.put("fullClassName", pkg + "." + name);

    return Context.newContext(dataType).combine(paramMap);
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
      String propClassName = langAdapter.getTypeName(propType);
      if (propClassName == null) continue;
      String path = langAdapter.getModelPath(propType);
      String pkg  = path.replace('/', '.');
      imports.add(pkg + "." + propClassName);
    }
  }
}

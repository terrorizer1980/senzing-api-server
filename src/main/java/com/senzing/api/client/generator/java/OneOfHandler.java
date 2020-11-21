package com.senzing.api.client.generator.java;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.URLTemplateSource;
import com.senzing.api.client.generator.ApiSpecification;
import com.senzing.api.client.generator.HandlebarsModelTypeHandler;
import com.senzing.api.client.generator.LanguageAdapter;
import com.senzing.api.client.generator.schema.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.*;
import static com.senzing.io.IOUtilities.UTF_8;

/**
 * A handler that produces one-of classes from {@link OneOfDataType} instances.
 */
public class OneOfHandler extends HandlebarsModelTypeHandler {
  /**
   * Tracks if the support files have been created for a given API spec.
   */
  private static final WeakHashMap<Integer, Boolean> SUPPORT_FILE_MAP
      = new WeakHashMap<>();

  /**
   * Default constructor.
   */
  public OneOfHandler() {
    super("OneOfTemplate.java.hbs");
  }

  /**
   * Implemented to <tt>true</tt> if and only if the specified {@link
   * ApiDataType} is an instance of {@link OneOfDataType}.
   *
   * @param dataType The {@link ApiDataType} to check if supported.
   * @param apiSpec The {@link ApiSpecification} that the specified {@link
   *                ApiDataType} is associated with.
   * @param langAdapter The {@link LanguageAdapter} to leverage.
   * @return <tt>true</tt> if supported, otherwise <tt>false</tt>.
   */
  public boolean isSupported(ApiDataType      dataType,
                             ApiSpecification apiSpec,
                             LanguageAdapter  langAdapter) {
    return (dataType instanceof OneOfDataType);
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

    OneOfDataType oneOfDataType = (OneOfDataType) dataType;
    Set<String> imports = new LinkedHashSet<>();

    // get the component types
    List<ApiDataType> componentTypes = oneOfDataType.getTypes();
    List<String> compTypeNames = new ArrayList<>(componentTypes.size());

    // loop through the component types
    for (ApiDataType compType : componentTypes) {
      // get the dependencies
      imports.addAll(langAdapter.getDependencies(compType, apiSpec));

      // get the native type names
      compTypeNames.addAll(langAdapter.getNativeTypeNames(compType, apiSpec));
    }

    // sort the imports for cosmetic reasons
    imports = JavaAdapter.sortImports(imports);

    // handle the
    Map<String, Object> paramMap = new LinkedHashMap<>();

    String pkg = langAdapter.getModelSubPath(dataType, apiSpec)
        .replace('/', '.');
    String name = langAdapter.getTypeName(dataType, apiSpec);

    paramMap.put("imports", imports);
    paramMap.put("componentTypes", compTypeNames);
    paramMap.put("className", name);
    paramMap.put("packageName", pkg);
    paramMap.put("fullClassName", pkg + "." + name);

    return Context.newContext(dataType).combine(paramMap);
  }

  /**
   * Overriden to ensure the support classes are generated before calling the
   * base class.
   *
   * @param dataType    The {@link ApiDataType} to produce the enum class for.
   * @param apiSpec The {@link ApiSpecification} that the specified {@link
   *                ApiDataType} is associated with.
   * @param langAdapter The {@link LanguageAdapter} to leverage.
   */
  @Override
  public void generateModelType(ApiDataType       dataType,
                                ApiSpecification  apiSpec,
                                LanguageAdapter   langAdapter)
  {
    if (!this.isSupported(dataType, apiSpec, langAdapter)) {
      throw new IllegalArgumentException(
          "The specified data type is not supported by this handler: "
              + dataType);
    }

    Integer identityHashCode = System.identityHashCode(apiSpec);
    synchronized (SUPPORT_FILE_MAP) {
      if (!Boolean.TRUE.equals(SUPPORT_FILE_MAP.get(identityHashCode))) {
        File file = langAdapter.getFileForModelType(dataType, apiSpec);
        File directory = file.getParentFile();
        File supportDir = new File(directory, "support");
        File wrapperFile = new File(supportDir, "Wrapper.java");
        File oneOfFile = new File(supportDir, "OneOf.java");
        if (!supportDir.exists()) supportDir.mkdirs();
        try (FileOutputStream wrapOS = new FileOutputStream(wrapperFile);
             FileOutputStream baseOS = new FileOutputStream(oneOfFile);
             OutputStreamWriter wrapW = new OutputStreamWriter(wrapOS, UTF_8);
             OutputStreamWriter baseW = new OutputStreamWriter(baseOS, UTF_8))
        {
          Class c = this.getClass();
          String wrapperPath = "OneOfWrapperTemplate.java.hbs";
          String baseClassPath = "OneOfBaseTemplate.java.hbs";
          URL wrapperUrl = c.getResource(wrapperPath);
          URL baseClassUrl = c.getResource(baseClassPath);

          URLTemplateSource wrapperSource
              = new URLTemplateSource(wrapperPath, wrapperUrl);

          URLTemplateSource baseClassSource
              = new URLTemplateSource(baseClassPath, baseClassUrl);

          String name = langAdapter.getTypeName(dataType, apiSpec);
          String path = langAdapter.getModelSubPath(dataType, apiSpec);
          String pkg  = path.replace('/', '.') + ".support";

          Context context = Context.newContext(Map.of("packageName", pkg));

          Template wrapperTemplate    = HANDLEBARS.compile(wrapperSource);
          Template baseClassTemplate  = HANDLEBARS.compile(baseClassSource);

          wrapperTemplate.apply(context, wrapW);
          baseClassTemplate.apply(context, baseW);

         SUPPORT_FILE_MAP.put(identityHashCode, Boolean.TRUE);

        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }

    // now defer to the super class
    super.generateModelType(dataType, apiSpec, langAdapter);
  }
}

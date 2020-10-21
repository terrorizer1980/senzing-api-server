package com.senzing.api.client.generator;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.URLTemplateSource;
import com.senzing.api.client.generator.schema.ApiDataType;
import com.senzing.api.client.generator.schema.EnumerationDataType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.senzing.io.IOUtilities.UTF_8;

/**
 * Provides an abstract implementation of {@link ModelTypeHandler} that
 * leverages HandleBars templates.
 */
public abstract class HandlebarsModelTypeHandler implements ModelTypeHandler {
  /**
   * The {@link Handlebars} instance to use for compiling templates.
   */
  protected static final Handlebars HANDLEBARS = new Handlebars();

  /**
   * The default template path for the handle bars template.
   */
  private String templatePath = null;

  /**
   * Default constructor.  If used then the {@link
   * #getTemplateResourcePath(ApiDataType, ApiSpecification, LanguageAdapter)}
   * method should be overridden to handle the template path.
   */
  protected HandlebarsModelTypeHandler() {
    this(null);
  }

  /**
   * Constructs with the template path that is returned from the default
   * implementation of {@link #getTemplateResourcePath(ApiDataType,
   * ApiSpecification, LanguageAdapter)}.
   *
   * @param templatePath The default template path to use.
   */
  protected HandlebarsModelTypeHandler(String templatePath) {
    this.templatePath = templatePath;
  }

  /**
   * Provides the template path for the handle bars template to use for the
   * specified data type.  The default implementation of this method returns
   * the value with which this instance was constructed, or <tt>null</tt>
   * if none was provided at construction.
   *
   * @param dataType The {@link ApiDataType} to produce the enum class for.
   * @param apiSpec The {@link ApiSpecification} that the type belongs to.
   * @param langAdapter The {@link LanguageAdapter} to leverage.
   */
  protected String getTemplateResourcePath(ApiDataType       dataType,
                                           ApiSpecification  apiSpec,
                                           LanguageAdapter   langAdapter)
  {
    return this.templatePath;
  }

  /**
   * Sets up the template {@link Context} to use for populating the template.
   * The default implementation uses the specified {@link ApiDataType} as a
   * basis and adds "className", "packageName" and "fullClassName".
   *
   * @param dataType The {@link ApiDataType} to produce the enum class for.
   * @param apiSpec The {@link ApiSpecification} that the type belongs to.
   * @param langAdapter The {@link LanguageAdapter} to leverage.
   */
  protected Context createTemplateContext(ApiDataType       dataType,
                                          ApiSpecification  apiSpec,
                                          LanguageAdapter   langAdapter)
  {
    Map<String, Object> paramMap = new LinkedHashMap<>();

    String name = langAdapter.getTypeName(dataType);
    String pkg  = langAdapter.getModelPath(dataType).replace('/','.');

    paramMap.put("className", name);
    paramMap.put("packageName", pkg);
    paramMap.put("fullClassName", pkg + "." + name);

    return Context.newContext(dataType).combine(paramMap);
  }

  /**
   * Generates a Java class from the specified {@link ApiDataType} instance
   * using a handlebars template using the specified {@link ApiSpecification}
   * and {@link LanguageAdapter}.
   *
   * @param dataType The {@link ApiDataType} to produce the enum class for.
   * @param apiSpec The {@link ApiSpecification} that the type belongs to.
   * @param langAdapter The {@link LanguageAdapter} to leverage.
   */
  public void generateModelType(ApiDataType      dataType,
                                ApiSpecification apiSpec,
                                LanguageAdapter  langAdapter)
  {
    if (!this.isSupported(dataType, langAdapter)) {
      throw new IllegalArgumentException(
          "The specified data type is not supported by this handler: "
              + dataType);
    }
    File file = langAdapter.getFileForModelType(dataType);
    try (FileOutputStream fos = new FileOutputStream(file);
         OutputStreamWriter osw = new OutputStreamWriter(fos, UTF_8))
    {
      String templatePath = this.getTemplateResourcePath(dataType,
                                                         apiSpec,
                                                         langAdapter);

      URL url = this.getClass().getResource(templatePath);

      URLTemplateSource templateSource
          = new URLTemplateSource(templatePath, url);

      Template template = HANDLEBARS.compile(templateSource);

      Context context = this.createTemplateContext(dataType,
                                                   apiSpec,
                                                   langAdapter);

      template.apply(context, osw);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

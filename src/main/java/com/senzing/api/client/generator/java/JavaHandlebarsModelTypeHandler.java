package com.senzing.api.client.generator.java;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.URLTemplateSource;
import com.senzing.api.client.generator.ApiSpecification;
import com.senzing.api.client.generator.HandlebarsModelTypeHandler;
import com.senzing.api.client.generator.LanguageAdapter;
import com.senzing.api.client.generator.ModelTypeHandler;
import com.senzing.api.client.generator.schema.ApiDataType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.senzing.io.IOUtilities.UTF_8;

/**
 * Provides an abstract implementation of {@link ModelTypeHandler} that
 * leverages HandleBars templates.
 */
public abstract class JavaHandlebarsModelTypeHandler
    extends HandlebarsModelTypeHandler
{
  /**
   * Default constructor.  If used then the {@link
   * #getTemplateResourcePath(ApiDataType, ApiSpecification, LanguageAdapter)}
   * method should be overridden to handle the template path.
   */
  protected JavaHandlebarsModelTypeHandler() {
    this(null);
  }

  /**
   * Constructs with the template path that is returned from the default
   * implementation of {@link #getTemplateResourcePath(ApiDataType,
   * ApiSpecification, LanguageAdapter)}.
   *
   * @param templatePath The default template path to use.
   */
  protected JavaHandlebarsModelTypeHandler(String templatePath) {
    super(templatePath);
  }

  /**
   * Sets up the template {@link Context} to use for populating the template.
   * This is overridden to add the "className", "packageName" and
   * "fullClassName" to the basis of the {@link ApiDataType}.
   *
   * @param dataType The {@link ApiDataType} to produce the enum class for.
   * @param apiSpec The {@link ApiSpecification} that the type belongs to.
   * @param langAdapter The {@link LanguageAdapter} to leverage.
   */
  @Override
  protected Context createTemplateContext(ApiDataType       dataType,
                                          ApiSpecification  apiSpec,
                                          LanguageAdapter   langAdapter)
  {
    Context baseContext = super.createTemplateContext(
        dataType, apiSpec, langAdapter);

    Map<String, Object> paramMap = new LinkedHashMap<>();

    String name = langAdapter.getTypeName(dataType, apiSpec);
    String pkg  = langAdapter.getModelSubPath(dataType, apiSpec)
        .replace('/','.');

    paramMap.put("className", name);
    paramMap.put("packageName", pkg);
    paramMap.put("fullClassName", pkg + "." + name);

    return Context.newContext(baseContext).combine(paramMap);
  }
}

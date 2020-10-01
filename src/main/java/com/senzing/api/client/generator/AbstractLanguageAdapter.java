package com.senzing.api.client.generator;

import com.senzing.api.client.generator.schema.ApiDataType;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractLanguageAdapter implements LanguageAdapter {
  /**
   *
   */
  protected ApiSpecification apiSpec;

  /**
   *
   */
  protected Map<Class<? extends ApiDataType>, Integer> anonymousNameCounts;

  /**
   *
   */
  protected Map<ApiDataType, String> anonymousTypeNames;

  /**
   *
   */
  protected AbstractLanguageAdapter(ApiSpecification apiSpec) {
    this.apiSpec              = apiSpec;
    this.anonymousNameCounts  = new LinkedHashMap<>();
    this.anonymousTypeNames   = new LinkedHashMap<>();
  }

  /**
   *
   */
  public ApiSpecification getApiSpecification() {
    return this.apiSpec;
  }

  /**
   *
   */
  public String getTypeName(ApiDataType dataType) {
    // resolve the data type
    dataType = this.getApiSpecification().resolveDataType(dataType);
    if (dataType == null) return null;

    // check if it not a anonymous
    String name = dataType.getName();
    if (name != null && name.trim().length() > 0) return name;

    // check if we have already generated an anonymous name for this one
    name = this.anonymousTypeNames.get(dataType);
    if (name != null) return name;

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

}

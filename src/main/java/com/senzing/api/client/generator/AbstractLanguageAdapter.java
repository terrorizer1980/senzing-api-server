package com.senzing.api.client.generator;

import com.senzing.api.client.generator.schema.AnyType;
import com.senzing.api.client.generator.schema.ApiDataType;
import com.senzing.api.client.generator.schema.ObjectDataType;

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
  protected Map<String, Map<ApiDataType, String>> typeNameMap;
  /**
   *
   */
  protected Map<ApiDataType, String> anonymousTypeNames;

  /**
   *
   */
  protected AbstractLanguageAdapter(ApiSpecification apiSpec) {
    this.apiSpec              = apiSpec;
    this.typeNameMap          = new LinkedHashMap<>();
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
    if (name != null && name.trim().length() > 0) {
      if (name.startsWith(dataType.getClass().getSimpleName())) {
        System.err.println(dataType);
      }
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

    // check if we have already generated an anonymous name for this one
    name = this.anonymousTypeNames.get(dataType);
    if (name != null) {
      if (name.startsWith(dataType.getClass().getSimpleName())) {
        System.err.println(dataType);
      }
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

    if (name.startsWith(dataType.getClass().getSimpleName())) {
      System.err.println(dataType);
    }

    // return the name
    return name;
  }

}

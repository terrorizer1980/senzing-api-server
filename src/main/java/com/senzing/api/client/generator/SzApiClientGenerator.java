package com.senzing.api.client.generator;

import javax.json.*;
import javax.json.stream.JsonParser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.senzing.api.client.generator.java.JavaAdapter;
import com.senzing.util.JsonUtils;

import java.io.File;
import java.util.Map;

public class SzApiClientGenerator {

  /**
   *
   */
  public static void main(String[] args) {
    try {
      if (args.length != 2) {
        System.err.println("Must specify the YAML OpenAPI Specification file");
        System.exit(1);
      }
      File file = new File(args[0]);
      if (!file.exists()) {
        System.err.println("The specified file does not exist: " + args[0]);
        System.exit(1);
      }
      File dir = new File(args[1]);
      if (dir.exists() && !dir.isDirectory()) {
        System.err.println("The specified output directory exists as a file: "
                               + args[1]);
        System.exit(1);
      }
      dir.mkdirs();

      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      Map map = mapper.readValue(file, Map.class);

      JsonObjectBuilder job = Json.createObjectBuilder(map);
      JsonObject jsonObject = job.build();

      //System.out.println(JsonUtils.toJsonText(jsonObject, true));

      ApiSpecification apiSpec = ApiSpecification.parse(jsonObject);

      //System.out.println();
      //System.out.println(apiSpec.toString());

      JavaAdapter javaAdapter = new JavaAdapter(
          "com/senzing/api/model", dir, apiSpec);

      javaAdapter.generateModelTypes();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

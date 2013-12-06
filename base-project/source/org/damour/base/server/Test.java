package org.damour.base.server;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;

import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;


public class Test {
  public static void main(String args[]) throws IOException {
    String username = "1140226656";
    String password = "CAACW9GjzEk4BALjOtPl5xLw111hyZAagedJI0QeGPMz3V1MDvr3r3vaPUhlZCtFYjpOwymwOmhvXtEMH1HN2MrRFsByyYuPnwI4CY0MVoXcpxF53bsVuuuZA9I9NJy1tx8pF8JdQVxXeT7ZBInfub88WOB6rIM5V4OBrlMDZBgZBgFZA9oap55xAQzZACvuuyLUZD";
    URL url = new URL("https://graph.facebook.com/" + username + "?access_token=" + password);

    JsonParserFactory factory=JsonParserFactory.getInstance();
    JSONParser parser=factory.newJsonParser();
    Map<?, ?> json=parser.parseJson(IOUtils.toString(url.openStream()));    
    
    System.out.println(json.get("id"));
    System.out.println(StringEscapeUtils.unescapeJava((String)json.get("email")));
    System.out.println(json.get("first_name"));
    System.out.println(json.get("last_name"));

  }
}
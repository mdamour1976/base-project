package org.damour.base.server.resource;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;
import org.damour.base.client.objects.SecurityPrincipal;

public class SecurityPrincipalResolver implements TypeIdResolver {

  static List<JavaType> initTypes = new ArrayList<JavaType>();

  public Id getMechanism() {
    return Id.NAME;
  }

  public String idFromValue(Object value) {
    if (value instanceof SecurityPrincipal) {
      return value.getClass().getName();
    }
    System.out.println("unknown pwn: " + value.getClass().getName());
    return "unknown";
  }

  public String idFromValueAndType(Object value, Class<?> type) {
    return idFromValue(value);
  }

  public void init(JavaType baseType) {
    if (initTypes != null) {
      initTypes.add(baseType);
    }
  }

  public JavaType typeFromId(String id) {
    System.out.println("typeFromId: " + id);
    try {
      Class<?> clazz = Class.forName(id);
      if (SecurityPrincipal.class.isAssignableFrom(clazz)) {
        return TypeFactory.defaultInstance().constructType(clazz);
      }
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

}
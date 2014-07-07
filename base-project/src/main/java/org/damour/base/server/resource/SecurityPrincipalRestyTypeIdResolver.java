package org.damour.base.server.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.damour.base.client.objects.SecurityPrincipal;
import org.fusesource.restygwt.rebind.RestyJsonTypeIdResolver;
import org.reflections.Reflections;

public class SecurityPrincipalRestyTypeIdResolver implements RestyJsonTypeIdResolver {

  private HashMap<String, Class<?>> map;

  public Class<? extends TypeIdResolver> getTypeIdResolverClass() {
    return SecurityPrincipalResolver.class;
  }

  public Map<String, Class<?>> getIdClassMap() {
    if (map == null) {
      map = new HashMap<String, Class<?>>();
      Reflections reflections = new Reflections();
      Set<Class<? extends SecurityPrincipal>> types = reflections.getSubTypesOf(SecurityPrincipal.class);
      System.out.println("Adding: " + SecurityPrincipal.class.getName());
      map.put(SecurityPrincipal.class.getName(), SecurityPrincipal.class);

      for (Class<? extends SecurityPrincipal> clazz : types) {
        System.out.println("Adding: " + clazz.getName());
        map.put(clazz.getName(), clazz);
      }
    }

    return map;
  }
}
package org.damour.base.server.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.damour.base.client.objects.PermissibleObject;
import org.fusesource.restygwt.rebind.RestyJsonTypeIdResolver;
import org.reflections.Reflections;

public class PermissibleObjectRestyTypeIdResolver implements RestyJsonTypeIdResolver {

  private HashMap<String, Class<?>> map;

  public Class<? extends TypeIdResolver> getTypeIdResolverClass() {
    return PermissibleObjectResolver.class;
  }

  public Map<String, Class<?>> getIdClassMap() {
    if (map == null) {
      map = new HashMap<String, Class<?>>();
      Reflections reflections = new Reflections();
      Set<Class<? extends PermissibleObject>> types = reflections.getSubTypesOf(PermissibleObject.class);

      for (Class<? extends PermissibleObject> clazz : types) {
        System.out.println("Adding: " + clazz.getName());
        map.put(clazz.getName(), clazz);
      }
    }

    return map;
  }
}
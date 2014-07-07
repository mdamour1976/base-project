package org.damour.base.client.objects;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.map.annotate.JsonTypeIdResolver;

@JsonTypeIdResolver(org.damour.base.server.resource.SecurityPrincipalResolver.class)
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "@type")
public class SecurityPrincipal implements Serializable {
  public Long id;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public boolean isFieldUnique(String fieldName) {
    return false;
  }

  public boolean isFieldKey(String fieldName) {
    return false;
  }

  public String getFieldType(String fieldName) {
    return null;
  }

  public int getFieldLength(String fieldName) {
    return -1;
  }

  public String getSqlUpdate() {
    return null;
  }

  public String getCachePolicy() {
    return "nonstrict-read-write";
  }

  public boolean isLazy() {
    return false;
  }

  public boolean isFieldMapped(String fieldName) {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SecurityPrincipal other = (SecurityPrincipal) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

}

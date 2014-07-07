package org.damour.base.client.objects;

import java.util.List;

public class PermissibleObjectTreeRequest {

  public PermissibleObject parent;
  public User owner;
  public List<String> acceptedClasses;
  public int fetchDepth;
  public int metaDataFetchDepth;

  public PermissibleObjectTreeRequest() {
  }

  public PermissibleObject getParent() {
    return parent;
  }

  public void setParent(PermissibleObject parent) {
    this.parent = parent;
  }

  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  public List<String> getAcceptedClasses() {
    return acceptedClasses;
  }

  public void setAcceptedClasses(List<String> acceptedClasses) {
    this.acceptedClasses = acceptedClasses;
  }

  public int getFetchDepth() {
    return fetchDepth;
  }

  public void setFetchDepth(int fetchDepth) {
    this.fetchDepth = fetchDepth;
  }

  public int getMetaDataFetchDepth() {
    return metaDataFetchDepth;
  }

  public void setMetaDataFetchDepth(int metaDataFetchDepth) {
    this.metaDataFetchDepth = metaDataFetchDepth;
  }

}
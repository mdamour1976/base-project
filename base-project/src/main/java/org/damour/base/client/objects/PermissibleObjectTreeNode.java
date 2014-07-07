package org.damour.base.client.objects;

import java.util.ArrayList;
import java.util.List;

public class PermissibleObjectTreeNode implements Comparable<PermissibleObjectTreeNode> {

  public PermissibleObject object;
  public UserRating userRating;
  public UserAdvisory userAdvisory;
  public UserThumb userThumb;

  public List<PermissibleObjectTreeNode> children = new ArrayList<PermissibleObjectTreeNode>();

  public PermissibleObjectTreeNode() {
  }

  public List<PermissibleObjectTreeNode> getChildren() {
    return children;
  }

  public void setChildren(List<PermissibleObjectTreeNode> children) {
    this.children = children;
  }

  public PermissibleObject getObject() {
    return object;
  }

  public void setObject(PermissibleObject object) {
    this.object = object;
  }

  public UserRating getUserRating() {
    return userRating;
  }

  public void setUserRating(UserRating userRating) {
    this.userRating = userRating;
  }

  public UserAdvisory getUserAdvisory() {
    return userAdvisory;
  }

  public void setUserAdvisory(UserAdvisory userAdvisory) {
    this.userAdvisory = userAdvisory;
  }

  public UserThumb getUserThumb() {
    return userThumb;
  }

  public void setUserThumb(UserThumb userThumb) {
    this.userThumb = userThumb;
  }

  public int compareTo(PermissibleObjectTreeNode o) {
    if (getObject() == null || o.getObject() == null) {
      return 1;
    }
    return getObject().compareTo(o.getObject());
  }
}

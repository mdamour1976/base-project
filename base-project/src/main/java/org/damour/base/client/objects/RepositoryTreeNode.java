package org.damour.base.client.objects;

import java.util.ArrayList;
import java.util.List;

public class RepositoryTreeNode {

  public List<RepositoryTreeNode> children = new ArrayList<RepositoryTreeNode>();
  // either a File or Folder
  public PermissibleObject file;

  public RepositoryTreeNode() {
  }

  public List<RepositoryTreeNode> getChildren() {
    return children;
  }

  public void setChildren(List<RepositoryTreeNode> children) {
    this.children = children;
  }

  public PermissibleObject getFile() {
    return file;
  }

  public void setFile(PermissibleObject file) {
    this.file = file;
  }

}

package org.damour.base.client.ui.repository;

import java.util.Comparator;

import org.damour.base.client.objects.RepositoryTreeNode;

public class FolderAlphaComparator implements Comparator<RepositoryTreeNode> {

  boolean aToZ = true;

  public FolderAlphaComparator(boolean aToZ) {
    this.aToZ = aToZ;
  }

  public int compare(RepositoryTreeNode folder1, RepositoryTreeNode folder2) {
    if (aToZ) {
      return folder1.getFile().getName().compareTo(folder2.getFile().getName());
    }
    // z to a
    return folder2.getFile().getName().compareTo(folder1.getFile().getName());
  };
}

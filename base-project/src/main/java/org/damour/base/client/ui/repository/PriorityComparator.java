package org.damour.base.client.ui.repository;

import java.util.Comparator;

import org.damour.base.client.objects.RepositoryTreeNode;

public class PriorityComparator implements Comparator<RepositoryTreeNode> {

  boolean ascending = true;

  public PriorityComparator(boolean ascending) {
    this.ascending = ascending;
  }

  public int compare(RepositoryTreeNode obj1, RepositoryTreeNode obj2) {
    Long obj1L = obj1.getFile().getSortPriority();
    Long obj2L = obj2.getFile().getSortPriority();
    if (ascending) {
      return obj1L.compareTo(obj2L);
    }
    // z to a
    return obj2L.compareTo(obj1L);
  };
}

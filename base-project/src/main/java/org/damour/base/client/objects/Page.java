package org.damour.base.client.objects;

import java.util.ArrayList;
import java.util.List;

public class Page<T> {

  public List<T> results = new ArrayList<T>();
  public int pageNumber;

  public Page() {
  }

  public Page(List<T> results, int pageNumber) {
    this.results.addAll(results);
    this.pageNumber = pageNumber;
  }

  public List<T> getResults() {
    return results;
  }

  public void setResults(List<T> results) {
    this.results = results;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(int pageNumber) {
    this.pageNumber = pageNumber;
  }

}

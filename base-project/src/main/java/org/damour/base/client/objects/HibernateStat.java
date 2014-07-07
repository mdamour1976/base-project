package org.damour.base.client.objects;


public class HibernateStat {

  public String regionName;
  public long cachePuts;
  public long cacheHits;
  public long cacheMisses;
  public long numObjectsInMemory;
  public long memoryUsed;
  public long numObjectsOnDisk;

  public HibernateStat() {
  }

  public String getRegionName() {
    return regionName;
  }

  public void setRegionName(String regionName) {
    this.regionName = regionName;
  }

  public long getCachePuts() {
    return cachePuts;
  }

  public void setCachePuts(long cachePuts) {
    this.cachePuts = cachePuts;
  }

  public long getCacheHits() {
    return cacheHits;
  }

  public void setCacheHits(long cacheHits) {
    this.cacheHits = cacheHits;
  }

  public long getCacheMisses() {
    return cacheMisses;
  }

  public void setCacheMisses(long cacheMisses) {
    this.cacheMisses = cacheMisses;
  }

  public long getNumObjectsInMemory() {
    return numObjectsInMemory;
  }

  public void setNumObjectsInMemory(long numObjectsInMemory) {
    this.numObjectsInMemory = numObjectsInMemory;
  }

  public long getMemoryUsed() {
    return memoryUsed;
  }

  public void setMemoryUsed(long memoryUsed) {
    this.memoryUsed = memoryUsed;
  }

  public long getNumObjectsOnDisk() {
    return numObjectsOnDisk;
  }

  public void setNumObjectsOnDisk(long numObjectsOnDisk) {
    this.numObjectsOnDisk = numObjectsOnDisk;
  }

}
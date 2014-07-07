package org.damour.base.client.objects;


public class MemoryStats  {

  public long uptime;
  public long startupDate;
  public long freeMemory;
  public long totalMemory;
  public long maxMemory;

  public MemoryStats() {
  }

  public long getFreeMemory() {
    return freeMemory;
  }

  public void setFreeMemory(long freeMemory) {
    this.freeMemory = freeMemory;
  }

  public long getTotalMemory() {
    return totalMemory;
  }

  public void setTotalMemory(long totalMemory) {
    this.totalMemory = totalMemory;
  }

  public long getMaxMemory() {
    return maxMemory;
  }

  public void setMaxMemory(long maxMemory) {
    this.maxMemory = maxMemory;
  }

  public long getUptime() {
    return uptime;
  }

  public void setUptime(long uptime) {
    this.uptime = uptime;
  }

  public long getStartupDate() {
    return startupDate;
  }

  public void setStartupDate(long startupDate) {
    this.startupDate = startupDate;
  }

}

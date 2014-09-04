package org.damour.base.client.objects;

public class SystemStats {

  public long uptime;
  public long startupDate;
  public long time = System.currentTimeMillis();
  public long freeMemory;
  public long totalMemory;
  public long maxMemory;
  public long totalPhysicalMemorySize;
  public long freePhysicalMemorySize;
  public long totalSwapSpaceSize;
  public long freeSwapSpaceSize;
  public long cores;
  public String arch;
  public String osName;
  public String version;
  public CpuStats cpuStats;

  public SystemStats() {
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

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public long getCores() {
    return cores;
  }

  public void setCores(long cores) {
    this.cores = cores;
  }

  public String getArch() {
    return arch;
  }

  public void setArch(String arch) {
    this.arch = arch;
  }

  public String getOsName() {
    return osName;
  }

  public void setOsName(String osName) {
    this.osName = osName;
  }

  public long getTotalPhysicalMemorySize() {
    return totalPhysicalMemorySize;
  }

  public void setTotalPhysicalMemorySize(long totalPhysicalMemorySize) {
    this.totalPhysicalMemorySize = totalPhysicalMemorySize;
  }

  public long getFreePhysicalMemorySize() {
    return freePhysicalMemorySize;
  }

  public void setFreePhysicalMemorySize(long freePhysicalMemorySize) {
    this.freePhysicalMemorySize = freePhysicalMemorySize;
  }

  public long getTotalSwapSpaceSize() {
    return totalSwapSpaceSize;
  }

  public void setTotalSwapSpaceSize(long totalSwapSpaceSize) {
    this.totalSwapSpaceSize = totalSwapSpaceSize;
  }

  public long getFreeSwapSpaceSize() {
    return freeSwapSpaceSize;
  }

  public void setFreeSwapSpaceSize(long freeSwapSpaceSize) {
    this.freeSwapSpaceSize = freeSwapSpaceSize;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public CpuStats getCpuStats() {
    return cpuStats;
  }

  public void setCpuStats(CpuStats cpuStats) {
    this.cpuStats = cpuStats;
  }
}

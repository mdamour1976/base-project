package org.damour.base.client.objects;

public class CpuStats {

  public double processCpuLoad;
  public double systemCpuLoad;
  public double systemLoadAverage;
  public double processCpuTime;
  public long time = System.currentTimeMillis();

  public CpuStats() {
  }

  public double getProcessCpuLoad() {
    return processCpuLoad;
  }

  public void setProcessCpuLoad(double processCpuLoad) {
    this.processCpuLoad = processCpuLoad;
  }

  public double getSystemCpuLoad() {
    return systemCpuLoad;
  }

  public void setSystemCpuLoad(double systemCpuLoad) {
    this.systemCpuLoad = systemCpuLoad;
  }

  public double getSystemLoadAverage() {
    return systemLoadAverage;
  }

  public void setSystemLoadAverage(double systemLoadAverage) {
    this.systemLoadAverage = systemLoadAverage;
  }

  public double getProcessCpuTime() {
    return processCpuTime;
  }

  public void setProcessCpuTime(double processCpuTime) {
    this.processCpuTime = processCpuTime;
  }
  
  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }  
}

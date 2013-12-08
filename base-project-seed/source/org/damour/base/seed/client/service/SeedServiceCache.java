package org.damour.base.seed.client.service;

import org.damour.base.client.BaseApplication;

import com.google.gwt.core.client.GWT;

public class SeedServiceCache {
  private static SeedServiceAsync service = (SeedServiceAsync) GWT.create(SeedService.class);

  public static SeedServiceAsync getServiceUnsafe() {
    return service;
  }

  public static SeedServiceAsync getService() {
    while (!BaseApplication.isInitialized()) {
      // sleep
    }
    return service;
  }
}
package org.damour.base.client.service;

import org.fusesource.restygwt.client.Defaults;

import com.google.gwt.core.client.GWT;

public class ResourceCache {
  private static BaseResource baseResource;
  private static PermissibleResource permissibleResource;
  private static CommentResource commentResource;
  
  public static BaseResource getBaseResource() {
    if (baseResource == null) {
      Defaults.setServiceRoot(GWT.getHostPageBaseURL()); // (avoid Template in the url)
      baseResource = (BaseResource) GWT.create(BaseResource.class);
    }
    return baseResource;
  }

  public static PermissibleResource getPermissibleResource() {
    if (permissibleResource == null) {
      Defaults.setServiceRoot(GWT.getHostPageBaseURL()); // (avoid Template in the url)
      permissibleResource = (PermissibleResource) GWT.create(PermissibleResource.class);
    }
    return permissibleResource;
  }

  public static CommentResource getCommentResource() {
    if (commentResource == null) {
      Defaults.setServiceRoot(GWT.getHostPageBaseURL()); // (avoid Template in the url)
      commentResource = (CommentResource) GWT.create(CommentResource.class);
    }
    return commentResource;
  }
}
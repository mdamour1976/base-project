package org.damour.base.client.service;

import org.fusesource.restygwt.client.Defaults;

import com.google.gwt.core.client.GWT;

public class ResourceCache {
  private static BaseResource baseResource;
  private static PermissibleResource permissibleResource;
  private static CommentResource commentResource;
  private static GroupResource groupResource;
  private static UserResource userResource;
  private static ReferralResource referralResource;
  private static RatingResource ratingResource;

  static {
    Defaults.setServiceRoot("/"); // (avoid Template in the url)
  }
  
  public static BaseResource getBaseResource() {
    if (baseResource == null) {
      baseResource = (BaseResource) GWT.create(BaseResource.class);
    }
    return baseResource;
  }

  public static PermissibleResource getPermissibleResource() {
    if (permissibleResource == null) {
      permissibleResource = (PermissibleResource) GWT.create(PermissibleResource.class);
    }
    return permissibleResource;
  }

  public static CommentResource getCommentResource() {
    if (commentResource == null) {
      commentResource = (CommentResource) GWT.create(CommentResource.class);
    }
    return commentResource;
  }

  public static GroupResource getGroupResource() {
    if (groupResource == null) {
      groupResource = (GroupResource) GWT.create(GroupResource.class);
    }
    return groupResource;
  }

  public static UserResource getUserResource() {
    if (userResource == null) {
      userResource = (UserResource) GWT.create(UserResource.class);
    }
    return userResource;
  }

  public static ReferralResource getReferralResource() {
    if (referralResource == null) {
      referralResource = (ReferralResource) GWT.create(ReferralResource.class);
    }
    return referralResource;
  }

  public static RatingResource getRatingResource() {
    if (ratingResource == null) {
      ratingResource = (RatingResource) GWT.create(RatingResource.class);
    }
    return ratingResource;
  }
}
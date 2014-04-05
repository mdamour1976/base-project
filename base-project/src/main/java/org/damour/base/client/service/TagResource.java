package org.damour.base.client.service;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Tag;
import org.damour.base.client.objects.TagMembership;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

@Path("rest/tag")
public interface TagResource extends RestService {

  @GET
  @Path("/list")
  void getTags(MethodCallback<List<Tag>> callback);

  @GET
  @Path("/{pid}/list")
  void getTags(@PathParam("pid") Long pid, MethodCallback<List<Tag>> callback);

  @GET
  @Path("/{tid}/tagged-objects")
  void getTaggedPermissibleObjects(@PathParam("tid") Long tid, MethodCallback<List<PermissibleObject>> callback);

  @PUT
  @Path("/{parentTagId}/create")
  void createTag(@PathParam("parentTagId") Long parentTagId, @QueryParam("tagName") String tagName, @QueryParam("tagDescription") String tagDescription,
      MethodCallback<Tag> callback);

  @DELETE
  @Path("/{tid}/delete")
  void deleteTag(@PathParam("tid") Long tid, MethodCallback<Void> callback);

  @DELETE
  @Path("/{tid}/{pid}/delete")
  void removeFromTag(@PathParam("tid") Long tid, @PathParam("pid") Long pid, MethodCallback<Void> callback);

  @DELETE
  @Path("/delete")
  void removeTagMembership(TagMembership tagMembership, MethodCallback<Void> callback);

  @PUT
  @Path("/{tid}/{pid}/add")
  void addToTag(@PathParam("tid") Long tid, @PathParam("pid") Long pid, MethodCallback<TagMembership> callback);

  @PUT
  @Path("/add")
  void addToTag(TagMembership tagMembership, MethodCallback<TagMembership> callback);
}

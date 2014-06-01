package org.damour.base.client.service;

import java.util.List;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.damour.base.client.objects.GroupMembership;
import org.damour.base.client.objects.PendingGroupMembership;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

@Path("/rest/group")
public interface GroupResource extends RestService {

  // users/group admin methods
  @PUT
  @Path("/{userId}/{groupId}/add")
  void addUserToGroup(@PathParam("userId") Long userId, @PathParam("groupId") Long groupId, MethodCallback<GroupMembership> callback);

  @PUT
  @Path("/create")
  void createOrEditGroup(UserGroup group, MethodCallback<UserGroup> callback);

  @DELETE
  @Path("/{userId}/{groupId}/delete")
  void deleteUser(@PathParam("userId") Long userId, @PathParam("groupId") Long groupId, MethodCallback<Void> callback);

  @DELETE
  @Path("/{groupId}/delete")
  public void deleteGroup(@PathParam("groupId") Long groupId, MethodCallback<Void> callback);

  @GET
  @Path("/{userId}/pending")
  void getPendingGroupMemberships(@PathParam("userId") Long userId, MethodCallback<List<PendingGroupMembership>> callback);

  @PUT
  @Path("/{userId}/{approve}/approve")
  void submitPendingGroupMembershipApproval(@PathParam("userId") Long userId, @PathParam("approve") boolean approve, Set<PendingGroupMembership> members,
      MethodCallback<List<PendingGroupMembership>> callback);

  @GET
  @Path("/{groupId}/users")
  void getUsers(@PathParam("groupId") Long groupId, MethodCallback<List<User>> callback);

  @GET
  @Path("/{username}/groups")
  void getGroups(@PathParam("username") String username, MethodCallback<List<UserGroup>> callback);

  @GET
  @Path("/list")
  void getGroups(MethodCallback<List<UserGroup>> callback);

  @GET
  @Path("/{username}/owned-groups")
  void getOwnedGroups(@PathParam("username") String username, MethodCallback<List<UserGroup>> callback);

}

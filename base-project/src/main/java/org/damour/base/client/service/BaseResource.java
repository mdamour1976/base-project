package org.damour.base.client.service;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.damour.base.client.objects.HibernateStat;
import org.damour.base.client.objects.MemoryStats;
import org.damour.base.client.objects.Referral;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

@Path("rest/base")
public interface BaseResource extends RestService {

  @GET
  @Path("hibernate/stats")
  void getHibernateStats(MethodCallback<List<HibernateStat>> callback);

  @POST
  @Path("hibernate/reset")
  void resetHibernate(MethodCallback<List<HibernateStat>> callback);

  @DELETE
  @Path("hibernate/evict/{classname}")
  void evictClassFromCache(@PathParam("classname") String className, MethodCallback<List<HibernateStat>> callback);

  @GET
  @Path("memory/stats")
  void getMemoryStats(MethodCallback<MemoryStats> callback);

  @POST
  @Path("memory/gc")
  void requestGarbageCollection(MethodCallback<MemoryStats> callback);

  @GET
  @Path("ping")
  void ping(MethodCallback<Long> callback);

  @PUT
  @Path("referral")
  void submitReferral(Referral referral, MethodCallback<Referral> callback);

  @GET
  @Path("referral/{id}")
  void getReferrals(@PathParam("id") String id, MethodCallback<List<Referral>> callback);

  @GET
  @Path("referral")
  void getReferrals(MethodCallback<List<Referral>> callback);

  @GET
  @Path("authenticated-user")
  void getAuthenticatedUser(MethodCallback<User> callback);

  @GET
  @Path("users")
  void getUsers(MethodCallback<List<User>> callback);

  @POST
  @Path("users-in-group")
  void getUsers(UserGroup group, MethodCallback<List<User>> callback);

  @GET
  @Path("user/{username}")
  void getUser(@PathParam("username") String username, MethodCallback<User> callback);

  @GET
  @Path("groups/{username}")
  void getGroups(@PathParam("username") String username, MethodCallback<List<UserGroup>> callback);

  @GET
  @Path("groups")
  void getGroups(MethodCallback<List<UserGroup>> callback);

  @GET
  @Path("owned-groups/{username}")
  void getOwnedGroups(@PathParam("username") String username, MethodCallback<List<UserGroup>> callback);

}
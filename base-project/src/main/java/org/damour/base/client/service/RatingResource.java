package org.damour.base.client.service;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.UserAdvisory;
import org.damour.base.client.objects.UserRating;
import org.damour.base.client.objects.UserThumb;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

@Path("/rest/rate")
public interface RatingResource extends RestService {

  @GET
  @Path("/{id}/rating")
  void getUserRating(@PathParam("id") Long id, MethodCallback<UserRating> callback);

  @PUT
  @Path("/{id}/rating")
  void setUserRating(@PathParam("id") Long id, Integer rating, MethodCallback<UserRating> callback);

  @GET
  @Path("/{id}/{objectType}/next")
  void getNextUnratedPermissibleObject(@PathParam("id") Long id, @PathParam("objectType") String objectType, MethodCallback<PermissibleObject> callback);

  @GET
  @Path("/{id}/advisory")
  void getUserAdvisory(@PathParam("id") Long id, MethodCallback<UserAdvisory> callback);

  @PUT
  @Path("/{id}/advisory")
  void setUserAdvisory(@PathParam("id") Long id, Integer advisory, MethodCallback<UserAdvisory> callback);

  @GET
  @Path("/{id}/thumb")
  void getUserThumb(@PathParam("id") Long id, MethodCallback<UserThumb> callback);

  @PUT
  @Path("/{id}/thumb")
  void setUserThumb(@PathParam("id") Long id, Boolean like, MethodCallback<UserThumb> callback);

  // top rated/most liked api
  @GET
  @Path("/mostRated/{classType}/{maxResults}")
  void getMostRated(@PathParam("classType") String classType, @PathParam("maxResults") int maxResults, MethodCallback<List<PermissibleObject>> callback);

  @GET
  @Path("/top/{classType}/{minNumVotes}/{maxResults}")
  void getTopRated(@PathParam("classType") String classType, @PathParam("minNumVotes") int minNumVotes, @PathParam("maxResults") int maxResults,
      MethodCallback<List<PermissibleObject>> callback);

  @GET
  @Path("/bottom/{classType}/{minNumVotes}/{maxResults}")
  void getBottomRated(@PathParam("classType") String classType, @PathParam("minNumVotes") int minNumVotes, @PathParam("maxResults") int maxResults,
      MethodCallback<List<PermissibleObject>> callback);

  @GET
  @Path("/mostLiked/{classType}/{minNumVotes}/{maxResults}")
  void getMostLiked(@PathParam("classType") String classType, @PathParam("minNumVotes") int minNumVotes, @PathParam("maxResults") int maxResults,
      MethodCallback<List<PermissibleObject>> callback);

  @GET
  @Path("/mostDisliked/{classType}/{minNumVotes}/{maxResults}")
  void getMostDisliked(@PathParam("classType") String classType, @PathParam("minNumVotes") int minNumVotes, @PathParam("maxResults") int maxResults,
      MethodCallback<List<PermissibleObject>> callback);

  @GET
  @Path("/since/{classType}/{createdSinceMillis}/{maxResults}")
  void getCreatedSince(@PathParam("classType") String classType, @PathParam("createdSinceMillis") long createdSinceMillis,
      @PathParam("maxResults") int maxResults, MethodCallback<List<PermissibleObject>> callback);

}

package org.damour.base.client.service;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.damour.base.client.objects.StringWrapper;
import org.damour.base.client.objects.User;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

@Path("rest/user")
public interface UserResource extends RestService {

  @POST
  @Path("/account")
  void createOrEditAccount(User user, @QueryParam("password") String password, @QueryParam("captchText") String captchaText, MethodCallback<User> callback);

  @POST
  @Path("/login")
  void login(@QueryParam("username") String username, @QueryParam("password") String password, @QueryParam("facebook") Boolean facebook,
      MethodCallback<User> callback);

  @GET
  @Path("/logout")
  void logout(MethodCallback<Boolean> callback);

  @GET
  @Path("/hint")
  void getLoginHint(@QueryParam("username") String username, MethodCallback<StringWrapper> callback);

  @GET
  @Path("/validate")
  void submitAccountValidation(@QueryParam("username") String username, @QueryParam("validationCode") String validationCode, MethodCallback<User> callback);

  @GET
  @Path("/authenticated-user")
  void getAuthenticatedUser(MethodCallback<User> callback);

  @GET
  @Path("/list")
  void getUsers(MethodCallback<List<User>> callback);

  @GET
  @Path("/{username}")
  void getUser(@PathParam("username") String username, MethodCallback<User> callback);

}
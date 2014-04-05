package org.damour.base.client.service;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.damour.base.client.objects.Comment;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

@Path("rest/comment")
public interface CommentResource extends RestService {

  @POST
  @Path("/create")
  public void submitComment(Comment comment, MethodCallback<Boolean> callback);

  @GET
  @Path("/{commentId}/approve")
  public void approveComment(@PathParam("commentId") Long commentId, MethodCallback<Boolean> callback);

  @DELETE
  @Path("/{commentId}/remove")
  public void deleteComment(@PathParam("commentId") Long commentId, MethodCallback<Boolean> callback);

}
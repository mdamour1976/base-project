package org.damour.base.client.service;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.damour.base.client.objects.Comment;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

@Path("rest/comment")
public interface CommentResource extends RestService {

  @POST
  public void submitComment(Comment comment, MethodCallback<Boolean> callback);

  @POST
  @Path("approve")
  public void approveComment(Comment comment, MethodCallback<Boolean> callback);

  @DELETE
  public void deleteComment(Comment comment, MethodCallback<Boolean> callback);

}
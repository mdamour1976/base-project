package org.damour.base.server.resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.damour.base.client.exceptions.SimpleMessageException;
import org.damour.base.client.objects.Comment;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Permission.PERM;
import org.damour.base.client.objects.User;
import org.damour.base.server.Logger;
import org.damour.base.server.hibernate.HibernateUtil;
import org.damour.base.server.hibernate.helpers.CommentHelper;
import org.damour.base.server.hibernate.helpers.SecurityHelper;
import org.hibernate.Session;
import org.hibernate.Transaction;

@Path("/comment")
public class CommentResource {

  @POST
  @Path("/create")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Comment submitComment(Comment comment, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();

      User authUser = null;
      try {
        authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      } catch (WebApplicationException wa) {
      }
      if (comment == null) {
        Exception t = new SimpleMessageException("Comment not supplied.");
        throw new WebApplicationException(t, Response.Status.NOT_FOUND);
      }
      if (comment.getParent() == null) {
        Exception t = new SimpleMessageException("PermissibleObject not supplied with comment.");
        throw new WebApplicationException(t, Response.Status.NOT_FOUND);
      }
      Transaction tx = session.beginTransaction();
      try {
        PermissibleObject parentPermissibleObject = (PermissibleObject) session.load(PermissibleObject.class, comment.getParent().getId());
        parentPermissibleObject.setNumComments(parentPermissibleObject.getNumComments() + 1);
        comment.setParent(parentPermissibleObject);
        if (!SecurityHelper.doesUserHavePermission(session, authUser, comment.getParent(), PERM.READ)) {
          Exception t = new SimpleMessageException("User is not authorized to make comments on this content.");
          throw new WebApplicationException(t, Response.Status.UNAUTHORIZED);
        }
        if (!comment.getParent().isAllowComments()) {
          Exception t = new SimpleMessageException("Comments are not allowed on this content.");
          throw new WebApplicationException(t, Response.Status.UNAUTHORIZED);
        }
        // the comment is approved if we are not moderating or if the commenter is the file owner
        comment.setApproved(!comment.getParent().isModerateComments() || comment.getParent().getOwner().equals(authUser));
        comment.setAuthorIP(httpRequest.getRemoteAddr());
        session.save(comment);
        session.save(parentPermissibleObject);
        tx.commit();
        return comment;
      } catch (Throwable t) {
        Logger.log(t);
        try {
          tx.rollback();
        } catch (Throwable tt) {
        }
        throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
      }
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/{commentId}/approve")
  @Produces(MediaType.APPLICATION_JSON)
  public Comment approveComment(@PathParam("commentId") Long commentId, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse)
      throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();

      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      if (commentId == null) {
        Exception t = new SimpleMessageException("Comment not supplied.");
        throw new WebApplicationException(t, Response.Status.NOT_FOUND);
      }
      if (authUser == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      Transaction tx = session.beginTransaction();
      try {
        Comment comment = ((Comment) session.load(Comment.class, commentId));
        if (!SecurityHelper.doesUserHavePermission(session, authUser, comment.getParent(), PERM.WRITE)) {
          throw new SimpleMessageException("User is not authorized to approve comments for this content.");
        }
        comment.setApproved(true);
        session.save(comment);
        tx.commit();
        return comment;
      } catch (Throwable t) {
        Logger.log(t);
        try {
          tx.rollback();
        } catch (Throwable tt) {
        }
        throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
      }
    } finally {
      session.close();
    }
  }

  @DELETE
  @Path("/{commentId}/remove")
  @Produces(MediaType.APPLICATION_JSON)
  public Boolean deleteComment(@PathParam("commentId") Long commentId, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse)
      throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      if (commentId == null) {
        throw new SimpleMessageException("Comment not supplied.");
      }
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser == null) {
        throw new SimpleMessageException("User is not authenticated.");
      }
      Transaction tx = session.beginTransaction();
      try {
        Comment comment = ((Comment) session.load(Comment.class, commentId));
        boolean isAuthor = comment.getAuthor() != null && comment.getAuthor().equals(authUser);
        if (!isAuthor && !SecurityHelper.doesUserHavePermission(session, authUser, comment.getParent(), PERM.WRITE)) {
          throw new SimpleMessageException("User is not authorized to delete comments for this content.");
        }
        // we can't delete this comment until we delete all the child comments
        CommentHelper.deleteComment(session, comment);
        tx.commit();
        return true;
      } catch (Throwable t) {
        Logger.log(t);
        try {
          tx.rollback();
        } catch (Throwable tt) {
        }
      }
    } finally {
      session.close();
    }
    return false;
  }
}
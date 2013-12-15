package org.damour.base.server.resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.damour.base.client.exceptions.SimpleMessageException;
import org.damour.base.client.objects.Comment;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Permission.PERM;
import org.damour.base.client.objects.User;
import org.damour.base.server.BaseService;
import org.damour.base.server.Logger;
import org.damour.base.server.hibernate.HibernateUtil;
import org.damour.base.server.hibernate.helpers.CommentHelper;
import org.damour.base.server.hibernate.helpers.SecurityHelper;
import org.hibernate.Session;
import org.hibernate.Transaction;

@Path("/comment")
public class CommentResource {

  @POST
  public Boolean submitComment(Comment comment, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse)
      throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();

      User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
      if (comment == null) {
        throw new SimpleMessageException("Comment not supplied.");
      }
      if (comment.getParent() == null) {
        throw new SimpleMessageException("PermissibleObject not supplied with comment.");
      }
      Transaction tx = session.beginTransaction();
      try {
        PermissibleObject parentPermissibleObject = (PermissibleObject) session.load(PermissibleObject.class, comment.getParent().getId());
        parentPermissibleObject.setNumComments(parentPermissibleObject.getNumComments() + 1);
        comment.setParent(parentPermissibleObject);
        if (!SecurityHelper.doesUserHavePermission(session, authUser, comment.getParent(), PERM.READ)) {
          throw new SimpleMessageException("User is not authorized to make comments on this content.");
        }
        if (!comment.getParent().isAllowComments()) {
          throw new SimpleMessageException("Comments are not allowed on this content.");
        }
        // the comment is approved if we are not moderating or if the commenter is the file owner
        comment.setApproved(!comment.getParent().isModerateComments() || comment.getParent().getOwner().equals(authUser));
        comment.setAuthorIP(httpRequest.getRemoteAddr());
        session.save(comment);
        session.save(parentPermissibleObject);
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

  @POST
  @Path("approve")
  public Boolean approveComment(Comment comment, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse)
      throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();

      User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
      if (comment == null) {
        throw new SimpleMessageException("Comment not supplied.");
      }
      if (authUser == null) {
        throw new SimpleMessageException(".");
      }
      Transaction tx = session.beginTransaction();
      try {
        comment = ((Comment) session.load(Comment.class, comment.getId()));
        if (!SecurityHelper.doesUserHavePermission(session, authUser, comment.getParent(), PERM.WRITE)) {
          throw new SimpleMessageException("User is not authorized to approve comments for this content.");
        }
        comment.setApproved(true);
        session.save(comment);
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

  @DELETE
  public Boolean deleteComment(Comment comment, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse)
      throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      if (comment == null) {
        throw new SimpleMessageException("Comment not supplied.");
      }
      User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser == null) {
        throw new SimpleMessageException("User is not authenticated.");
      }
      Transaction tx = session.beginTransaction();
      try {
        comment = ((Comment) session.load(Comment.class, comment.getId()));
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
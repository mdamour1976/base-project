package org.damour.base.server.resource;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Tag;
import org.damour.base.client.objects.TagMembership;
import org.damour.base.client.objects.User;
import org.damour.base.client.utils.StringUtils;
import org.damour.base.server.Logger;
import org.damour.base.server.hibernate.HibernateUtil;
import org.damour.base.server.hibernate.helpers.TagHelper;
import org.hibernate.Session;
import org.hibernate.Transaction;

@Path("/tag")
public class TagResource {

  @GET
  @Path("/{tid}/tagged-objects")
  @Produces(MediaType.APPLICATION_JSON)
  public List<PermissibleObject> getTaggedPermissibleObjects(@PathParam("tid") Long tid) {
    // TODO Auto-generated method stub
    return null;
  }

  @PUT
  @Path("/{parentTagId}/create")
  @Produces(MediaType.APPLICATION_JSON)
  public void createTag(@PathParam("parentTagId") Long parentTagId, @QueryParam("tagName") String tagName, @QueryParam("tagDescription") String tagDescription,
      @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = null;
    Transaction tx = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }

      if (StringUtils.isEmpty(tagName)) {
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
      }

      Tag hibParentTag = null;
      if (parentTagId != null) {
        hibParentTag = ((Tag) session.load(Tag.class, parentTagId));
      }

      tx = session.beginTransaction();
      Tag tag = new Tag();
      tag.setName(tagName);
      tag.setDescription(tagDescription);
      tag.setParentTag(hibParentTag);
      session.save(tag);
      tx.commit();
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      session.close();
    }
  }

  @DELETE
  @Path("/{tid}/delete")
  public void deleteTag(@PathParam("tid") Long tid, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = null;
    Transaction tx = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }

      if (tid == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }

      Tag hibTag = ((Tag) session.load(Tag.class, tid));
      if (hibTag == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }

      tx = session.beginTransaction();
      TagHelper.deleteTag(session, hibTag);
      tx.commit();
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/list")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Tag> getTags(@Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    // anyone can get tags
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      return TagHelper.getTags(session);
    } finally {
      session.close();
    }
  }

  @PUT
  @Path("/{tid}/{pid}/add")
  @Produces(MediaType.APPLICATION_JSON)
  public TagMembership addToTag(@PathParam("tid") Long tid, @PathParam("pid") Long pid, @Context HttpServletRequest httpRequest,
      @Context HttpServletResponse httpResponse) {
    if (tid == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    if (pid == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Session session = null;
    Transaction tx = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }

      // assumption is that the membership does not exist but the category / permissible object do
      // they must be loaded
      Tag hibTag = ((Tag) session.load(Tag.class, tid));
      if (hibTag == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }

      PermissibleObject hibPermissibleObject = ((PermissibleObject) session.load(PermissibleObject.class, pid));
      if (hibPermissibleObject == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }

      tx = session.beginTransaction();
      TagMembership tagMembership = new TagMembership();
      tagMembership.setTag(hibTag);
      tagMembership.setPermissibleObject(hibPermissibleObject);
      session.save(tagMembership);
      tx.commit();
      return tagMembership;
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      session.close();
    }
  }

  @PUT
  @Path("/add")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public TagMembership addToTag(final TagMembership tagMembership, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    return addToTag(tagMembership.getTag().getId(), tagMembership.getPermissibleObject().getId(), httpRequest, httpResponse);
  }

  @GET
  @Path("/{pid}/list")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Tag> getTags(@PathParam("pid") Long pid, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (pid == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      PermissibleObject permissibleObject = ((PermissibleObject) session.load(PermissibleObject.class, pid));
      if (permissibleObject == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      return TagHelper.getTags(session, permissibleObject);
    } finally {
      session.close();
    }
  }

  @DELETE
  @Path("/delete")
  @Consumes(MediaType.APPLICATION_JSON)
  public void removeTagMembership(final TagMembership tagMembership, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    removeFromTag(tagMembership.getTag().getId(), tagMembership.getPermissibleObject().getId(), httpRequest, httpResponse);
  }

  @DELETE
  @Path("/{tid}/{pid}/delete")
  public void removeFromTag(@PathParam("tid") Long tid, @PathParam("pid") Long pid, @Context HttpServletRequest httpRequest,
      @Context HttpServletResponse httpResponse) {
    Session session = null;
    Transaction tx = null;

    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }

      if (tid == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }

      if (pid == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }

      Tag hibTag = ((Tag) session.load(Tag.class, tid));
      if (hibTag == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }

      PermissibleObject hibPermissibleObject = ((PermissibleObject) session.load(PermissibleObject.class, pid));
      if (hibPermissibleObject == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }

      tx = session.beginTransaction();
      TagMembership cm = TagHelper.getTagMembership(session, hibTag, hibPermissibleObject);
      session.delete(cm);
      tx.commit();
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      session.close();
    }
  }

}

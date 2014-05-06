package org.damour.base.server.resource;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.damour.base.client.objects.Page;
import org.damour.base.client.objects.PageInfo;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.PermissibleObjectTreeNode;
import org.damour.base.client.objects.PermissibleObjectTreeRequest;
import org.damour.base.client.objects.Permission.PERM;
import org.damour.base.client.objects.RepositoryTreeNode;
import org.damour.base.client.objects.User;
import org.damour.base.server.Logger;
import org.damour.base.server.hibernate.HibernateUtil;
import org.damour.base.server.hibernate.helpers.PageHelper;
import org.damour.base.server.hibernate.helpers.RatingHelper;
import org.damour.base.server.hibernate.helpers.RepositoryHelper;
import org.damour.base.server.hibernate.helpers.SecurityHelper;
import org.hibernate.Session;
import org.hibernate.Transaction;

@Path("/objects")
public class PermissibleResource {

  @GET
  @Path("/{id : .+}")
  @Produces(MediaType.APPLICATION_JSON)
  public PermissibleObject getPermissibleObject(@PathParam("id") Long id, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      if (id == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      User authUser = null;

      try {
        authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      } catch (Throwable t) {
      }
      try {
        PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);
        if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
          throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return permissibleObject;
      } catch (Throwable t) {
        Logger.log(t);
        throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
      }
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/{id}/children/{objectType}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<PermissibleObject> getPermissibleObject(@PathParam("id") Long id, @PathParam("objectType") String objectType,
      @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      if (id == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      User authUser = null;

      try {
        authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      } catch (Throwable t) {
      }
      try {
        PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);
        if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
          throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        ArrayList<PermissibleObject> objects = new ArrayList<PermissibleObject>();
        Class<?> clazz = Class.forName(objectType);
        RepositoryHelper.getPermissibleObjects(session, authUser, objects, permissibleObject, clazz);
        return objects;

      } catch (Throwable t) {
        Logger.log(t);
        throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
      }
    } finally {
      session.close();
    }
  }

  @POST
  @Path("/tree")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public PermissibleObjectTreeNode getPermissibleObjectTree(PermissibleObjectTreeRequest request, @Context HttpServletRequest httpRequest,
      @Context HttpServletResponse httpResponse) {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = null;

      try {
        authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      } catch (Throwable t) {
      }
      PermissibleObjectTreeNode root = new PermissibleObjectTreeNode();
      PermissibleObject parent = request.getParent();
      if (parent != null) {
        parent = getPermissibleObject(parent.getId(), httpRequest, httpResponse);
      }
      User owner = request.getOwner();
      if (owner == null || owner.getId() == null) {
        owner = null;
      }
      RepositoryHelper.buildPermissibleObjectTreeNode(session, authUser, owner, RatingHelper.getVoterGUID(httpRequest, httpResponse), root, parent,
          request.getAcceptedClasses(), 0, request.getFetchDepth(), request.getMetaDataFetchDepth());
      return root;
    } finally {
      session.close();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/page/{id}/{pageClassType}/{sortField}/{sortDescending}/{pageNumber}/{pageSize}")
  public Page<PermissibleObject> getPage(@PathParam("id") Long id, @PathParam("pageClassType") String pageClassType, @PathParam("sortField") String sortField,
      @PathParam("sortDescending") boolean sortDescending, @PathParam("pageNumber") int pageNumber, @PathParam("pageSize") int pageSize,
      @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = null;

      try {
        authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      } catch (Throwable t) {
      }
      try {
        Class<?> clazz = Class.forName(pageClassType);
        PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);
        if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
          throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return PageHelper.getPage(session, permissibleObject, clazz, authUser, sortField, sortDescending, pageNumber, pageSize);
      } catch (Throwable t) {
        Logger.log(t);
        throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
      }
    } finally {
      session.close();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/page/{id}/{pageClassType}/{pageSize}")
  public PageInfo getPageInfo(@PathParam("id") Long id, @PathParam("pageClassType") String pageClassType, @PathParam("pageSize") int pageSize,
      @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      try {
        User authUser = null;

        try {
          authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
        } catch (Throwable t) {
        }
        PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);
        if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
          throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        Class<?> clazz = Class.forName(pageClassType);
        return PageHelper.getPageInfo(session, permissibleObject, clazz, authUser, pageSize);
      } catch (Throwable t) {
        Logger.log(t);
        throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
      }
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/filetree")
  @Produces(MediaType.APPLICATION_JSON)
  public RepositoryTreeNode getRepositoryTree(@Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = null;

      try {
        authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      } catch (Throwable t) {
      }
      RepositoryTreeNode root = new RepositoryTreeNode();
      RepositoryHelper.buildRepositoryTreeNode(session, authUser, root, null);
      return root;
    } catch (Throwable t) {
      Logger.log(t);
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/echo")
  @Produces(MediaType.APPLICATION_JSON)
  public PermissibleObject echoPermissibleObject(PermissibleObject object, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    // for debug purposes: simply return what was given, proving the serialization of the desired object
    return object;
  }

  @GET
  @Path("/counter")
  @Produces(MediaType.APPLICATION_JSON)
  public Long getCustomCounter1(@PathParam("id") Long id, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (id == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);
      return permissibleObject.getCustomCounter1();
    } catch (Throwable t) {
      Logger.log(t);
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      session.close();
    }
  }

  @POST
  @Path("/counter")
  @Produces(MediaType.APPLICATION_JSON)
  public Long incrementCustomCounter1(@PathParam("id") Long id, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (id == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Transaction tx = null;
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      tx = session.beginTransaction();
      PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);
      permissibleObject.setCustomCounter1(permissibleObject.getCustomCounter1() + 1);
      session.save(permissibleObject);
      tx.commit();
      return permissibleObject.getCustomCounter1();
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
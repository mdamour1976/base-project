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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.damour.base.client.exceptions.SimpleMessageException;
import org.damour.base.client.objects.Page;
import org.damour.base.client.objects.PageInfo;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.PermissibleObjectTreeNode;
import org.damour.base.client.objects.PermissibleObjectTreeRequest;
import org.damour.base.client.objects.Permission.PERM;
import org.damour.base.client.objects.User;
import org.damour.base.server.BaseService;
import org.damour.base.server.Logger;
import org.damour.base.server.hibernate.HibernateUtil;
import org.damour.base.server.hibernate.helpers.PageHelper;
import org.damour.base.server.hibernate.helpers.RepositoryHelper;
import org.damour.base.server.hibernate.helpers.SecurityHelper;
import org.hibernate.Session;

@Path("/objects")
public class PermissibleResource {

  @GET
  @Path("/{id : .+}")
  @Produces(MediaType.APPLICATION_JSON)
  public PermissibleObject getPermissibleObject(@PathParam("id") Long id, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse)
      throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      if (id == null) {
        throw new SimpleMessageException("Id not supplied.");
      }
      User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
      try {
        PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);
        if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
          throw new SimpleMessageException("User is not authorized to get this content.");
        }
        return permissibleObject;
      } catch (Throwable t) {
        Logger.log(t);
        throw new SimpleMessageException(t.getMessage());
      }
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/{id}/children/{objectType}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<PermissibleObject> getPermissibleObject(@PathParam("id") Long id, @PathParam("objectType") String objectType,
      @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      if (id == null) {
        throw new SimpleMessageException("Id not supplied.");
      }
      User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
      try {
        PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);
        if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
          throw new SimpleMessageException("User is not authorized to get this content.");
        }

        ArrayList<PermissibleObject> objects = new ArrayList<PermissibleObject>();
        Class<?> clazz = Class.forName(objectType);
        RepositoryHelper.getPermissibleObjects(session, authUser, objects, permissibleObject, clazz);
        return objects;

      } catch (Throwable t) {
        Logger.log(t);
        throw new SimpleMessageException(t.getMessage());
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
      @Context HttpServletResponse httpResponse) throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
      PermissibleObjectTreeNode root = new PermissibleObjectTreeNode();
      PermissibleObject parent = request.getParent();
      if (parent != null) {
        parent = getPermissibleObject(parent.getId(), httpRequest, httpResponse);
      }
      User owner = request.getOwner();
      if (owner == null || owner.getId() == null) {
        owner = null;
      }
      RepositoryHelper.buildPermissibleObjectTreeNode(session, authUser, owner, BaseService.getVoterGUID(httpRequest, httpResponse), root, parent,
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
      @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
      try {
        Class<?> clazz = Class.forName(pageClassType);
        PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);
        if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
          throw new SimpleMessageException("User is not authorized to get this content.");
        }
        return PageHelper.getPage(session, permissibleObject, clazz, authUser, sortField, sortDescending, pageNumber, pageSize);
      } catch (Throwable t) {
        Logger.log(t);
        throw new SimpleMessageException(t.getMessage());
      }
    } finally {
      session.close();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/page/{id}/{pageClassType}/{pageSize}")
  public PageInfo getPageInfo(@PathParam("id") Long id, @PathParam("pageClassType") String pageClassType, @PathParam("pageSize") int pageSize,
      @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      try {
        User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
        PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);
        if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
          throw new SimpleMessageException("User is not authorized to get this content.");
        }
        Class<?> clazz = Class.forName(pageClassType);
        return PageHelper.getPageInfo(session, permissibleObject, clazz, authUser, pageSize);
      } catch (Throwable t) {
        Logger.log(t);
        throw new SimpleMessageException(t.getMessage());
      }
    } finally {
      session.close();
    }
  }

}
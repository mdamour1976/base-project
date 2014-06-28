package org.damour.base.server.resource;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.IAnonymousPermissibleObject;
import org.damour.base.client.objects.Page;
import org.damour.base.client.objects.PageInfo;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.PermissibleObjectTreeNode;
import org.damour.base.client.objects.PermissibleObjectTreeRequest;
import org.damour.base.client.objects.Permission.PERM;
import org.damour.base.client.objects.RepositoryTreeNode;
import org.damour.base.client.objects.User;
import org.damour.base.client.utils.StringUtils;
import org.damour.base.server.Logger;
import org.damour.base.server.hibernate.HibernateUtil;
import org.damour.base.server.hibernate.ReflectionCache;
import org.damour.base.server.hibernate.helpers.FolderHelper;
import org.damour.base.server.hibernate.helpers.PageHelper;
import org.damour.base.server.hibernate.helpers.PermissibleObjectHelper;
import org.damour.base.server.hibernate.helpers.RatingHelper;
import org.damour.base.server.hibernate.helpers.RepositoryHelper;
import org.damour.base.server.hibernate.helpers.SecurityHelper;
import org.damour.base.server.hibernate.helpers.UserHelper;
import org.hibernate.Session;
import org.hibernate.Transaction;

@Path("/objects")
public class PermissibleResource {

  @PUT
  @Path("/save")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public PermissibleObject savePermissibleObject(PermissibleObject permissibleObject, @Context HttpServletRequest httpRequest,
      @Context HttpServletResponse httpResponse) {
    if (permissibleObject == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Transaction tx = null;
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      tx = session.beginTransaction();

      User authUser = null;
      try {
        authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      } catch (Throwable t) {
        if (permissibleObject instanceof IAnonymousPermissibleObject) {
          authUser = UserHelper.getUser(session, "anonymous");
        }
      }
      if (authUser == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }

      if (permissibleObject.getParent() != null) {
        permissibleObject.setParent((PermissibleObject) session.load(PermissibleObject.class, permissibleObject.getParent().getId()));
      }
      if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject.getParent(), PERM.CREATE_CHILD)) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      if (permissibleObject.getId() != null) {
        PermissibleObject hibNewObject = (PermissibleObject) session.load(PermissibleObject.class, permissibleObject.getId());
        if (hibNewObject != null) {
          if (!SecurityHelper.doesUserHavePermission(session, authUser, hibNewObject, PERM.WRITE)) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
          }
          List<Field> fields = ReflectionCache.getFields(permissibleObject.getClass());
          for (Field field : fields) {
            try {
              field.set(hibNewObject, field.get(permissibleObject));
            } catch (Exception e) {
              e.printStackTrace();
              Logger.log(e);
            }
          }

          permissibleObject = hibNewObject;
        }
      }

      List<Field> fields = ReflectionCache.getFields(permissibleObject.getClass());
      for (Field field : fields) {
        try {
          // do not update parent permission only our 'owned' objects
          if (!"parent".equals(field.getName())) {
            Object obj = field.get(permissibleObject);
            if (obj instanceof PermissibleObject) {
              PermissibleObject childObj = (PermissibleObject) obj;
              PermissibleObject hibChild = (PermissibleObject) session.load(PermissibleObject.class, childObj.getId());
              hibChild.setGlobalRead(permissibleObject.isGlobalRead());
              hibChild.setGlobalWrite(permissibleObject.isGlobalWrite());
              hibChild.setGlobalExecute(permissibleObject.isGlobalExecute());
              hibChild.setGlobalCreateChild(permissibleObject.isGlobalCreateChild());
              field.set(permissibleObject, hibChild);
            }
          }
        } catch (Exception e) {
          Logger.log(e);
        }
      }

      permissibleObject.setOwner(authUser);
      session.save(permissibleObject);
      tx.commit();
      return permissibleObject;
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
  @Path("/saveList")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public List<PermissibleObject> savePermissibleObjects(List<PermissibleObject> permissibleObjects, @Context HttpServletRequest httpRequest,
      @Context HttpServletResponse httpResponse) {
    for (PermissibleObject object : permissibleObjects) {
      savePermissibleObject(object, httpRequest, httpResponse);
    }
    return permissibleObjects;
  }

  @PUT
  @Path("/update")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public PermissibleObject updatePermissibleObject(PermissibleObject permissibleObject, @Context HttpServletRequest httpRequest,
      @Context HttpServletResponse httpResponse) {
    if (permissibleObject == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Transaction tx = null;
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      tx = session.beginTransaction();
      User newOwner = ((User) session.load(User.class, permissibleObject.getOwner().getId()));

      User authUser = null;
      try {
        authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      } catch (Throwable t) {
        if (permissibleObject instanceof IAnonymousPermissibleObject) {
          authUser = UserHelper.getUser(session, "anonymous");
        }
      }
      if (authUser == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }

      PermissibleObject hibPermissibleObject = ((PermissibleObject) session.load(PermissibleObject.class, permissibleObject.getId()));
      if (!authUser.isAdministrator() && !hibPermissibleObject.getOwner().equals(authUser)) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      // update fields (for example, image has child permissibles)
      List<Field> fields = ReflectionCache.getFields(hibPermissibleObject.getClass());
      for (Field field : fields) {
        try {
          if (!field.getName().equals("parent") && PermissibleObject.class.isAssignableFrom(field.getType())) {
            Object obj = field.get(hibPermissibleObject);
            if (obj == null) {
              field.set(hibPermissibleObject, field.get(permissibleObject));
              obj = field.get(hibPermissibleObject);
              if (obj != null) {
                PermissibleObject hibSubObj = ((PermissibleObject) session.load(PermissibleObject.class, ((PermissibleObject) obj).getId()));
                obj = hibSubObj;
              }
            }
            if (obj != null) {
              PermissibleObject childObj = (PermissibleObject) obj;
              childObj.setGlobalRead(hibPermissibleObject.isGlobalRead());
              childObj.setGlobalWrite(hibPermissibleObject.isGlobalWrite());
              childObj.setGlobalExecute(hibPermissibleObject.isGlobalExecute());
              childObj.setGlobalCreateChild(hibPermissibleObject.isGlobalCreateChild());
              session.save(childObj);
            }
          }
          if (!field.getName().equals("parent")) {
            try {
              field.set(hibPermissibleObject, field.get(permissibleObject));
            } catch (Throwable t) {
            }
          }
        } catch (Exception e) {
          Logger.log(e);
        }
      }
      hibPermissibleObject.setOwner(newOwner);
      // save it
      session.save(hibPermissibleObject);
      tx.commit();
      return hibPermissibleObject;
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

  @POST
  @Path("/updateList")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public List<PermissibleObject> updatePermissibleObjects(List<PermissibleObject> permissibleObjects, @Context HttpServletRequest httpRequest,
      @Context HttpServletResponse httpResponse) {
    for (PermissibleObject object : permissibleObjects) {
      updatePermissibleObject(object, httpRequest, httpResponse);
    }
    return permissibleObjects;
  }

  @DELETE
  @Path("/delete/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public void deletePermissibleObject(@PathParam("id") Long id, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (id == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Transaction tx = null;
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      tx = session.beginTransaction();

      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }

      PermissibleObject permissibleObject = ((PermissibleObject) session.load(PermissibleObject.class, id));

      if (permissibleObject instanceof Folder) {
        Folder folder = (Folder) permissibleObject;
        if (!authUser.isAdministrator() && !authUser.equals(folder.getOwner())) {
          throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        FolderHelper.deleteFolder(session, folder);
      } else {
        if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.WRITE)) {
          throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        // just try to delete the object, hopefully it has no children
        PermissibleObjectHelper.deletePermissibleObject(session, permissibleObject);
      }
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
  @Path("/delete")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void deletePermissibleObjects(Set<Long> ids, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (ids == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    for (Long id : ids) {
      deletePermissibleObject(id, httpRequest, httpResponse);
    }
  }

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

  @PUT
  @Path("/newFolder")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Folder createNewFolder(Folder newFolder, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (newFolder == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    Transaction tx = null;
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      tx = session.beginTransaction();
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }

      if (newFolder.getParent() != null) {
        newFolder.setParent((PermissibleObject) session.load(PermissibleObject.class, newFolder.getParent().getId()));
      }
      if (!SecurityHelper.doesUserHavePermission(session, authUser, newFolder.getParent(), PERM.WRITE)) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      if (newFolder.getId() != null) {
        Folder hibNewFolder = (Folder) session.load(Folder.class, newFolder.getId());
        if (hibNewFolder != null) {
          if (!SecurityHelper.doesUserHavePermission(session, authUser, hibNewFolder, PERM.WRITE)) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
          }
          hibNewFolder.setName(newFolder.getName());
          hibNewFolder.setDescription(newFolder.getDescription());
          hibNewFolder.setParent(newFolder.getParent());
          newFolder = hibNewFolder;
        }
      }
      newFolder.setOwner(authUser);
      session.save(newFolder);
      tx.commit();
      return newFolder;
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @POST
  @Path("/rename/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public PermissibleObject rename(@PathParam("id") Long id, String name, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (id == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    if (StringUtils.isEmpty(name)) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    Transaction tx = null;
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      tx = session.beginTransaction();
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      PermissibleObject hibObj = (PermissibleObject) session.load(PermissibleObject.class, id);
      if (!SecurityHelper.doesUserHavePermission(session, authUser, hibObj, PERM.WRITE)) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      hibObj.setName(name);
      session.save(hibObj);
      tx.commit();
      return hibObj;
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @POST
  @Path("/echo")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
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
  @Path("/counterTick")
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
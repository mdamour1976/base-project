package org.damour.base.server;

import java.lang.reflect.Field;
import java.util.List;

import org.damour.base.client.exceptions.SimpleMessageException;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.PermissibleObjectTreeNode;
import org.damour.base.client.objects.Permission;
import org.damour.base.client.objects.Permission.PERM;
import org.damour.base.client.objects.User;
import org.damour.base.server.hibernate.HibernateUtil;
import org.damour.base.server.hibernate.ReflectionCache;
import org.damour.base.server.hibernate.helpers.PermissibleObjectHelper;
import org.damour.base.server.hibernate.helpers.RatingHelper;
import org.damour.base.server.hibernate.helpers.SecurityHelper;
import org.damour.base.server.resource.UserResource;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class BaseService extends RemoteServiceServlet implements org.damour.base.client.service.BaseService {

  private ThreadLocal<Session> session = new ThreadLocal<Session>();

  public Session getSession() {
    return session.get();
  }

  public BaseService() {
    super();
  }

  protected void onBeforeRequestDeserialized(String serializedRequest) {
    session.set(HibernateUtil.getInstance().getSession());
    BaseSystem.getDomainName(getThreadLocalRequest());
    Logger.log(serializedRequest);
  }

  protected void onAfterResponseSerialized(String serializedResponse) {
    try {
      session.get().close();
    } catch (Throwable t) {
    }
    try {
      session.set(null);
    } catch (Throwable t) {
    }
    Logger.log(serializedResponse);
  }

  protected void doUnexpectedFailure(Throwable e) {
    try {
      session.get().close();
    } catch (Throwable t) {
    }
    try {
      session.set(null);
    } catch (Throwable t) {
    }
    Logger.log(e);
    super.doUnexpectedFailure(e);
  }

  public PermissibleObject getPermissibleObject(Long id) throws SimpleMessageException {
    if (id == null) {
      throw new SimpleMessageException("Id not supplied.");
    }
    User authUser = (new UserResource()).getAuthenticatedUser(session.get(), getThreadLocalRequest(), getThreadLocalResponse());
    try {
      PermissibleObject permissibleObject = (PermissibleObject) session.get().load(PermissibleObject.class, id);
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.READ)) {
        throw new SimpleMessageException("User is not authorized to get this content.");
      }
      return permissibleObject;
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public List<PermissibleObject> getMyPermissibleObjects(PermissibleObject parent, String objectType) throws SimpleMessageException {
    User authUser = (new UserResource()).getAuthenticatedUser(session.get(), getThreadLocalRequest(), getThreadLocalResponse());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    Class<?> clazz;
    try {
      clazz = Class.forName(objectType);
      return PermissibleObjectHelper.getMyPermissibleObjects(session.get(), authUser, parent, clazz);
    } catch (ClassNotFoundException cnfe) {
      throw new SimpleMessageException(cnfe.getMessage());
    }
  }

  public List<Permission> getPermissions(PermissibleObject permissibleObject) throws SimpleMessageException {
    if (permissibleObject == null) {
      throw new SimpleMessageException("PermissibleObject not supplied.");
    }
    User authUser = (new UserResource()).getAuthenticatedUser(session.get(), getThreadLocalRequest(), getThreadLocalResponse());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    try {
      permissibleObject = ((PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId()));
      if (!authUser.isAdministrator() && !permissibleObject.getOwner().equals(authUser)) {
        throw new SimpleMessageException("User is not authorized to get permissions on this content.");
      }
      return SecurityHelper.getPermissions(session.get(), permissibleObject);
    } catch (Throwable t) {
      Logger.log(t);
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public void setPermissions(PermissibleObject permissibleObject, List<Permission> permissions) throws SimpleMessageException {
    if (permissibleObject == null) {
      throw new SimpleMessageException("PermissibleObject not supplied.");
    }
    if (permissions == null) {
      throw new SimpleMessageException("Permissions not supplied.");
    }
    User authUser = (new UserResource()).getAuthenticatedUser(session.get(), getThreadLocalRequest(), getThreadLocalResponse());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      PermissibleObject hibPermissibleObject = ((PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId()));
      if (!authUser.isAdministrator() && !hibPermissibleObject.getOwner().equals(authUser)) {
        throw new SimpleMessageException("User is not authorized to set permissions on this object.");
      }
      session.get().evict(authUser);

      SecurityHelper.deletePermissions(session.get(), permissibleObject);
      for (Permission permission : permissions) {
        session.get().save(permission);
      }

      List<Field> fields = ReflectionCache.getFields(permissibleObject.getClass());
      for (Field field : fields) {
        try {
          // do not update parent permission only our 'owned' objects
          if (!"parent".equals(field.getName())) {
            Object obj = field.get(permissibleObject);
            if (obj instanceof PermissibleObject) {
              PermissibleObject childObj = (PermissibleObject) obj;
              childObj.setGlobalRead(permissibleObject.isGlobalRead());
              childObj.setGlobalWrite(permissibleObject.isGlobalWrite());
              childObj.setGlobalExecute(permissibleObject.isGlobalExecute());
              childObj.setGlobalCreateChild(permissibleObject.isGlobalCreateChild());
              SecurityHelper.deletePermissions(session.get(), childObj);
              for (Permission permission : permissions) {
                Permission newPerm = new Permission();
                newPerm.setPermissibleObject(childObj);
                newPerm.setSecurityPrincipal(permission.getSecurityPrincipal());
                newPerm.setReadPerm(permission.isReadPerm());
                newPerm.setWritePerm(permission.isWritePerm());
                newPerm.setExecutePerm(permission.isExecutePerm());
                newPerm.setCreateChildPerm(permission.isCreateChildPerm());
                session.get().save(newPerm);
              }
            }
          }
        } catch (Exception e) {
          Logger.log(e);
        }
      }
      tx.commit();
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public List<PermissibleObjectTreeNode> searchPermissibleObjects(PermissibleObject parent, String query, String sortField, boolean sortDescending,
      String searchObjectType, boolean searchNames, boolean searchDescriptions, boolean searchKeywords, boolean useExactPhrase) throws SimpleMessageException {
    User authUser = (new UserResource()).getAuthenticatedUser(session.get(), getThreadLocalRequest(), getThreadLocalResponse());
    // return all permissible objects which match the name/description
    try {
      Class<?> clazz = Class.forName(searchObjectType);
      return PermissibleObjectHelper.search(session.get(), authUser, RatingHelper.getVoterGUID(getThreadLocalRequest(), getThreadLocalResponse()), clazz,
          query, sortField, sortDescending, searchNames, searchDescriptions, searchKeywords, useExactPhrase);
    } catch (Throwable t) {
      throw new SimpleMessageException(t.getMessage());
    }
  }

}

package org.damour.base.server;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.damour.base.client.exceptions.SimpleMessageException;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.PermissibleObjectTreeNode;
import org.damour.base.client.objects.Permission;
import org.damour.base.client.objects.Permission.PERM;
import org.damour.base.client.objects.User;
import org.damour.base.server.hibernate.HibernateUtil;
import org.damour.base.server.hibernate.ReflectionCache;
import org.damour.base.server.hibernate.helpers.FolderHelper;
import org.damour.base.server.hibernate.helpers.PermissibleObjectHelper;
import org.damour.base.server.hibernate.helpers.RatingHelper;
import org.damour.base.server.hibernate.helpers.SecurityHelper;
import org.damour.base.server.resource.PermissibleResource;
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

  public void deletePermissibleObject(PermissibleObject permissibleObject) throws SimpleMessageException {
    if (permissibleObject == null) {
      throw new SimpleMessageException("Object not supplied.");
    }
    User authUser = (new UserResource()).getAuthenticatedUser(session.get(), getThreadLocalRequest(), getThreadLocalResponse());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();

    permissibleObject = ((PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId()));

    try {
      if (permissibleObject instanceof Folder) {
        Folder folder = (Folder) permissibleObject;
        if (!authUser.isAdministrator() && !authUser.equals(folder.getOwner())) {
          throw new SimpleMessageException("User is not authorized to delete this object.");
        }
        FolderHelper.deleteFolder(session.get(), folder);
      } else {
        if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, permissibleObject, PERM.WRITE)) {
          throw new SimpleMessageException("User is not authorized to delete this object.");
        }
        // just try to delete the object, hopefully it has no children
        PermissibleObjectHelper.deletePermissibleObject(session.get(), permissibleObject);
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

  public void deletePermissibleObjects(Set<PermissibleObject> permissibleObjects) throws SimpleMessageException {
    if (permissibleObjects == null) {
      throw new SimpleMessageException("Objects not supplied.");
    }
    User authUser = (new UserResource()).getAuthenticatedUser(session.get(), getThreadLocalRequest(), getThreadLocalResponse());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    for (PermissibleObject permissibleObject : permissibleObjects) {
      deletePermissibleObject(permissibleObject);
    }
  }

  public void deleteAndSavePermissibleObjects(Set<PermissibleObject> toBeDeleted, Set<PermissibleObject> toBeSaved) throws SimpleMessageException {
    deletePermissibleObjects(toBeDeleted);
    (new PermissibleResource()).savePermissibleObjects(new ArrayList<PermissibleObject>(toBeSaved), getThreadLocalRequest(), getThreadLocalResponse());
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

  public Folder createNewFolder(Folder newFolder) throws SimpleMessageException {
    if (newFolder == null) {
      throw new SimpleMessageException("Folder not supplied.");
    }
    User authUser = (new UserResource()).getAuthenticatedUser(session.get(), getThreadLocalRequest(), getThreadLocalResponse());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      if (newFolder.getParent() != null) {
        newFolder.setParent((PermissibleObject) session.get().load(PermissibleObject.class, newFolder.getParent().getId()));
      }
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, newFolder.getParent(), PERM.WRITE)) {
        throw new SimpleMessageException("User is not authorized to create a new folder here.");
      }
      if (newFolder.getId() != null) {
        Folder hibNewFolder = (Folder) session.get().load(Folder.class, newFolder.getId());
        if (hibNewFolder != null) {
          if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, hibNewFolder, PERM.WRITE)) {
            throw new SimpleMessageException("User is not authorized to save a new folder here.");
          }
          hibNewFolder.setName(newFolder.getName());
          hibNewFolder.setDescription(newFolder.getDescription());
          hibNewFolder.setParent(newFolder.getParent());
          newFolder = hibNewFolder;
        }
      }

      newFolder.setOwner(authUser);
      session.get().save(newFolder);
      tx.commit();
      return newFolder;
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public void renameFile(File file) throws SimpleMessageException {
    if (file == null) {
      throw new SimpleMessageException("File not supplied.");
    }
    User authUser = (new UserResource()).getAuthenticatedUser(session.get(), getThreadLocalRequest(), getThreadLocalResponse());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      File hibfile = (File) session.get().load(File.class, file.getId());
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, hibfile, PERM.WRITE)) {
        throw new SimpleMessageException("User is not authorized to rename this file.");
      }
      hibfile.setName(file.getName());
      session.get().save(hibfile);
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

  public void renameFolder(Folder folder) throws SimpleMessageException {
    if (folder == null) {
      throw new SimpleMessageException("Folder not supplied.");
    }
    User authUser = (new UserResource()).getAuthenticatedUser(session.get(), getThreadLocalRequest(), getThreadLocalResponse());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      Folder hibfolder = (Folder) session.get().load(Folder.class, folder.getId());
      if (!SecurityHelper.doesUserHavePermission(session.get(), authUser, hibfolder, PERM.WRITE)) {
        throw new SimpleMessageException("User is not authorized to rename this folder.");
      }
      hibfolder.setName(folder.getName());
      session.get().save(hibfolder);
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

  public PermissibleObject updatePermissibleObject(PermissibleObject permissibleObject) throws SimpleMessageException {
    if (permissibleObject == null) {
      throw new SimpleMessageException("PermissibleObject not supplied.");
    }
    User authUser = (new UserResource()).getAuthenticatedUser(session.get(), getThreadLocalRequest(), getThreadLocalResponse());
    if (authUser == null) {
      throw new SimpleMessageException("User is not authenticated.");
    }
    Transaction tx = session.get().beginTransaction();
    try {
      User newOwner = ((User) session.get().load(User.class, permissibleObject.getOwner().getId()));
      PermissibleObject hibPermissibleObject = ((PermissibleObject) session.get().load(PermissibleObject.class, permissibleObject.getId()));
      if (!authUser.isAdministrator() && !hibPermissibleObject.getOwner().equals(authUser)) {
        throw new SimpleMessageException("User is not authorized to update this object.");
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
                PermissibleObject hibSubObj = ((PermissibleObject) session.get().load(PermissibleObject.class, ((PermissibleObject) obj).getId()));
                obj = hibSubObj;
              }
            }
            if (obj != null) {
              PermissibleObject childObj = (PermissibleObject) obj;
              childObj.setGlobalRead(hibPermissibleObject.isGlobalRead());
              childObj.setGlobalWrite(hibPermissibleObject.isGlobalWrite());
              childObj.setGlobalExecute(hibPermissibleObject.isGlobalExecute());
              childObj.setGlobalCreateChild(hibPermissibleObject.isGlobalCreateChild());
              session.get().save(childObj);
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

      // save it
      session.get().save(hibPermissibleObject);
      tx.commit();
      return hibPermissibleObject;
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new SimpleMessageException(t.getMessage());
    }
  }

  public List<PermissibleObject> updatePermissibleObjects(List<PermissibleObject> permissibleObjects) throws SimpleMessageException {
    for (PermissibleObject object : permissibleObjects) {
      updatePermissibleObject(object);
    }
    return permissibleObjects;
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

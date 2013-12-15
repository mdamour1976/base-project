package org.damour.base.server.hibernate.helpers;

import java.util.List;
import java.util.Set;

import org.damour.base.client.objects.File;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.PermissibleObjectTreeNode;
import org.damour.base.client.objects.Permission;
import org.damour.base.client.objects.Permission.PERM;
import org.damour.base.client.objects.RepositoryTreeNode;
import org.damour.base.client.objects.User;
import org.hibernate.Session;

public class RepositoryHelper {

  public static void buildRepositoryTreeNode(Session session, User user, RepositoryTreeNode parentNode, Folder parentFolder) {
    if (!SecurityHelper.doesUserHavePermission(session, user, parentFolder, PERM.READ)) {
      return;
    }

    List<Folder> folders = null;
    if (parentFolder == null) {
      if (user != null && user.isAdministrator()) {
        // admin sees all
        folders = session.createQuery("from " + Folder.class.getSimpleName() + " as folder where folder.parent is null").setCacheable(true).list();
      } else if (user == null) {
        folders = session.createQuery("from " + Folder.class.getSimpleName() + " as folder where folder.parent is null folder.globalRead = true")
            .setCacheable(true).list();
      } else {
        folders = session
            .createQuery(
                "from " + Folder.class.getSimpleName() + " as folder where folder.parent is null and (folder.owner.id = " + user.id
                    + " OR folder.globalRead = true OR folder.id in " + getUserPermissionQuery(user, "folder") + " OR folder.id in "
                    + getGroupPermissionQuery(user, "folder") + ")").setCacheable(true).list();
      }
    } else {
      if (user != null && user.isAdministrator()) {
        // admin sees all
        folders = session.createQuery("from " + Folder.class.getSimpleName() + " as folder where folder.parent.id = " + parentFolder.id).setCacheable(true)
            .list();
      } else if (user == null) {
        folders = session
            .createQuery("from " + Folder.class.getSimpleName() + " as folder where folder.parent.id = " + parentFolder.id + " and folder.globalRead = true")
            .setCacheable(true).list();
      } else {
        folders = session
            .createQuery(
                "from " + Folder.class.getSimpleName() + " as folder where folder.parent.id = " + parentFolder.id + " and (folder.owner.id = " + user.id
                    + " OR folder.globalRead = true OR folder.id in " + getUserPermissionQuery(user, "folder") + " OR folder.id in "
                    + getGroupPermissionQuery(user, "folder") + ")").setCacheable(true).list();
      }
    }
    List<File> files = null;
    if (parentFolder == null) {
      if (user != null && user.isAdministrator()) {
        // admin sees all
        files = session.createQuery("from " + File.class.getSimpleName() + " as file where file.parent is null").setCacheable(true).list();
      } else if (user == null) {
        files = session.createQuery("from " + File.class.getSimpleName() + " as file where file.parent is null and file.globalRead = true").setCacheable(true)
            .list();
      } else {
        files = session
            .createQuery(
                "from " + File.class.getSimpleName() + " as file where file.parent is null and (file.owner.id = " + user.id
                    + " OR file.globalRead = true OR file.id in " + getUserPermissionQuery(user, "file") + " OR file.id in "
                    + getGroupPermissionQuery(user, "file") + ")").setCacheable(true).list();
      }
    } else {
      if (user != null && user.isAdministrator()) {
        // admin sees all
        files = session.createQuery("from " + File.class.getSimpleName() + " as file where file.parent.id = " + parentFolder.id).setCacheable(true).list();
      } else if (user == null) {
        files = session
            .createQuery("from " + File.class.getSimpleName() + " as file where file.parent.id = " + parentFolder.id + " and file.globalRead = true")
            .setCacheable(true).list();
      } else {
        files = session
            .createQuery(
                "from " + File.class.getSimpleName() + " as file where file.parent.id = " + parentFolder.id + " and (file.owner.id = " + user.id
                    + " OR file.globalRead = true OR file.id in " + getUserPermissionQuery(user, "file") + " OR file.id in "
                    + getGroupPermissionQuery(user, "file") + ")").setCacheable(true).list();
      }
    }
    for (Folder folder : folders) {
      RepositoryTreeNode childNode = new RepositoryTreeNode();
      parentNode.getFolders().put(folder, childNode);
      buildRepositoryTreeNode(session, user, childNode, folder);
    }
    parentNode.setFiles(files);
  }

  public static void buildPermissibleObjectTreeNode(Session session, User user, User owner, String voterGUID, PermissibleObjectTreeNode parentNode,
      PermissibleObject parent, List<String> acceptedClasses, int currentDepth, int fetchDepth, int metaDataFetchDepth) {

    if (!SecurityHelper.doesUserHavePermission(session, user, parent, PERM.READ)) {
      return;
    }

    if (parent != null && acceptedClasses != null && !acceptedClasses.contains(parent.getClass().getName())) {
      return;
    }
    parentNode.setObject(parent);

    if (parent != null && (metaDataFetchDepth == -1 || currentDepth <= metaDataFetchDepth)) {
      if (parent.getNumAdvisoryVotes() > 0) {
        parentNode.setUserAdvisory(AdvisoryHelper.getUserAdvisory(session, parent, user, voterGUID));
      }
      if (parent.getNumRatingVotes() > 0) {
        parentNode.setUserRating(RatingHelper.getUserRating(session, parent, user, voterGUID));
      }
      if (parent.getNumUpVotes() > 0 || parent.getNumDownVotes() > 0) {
        parentNode.setUserThumb(ThumbHelper.getUserThumb(session, parent, user, voterGUID));
      }
    }

    if (currentDepth != -1 && currentDepth >= fetchDepth) {
      return;
    }

    List<PermissibleObject> children = null;
    if (parent == null) {
      if (user != null && user.isAdministrator()) {
        // admin sees all
        if (owner != null) {
          children = session
              .createQuery(
                  "from " + PermissibleObject.class.getSimpleName() + " as obj where obj.parent is null and obj.owner.id = " + owner.getId() + " order by id")
              .setCacheable(true).list();
        } else {
          children = session.createQuery("from " + PermissibleObject.class.getSimpleName() + " as obj where obj.parent is null order by id").setCacheable(true)
              .list();
        }
      } else if (user == null) {
        if (owner != null) {
          children = session
              .createQuery(
                  "from " + PermissibleObject.class.getSimpleName() + " as obj where obj.parent is null and obj.globalRead = true and obj.owner.id = "
                      + owner.getId() + " order by id").setCacheable(true).list();
        } else {
          children = session
              .createQuery("from " + PermissibleObject.class.getSimpleName() + " as obj where obj.parent is null and obj.globalRead = true order by id")
              .setCacheable(true).list();
        }
      } else {
        if (owner != null) {
          children = session
              .createQuery(
                  "from " + PermissibleObject.class.getSimpleName() + " as obj where obj.parent is null and obj.owner.id = " + owner.getId()
                      + " and (obj.owner.id = " + user.id + " OR obj.globalRead = true OR obj.id in " + getUserPermissionQuery(user, "obj") + " OR obj.id in "
                      + getGroupPermissionQuery(user, "obj") + ") order by id").setCacheable(true).list();
        } else {
          children = session
              .createQuery(
                  "from " + PermissibleObject.class.getSimpleName() + " as obj where obj.parent is null and (obj.owner.id = " + user.id
                      + " OR obj.globalRead = true OR obj.id in " + getUserPermissionQuery(user, "obj") + " OR obj.id in "
                      + getGroupPermissionQuery(user, "obj") + ") order by id").setCacheable(true).list();
        }
      }
    } else {
      if (user != null && user.isAdministrator()) {
        // admin sees all
        if (owner != null) {
          children = session
              .createQuery(
                  "from " + PermissibleObject.class.getSimpleName() + " as obj where obj.parent.id = " + parent.id + " and obj.owner.id = " + owner.getId()
                      + " order by id").setCacheable(true).list();
        } else {
          children = session.createQuery("from " + PermissibleObject.class.getSimpleName() + " as obj where obj.parent.id = " + parent.id + " order by id")
              .setCacheable(true).list();
        }
      } else if (user == null) {
        if (owner != null) {
          children = session
              .createQuery(
                  "from " + PermissibleObject.class.getSimpleName() + " as obj where obj.parent.id = " + parent.id
                      + " and obj.globalRead = true and obj.owner.id = " + owner.getId() + " order by id").setCacheable(true).list();
        } else {
          children = session
              .createQuery(
                  "from " + PermissibleObject.class.getSimpleName() + " as obj where obj.parent.id = " + parent.id + " and obj.globalRead = true order by id")
              .setCacheable(true).list();
        }
      } else {
        if (owner != null) {
          children = session
              .createQuery(
                  "from " + PermissibleObject.class.getSimpleName() + " as obj where obj.parent.id = " + parent.id + " and obj.owner.id = " + owner.getId()
                      + " and (obj.owner.id = " + user.id + " OR obj.globalRead = true OR obj.id in " + getUserPermissionQuery(user, "obj") + " OR obj.id in "
                      + getGroupPermissionQuery(user, "obj") + ") order by id").setCacheable(true).list();
        } else {
          children = session
              .createQuery(
                  "from " + PermissibleObject.class.getSimpleName() + " as obj where obj.parent.id = " + parent.id + " and (obj.owner.id = " + user.id
                      + " OR obj.globalRead = true OR obj.id in " + getUserPermissionQuery(user, "obj") + " OR obj.id in "
                      + getGroupPermissionQuery(user, "obj") + ") order by id").setCacheable(true).list();
        }
      }
    }
    for (PermissibleObject child : children) {
      if (acceptedClasses != null && !acceptedClasses.contains(child.getClass().getName())) {
        continue;
      }

      PermissibleObjectTreeNode childNode = new PermissibleObjectTreeNode();
      parentNode.getChildren().add(childNode);
      childNode.setObject(child);
      buildPermissibleObjectTreeNode(session, user, owner, voterGUID, childNode, child, acceptedClasses, currentDepth + 1, fetchDepth, metaDataFetchDepth);
    }
  }

  private static String getUserPermissionQuery(User user, String objStr) {
    String query = "(select perm.permissibleObject.id from " + Permission.class.getSimpleName() + " as perm where perm.permissibleObject.id = " + objStr
        + ".id and perm.securityPrincipal.id = " + user.id + " and perm.readPerm = true)";
    return query;
  }

  private static String getGroupPermissionQuery(User user, String objStr) {
    String query = "(select perm.permissibleObject.id from "
        + Permission.class.getSimpleName()
        + " as perm where perm.permissibleObject.id = "
        + objStr
        + ".id and perm.readPerm = true and perm.securityPrincipal.id in (select membership.userGroup.id from GroupMembership as membership where membership.user.id = "
        + user.id + "))";
    return query;
  }

  public static void getPermissibleObjects(Session session, User user, List<PermissibleObject> permissibleObjectList, PermissibleObject parent,
      Class instanceType) {
    if (!SecurityHelper.doesUserHavePermission(session, user, parent, PERM.READ)) {
      return;
    }

    List<PermissibleObject> children = null;
    if (parent == null) {
      if (user != null && user.isAdministrator()) {
        // admin sees all
        children = session.createQuery("from " + instanceType.getSimpleName() + " as obj where obj.parent is null").setCacheable(true).list();
      } else if (user == null) {
        children = session.createQuery("from " + instanceType.getSimpleName() + " as obj where obj.parent is null and obj.globalRead = true")
            .setCacheable(true).list();
      } else {
        children = session
            .createQuery(
                "from " + instanceType.getSimpleName() + " as obj where obj.parent is null and (obj.owner.id = " + user.id
                    + " OR obj.globalRead = true OR obj.id in " + getUserPermissionQuery(user, "obj") + " OR obj.id in " + getGroupPermissionQuery(user, "obj")
                    + ")").setCacheable(true).list();
      }
    } else {
      if (user != null && user.isAdministrator()) {
        // admin sees all
        children = session.createQuery("from " + PermissibleObject.class.getSimpleName() + " as obj where obj.parent.id = " + parent.id).setCacheable(true)
            .list();
      } else if (user == null) {
        children = session
            .createQuery("from " + PermissibleObject.class.getSimpleName() + " as obj where obj.parent.id = " + parent.id + " and obj.globalRead = true")
            .setCacheable(true).list();
      } else {
        children = session
            .createQuery(
                "from " + PermissibleObject.class.getSimpleName() + " as obj where obj.parent.id = " + parent.id + " and (obj.owner.id = " + user.id
                    + " OR obj.globalRead = true OR obj.id in " + getUserPermissionQuery(user, "obj") + " OR obj.id in " + getGroupPermissionQuery(user, "obj")
                    + ")").setCacheable(true).list();
      }
    }
    for (PermissibleObject child : children) {
      if (child.getClass().isAssignableFrom(instanceType)) {
        permissibleObjectList.add(child);
      }
      getPermissibleObjects(session, user, permissibleObjectList, child, instanceType);
    }
  }

  public static void dumpTreeNode(RepositoryTreeNode parent, int depth) {
    Set<Folder> folders = parent.getFolders().keySet();
    List<File> files = parent.getFiles();

    if (folders != null) {
      for (Folder folder : folders) {
        for (int i = 0; i < depth; i++) {
          System.out.print("   ");
        }
        System.out.println("FOLDER: " + folder.getName());
      }
    }
    if (files != null) {
      for (File file : files) {
        for (int i = 0; i < depth; i++) {
          System.out.print("   ");
        }
        System.out.println("FILE: " + file.getName());
      }
    }
    for (RepositoryTreeNode childNode : parent.getFolders().values()) {
      dumpTreeNode(childNode, depth + 1);
    }
  }

  public static void dumpTreeNode(PermissibleObjectTreeNode parent, int depth) {
    List<PermissibleObjectTreeNode> children = parent.getChildren();

    if (children != null) {
      for (PermissibleObjectTreeNode child : children) {
        if (depth > 0) {
          System.out.print("  |");
        }
        for (int i = 0; i < depth; i++) {
          System.out.print("   ");
          if (i > 0 && i < depth - 1) {
            System.out.print("|");
          }
        }
        if (depth > 0) {
          System.out.print("+-");
        }
        System.out.println("CHILD: " + child.getObject().getName());
        dumpTreeNode(child, depth + 1);
      }
    }
  }
}

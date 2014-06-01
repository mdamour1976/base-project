package org.damour.base.client.service;

import java.util.List;

import org.damour.base.client.exceptions.SimpleMessageException;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.PermissibleObjectTreeNode;
import org.damour.base.client.objects.Permission;

import com.google.gwt.user.client.rpc.RemoteService;

public interface BaseService extends RemoteService {

  // file/content/permissions methods
  public List<PermissibleObject> getMyPermissibleObjects(PermissibleObject parent, String objectType) throws SimpleMessageException;
  public Folder createNewFolder(Folder newFolder) throws SimpleMessageException;
  public void renameFile(File file) throws SimpleMessageException;
  public void renameFolder(Folder folder) throws SimpleMessageException;
  public List<Permission> getPermissions(PermissibleObject permissibleObject) throws SimpleMessageException;
  public void setPermissions(PermissibleObject permissibleObject, List<Permission> permissions) throws SimpleMessageException;
  public List<PermissibleObjectTreeNode> searchPermissibleObjects(PermissibleObject parent, String query, String sortField, boolean sortDescending, String searchObjectType, boolean searchNames, boolean searchDescriptions, boolean searchKeywords, boolean useExactPhrase) throws SimpleMessageException;
}

package org.damour.base.client.service;

import java.util.List;
import java.util.Set;

import org.damour.base.client.exceptions.SimpleMessageException;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.FileUploadStatus;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.PermissibleObjectTreeNode;
import org.damour.base.client.objects.Permission;

import com.google.gwt.user.client.rpc.RemoteService;

public interface BaseService extends RemoteService {

  // file/content/permissions methods
  public PermissibleObject savePermissibleObject(PermissibleObject permissibleObject) throws SimpleMessageException;
  public List<PermissibleObject> savePermissibleObjects(List<PermissibleObject> permissibleObjects) throws SimpleMessageException;
  public void deletePermissibleObject(PermissibleObject permissibleObject) throws SimpleMessageException;
  public void deletePermissibleObjects(Set<PermissibleObject> permissibleObjects) throws SimpleMessageException;
  public void deleteAndSavePermissibleObjects(Set<PermissibleObject> toBeDeleted, Set<PermissibleObject> toBeSaved) throws SimpleMessageException;
  public List<PermissibleObject> getMyPermissibleObjects(PermissibleObject parent, String objectType) throws SimpleMessageException;
  public Folder createNewFolder(Folder newFolder) throws SimpleMessageException;
  public void renameFile(File file) throws SimpleMessageException;
  public void renameFolder(Folder folder) throws SimpleMessageException;
  public List<Permission> getPermissions(PermissibleObject permissibleObject) throws SimpleMessageException;
  public void setPermissions(PermissibleObject permissibleObject, List<Permission> permissions) throws SimpleMessageException;
  public PermissibleObject updatePermissibleObject(PermissibleObject permissibleObject) throws SimpleMessageException;
  public List<PermissibleObject> updatePermissibleObjects(List<PermissibleObject> permissibleObjects) throws SimpleMessageException;
  public FileUploadStatus getFileUploadStatus() throws SimpleMessageException;
  public List<PermissibleObjectTreeNode> searchPermissibleObjects(PermissibleObject parent, String query, String sortField, boolean sortDescending, String searchObjectType, boolean searchNames, boolean searchDescriptions, boolean searchKeywords, boolean useExactPhrase) throws SimpleMessageException;
  public Long getCustomCounter1(PermissibleObject permissibleObject);
  public Long incrementCustomCounter1(PermissibleObject permissibleObject);
  // for debug purposes: simply return what was given, proving the serialization of the desired object
  public PermissibleObject echoPermissibleObject(PermissibleObject permissibleObject) throws SimpleMessageException;

}

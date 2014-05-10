package org.damour.base.client.service;

import java.util.List;
import java.util.Set;

import org.damour.base.client.objects.File;
import org.damour.base.client.objects.FileUploadStatus;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.PermissibleObjectTreeNode;
import org.damour.base.client.objects.Permission;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface BaseServiceAsync {
  
  // file/content/permissions methods
  public void deletePermissibleObject(PermissibleObject permissibleObject, AsyncCallback<Void> callback);
  public void deletePermissibleObjects(Set<PermissibleObject> permissibleObjects, AsyncCallback<Void> callback);
  public void deleteAndSavePermissibleObjects(Set<PermissibleObject> toBeDeleted, Set<PermissibleObject> toBeSaved, AsyncCallback<Void> callback);
  public void getMyPermissibleObjects(PermissibleObject parent, String objectType, AsyncCallback<List<PermissibleObject>> callback);
  public void createNewFolder(Folder newFolder, AsyncCallback<Folder> callback);
  public void renameFile(File file, AsyncCallback<Void> callback);
  public void renameFolder(Folder folder, AsyncCallback<Void> callback);
  public void getPermissions(PermissibleObject permissibleObject, AsyncCallback<List<Permission>> callback);
  public void setPermissions(PermissibleObject permissibleObject, List<Permission> permissions, AsyncCallback<Void> callback);
  public void updatePermissibleObject(PermissibleObject permissibleObject, AsyncCallback<PermissibleObject> callback);
  public void updatePermissibleObjects(List<PermissibleObject> permissibleObjects, AsyncCallback<List<PermissibleObject>> callback);
  public void getFileUploadStatus(AsyncCallback<FileUploadStatus> callback);
  public void searchPermissibleObjects(PermissibleObject parent, String query, String sortField, boolean sortDescending, String searchObjectType, boolean searchNames, boolean searchDescriptions, boolean searchKeywords, boolean useExactPhrase, AsyncCallback<List<PermissibleObjectTreeNode>> callback);
}

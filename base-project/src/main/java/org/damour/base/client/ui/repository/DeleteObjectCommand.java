package org.damour.base.client.ui.repository;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.service.ResourceCache;
import org.damour.base.client.ui.dialogs.IDialogCallback;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.ui.dialogs.PromptDialogBox;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Label;

public class DeleteObjectCommand implements Command {

  PermissibleObject permissibleObject;
  IRepositoryCallback repositoryCallback;

  public DeleteObjectCommand(PermissibleObject permissibleObject, IRepositoryCallback repositoryCallback) {
    this.permissibleObject = permissibleObject;
    this.repositoryCallback = repositoryCallback;
  }

  public void execute() {
    PromptDialogBox dialogBox = new PromptDialogBox("Question", "Yes", null, "No", false, true);
    dialogBox.setContent(new Label("Delete " + permissibleObject.getName() + "?"));
    dialogBox.setCallback(new IDialogCallback() {
      public void okPressed() {
        final MethodCallback<Void> deleteCallback = new MethodCallback<Void>() {
          public void onFailure(Method method, Throwable exception) {
            MessageDialogBox messageDialog = new MessageDialogBox("Error", exception.getMessage(), false, true, true);
            messageDialog.center();
          }

          public void onSuccess(Method method, Void response) {
            repositoryCallback.fileDeleted();
          }
        };
        ResourceCache.getPermissibleResource().deletePermissibleObject(permissibleObject.getId(), deleteCallback);
      }

      public void cancelPressed() {
      }
    });
    dialogBox.center();
  }

}

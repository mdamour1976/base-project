package org.damour.base.client.ui.repository;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.service.ResourceCache;
import org.damour.base.client.ui.dialogs.IDialogCallback;
import org.damour.base.client.ui.dialogs.IDialogValidatorCallback;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.ui.dialogs.PromptDialogBox;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.TextBox;

public class RenameObjectCommand implements Command {

  PermissibleObject permissibleObject;
  IRepositoryCallback repositoryCallback;

  public RenameObjectCommand(PermissibleObject permissibleObject, IRepositoryCallback repositoryCallback) {
    this.permissibleObject = permissibleObject;
    this.repositoryCallback = repositoryCallback;
  }

  public void execute() {
    final TextBox nameTextBox = new TextBox();
    nameTextBox.setVisibleLength(60);
    nameTextBox.setText(permissibleObject.getName());
    PromptDialogBox dialogBox = new PromptDialogBox("Enter New Name", "OK", null, "Cancel", false, true);
    dialogBox.setContent(nameTextBox);
    dialogBox.setCallback(new IDialogCallback() {
      public void okPressed() {
        final MethodCallback<PermissibleObject> renameCallback = new MethodCallback<PermissibleObject>() {
          public void onFailure(Method method, Throwable exception) {
            MessageDialogBox messageDialog = new MessageDialogBox("Error", exception.getMessage(), false, true, true);
            messageDialog.center();
          }

          public void onSuccess(Method method, PermissibleObject permissibleObject) {
            repositoryCallback.objectRenamed(permissibleObject);
          }
        };

        permissibleObject.setName(nameTextBox.getText());
        ResourceCache.getPermissibleResource().rename(permissibleObject.getId(), nameTextBox.getText(), renameCallback);
      }

      public void cancelPressed() {
      }
    });
    dialogBox.setValidatorCallback(new IDialogValidatorCallback() {
      public boolean validate() {
        return (nameTextBox.getText() != null && !"".equals(nameTextBox.getText()));
      }
    });
    dialogBox.center();
  }

}

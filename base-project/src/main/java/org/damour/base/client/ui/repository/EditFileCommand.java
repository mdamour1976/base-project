package org.damour.base.client.ui.repository;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.service.ResourceCache;
import org.damour.base.client.ui.IGenericCallback;
import org.damour.base.client.ui.ckeditor.CKEditor;
import org.damour.base.client.ui.dialogs.IDialogCallback;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.ui.dialogs.PromptDialogBox;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;

public class EditFileCommand implements Command {

  PermissibleObject object;
  IGenericCallback<PermissibleObject> callback;

  public EditFileCommand() {
  }

  public EditFileCommand(PermissibleObject object) {
    this.object = object;
  }

  public PermissibleObject getObject() {
    return object;
  }

  public void setObject(PermissibleObject object) {
    this.object = object;
  }

  public IGenericCallback<PermissibleObject> getCallback() {
    return callback;
  }

  public void setCallback(IGenericCallback<PermissibleObject> callback) {
    this.callback = callback;
  }

  public void execute() {
    final CKEditor editor = new CKEditor("newEditor" + System.currentTimeMillis());
    final PromptDialogBox dialogBox = new PromptDialogBox("Edit", "Save", null, "Cancel", false, false);
    dialogBox.setAllowEnterSubmit(false);
    dialogBox.setAllowEscape(false);
    dialogBox.setContent(editor);
    dialogBox.setCallback(new IDialogCallback() {

      public void okPressed() {
        object.setContentHTML(editor.getData());
        MethodCallback<PermissibleObject> callback = new MethodCallback<PermissibleObject>() {
          public void onFailure(Method method, Throwable exception) {
            MessageDialogBox messageDialog = new MessageDialogBox("Error", exception.getMessage(), false, true, true);
            messageDialog.center();
          }

          public void onSuccess(Method method, PermissibleObject response) {
            if (EditFileCommand.this.callback != null) {
              EditFileCommand.this.callback.invoke(response);
            }
          }
        };
        ResourceCache.getPermissibleResource().updatePermissibleObject(object, callback);
      }

      public void cancelPressed() {
      }
    });
    dialogBox.center();
    editor.setup(1024, 400);
    editor.setData(object.getContentHTML());
    Timer t = new Timer() {
      public void run() {
        dialogBox.center();
      }
    };
    t.schedule(100);
  }
}

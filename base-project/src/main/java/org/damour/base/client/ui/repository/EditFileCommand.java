package org.damour.base.client.ui.repository;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.IGenericCallback;
import org.damour.base.client.ui.ckeditor.CKEditor;
import org.damour.base.client.ui.dialogs.IDialogCallback;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.ui.dialogs.PromptDialogBox;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

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
    dialogBox.setContent(editor);
    dialogBox.setCallback(new IDialogCallback() {

      public void okPressed() {
        object.setContentHTML(editor.getData());
        AsyncCallback<PermissibleObject> callback = new AsyncCallback<PermissibleObject>() {
          public void onFailure(Throwable caught) {
            MessageDialogBox messageDialog = new MessageDialogBox("Error", caught.getMessage(), false, true, true);
            messageDialog.center();
          }

          public void onSuccess(PermissibleObject result) {
            if (EditFileCommand.this.callback != null) {
              EditFileCommand.this.callback.invoke(result);
            }
          }
        };
        BaseServiceCache.getService().updatePermissibleObject(object, callback);
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

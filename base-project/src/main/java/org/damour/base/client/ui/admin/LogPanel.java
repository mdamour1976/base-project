package org.damour.base.client.ui.admin;

import org.damour.base.client.objects.StringWrapper;
import org.damour.base.client.service.ResourceCache;
import org.damour.base.client.ui.buttons.Button;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LogPanel extends VerticalPanel {

  HTML log = new HTML();
  
  public LogPanel() {
    Button refreshButton = new Button("Refresh");
    refreshButton.setCommand(new Command() {
      public void execute() {
        fetchLog();
      }
    });
    add(refreshButton);
    add(log);
    fetchLog();
  }

  private void populateLog(String logStr) {
    log.setHTML(logStr);
  }
  
  private void fetchLog() {
    final MethodCallback<StringWrapper> callback = new MethodCallback<StringWrapper>() {
      public void onFailure(Method method, Throwable caught) {
        Window.alert(caught.getMessage());
      }

      public void onSuccess(Method method, StringWrapper logStr) {
        populateLog(logStr.getString());
      };
    };
    ResourceCache.getBaseResource().getLog(0L, callback);
  }
}

package org.damour.base.client.ui.repository;


import org.damour.base.client.BaseApplication;
import org.damour.base.client.objects.File;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.RootPanel;

public class DownloadFileCommand implements Command {

  File object;

  public DownloadFileCommand(File object) {
    this.object = object;
  }

  public void execute() {
    String url = BaseApplication.getSettings().getString("GetFileService", BaseApplication.GET_FILE_SERVICE_PATH) + object.getId() + "_attachment_" + object.getName();
    final Frame hidden = new Frame(url);
    hidden.getElement().getStyle().setDisplay(Display.NONE);
    RootPanel.get().add(hidden);
    return;
  }

}

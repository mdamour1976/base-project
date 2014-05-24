package org.damour.base.seed.client;

import org.damour.base.client.BaseApplicationUI;

import com.google.gwt.user.client.ui.Widget;

public class SeedApplication extends BaseApplicationUI {

  public String getModuleName() {
    return "seed";
  }

  public void loadApplication() {
    getApplicationContentDeck().add(new Launcher(getAuthenticatedUser()));
    getApplicationContentDeck().showWidget(0);
  }

  public Widget buildApplicationToolBar() {
    Widget tb = super.buildApplicationToolBar();
//    tb.add(new Label("hello"));
//    tb.addPadding(5);
    return tb;
  }

}

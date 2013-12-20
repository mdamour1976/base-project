package org.damour.base.seed.client.images;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

public interface DemoImageBundle extends ImageBundle {
  public static final DemoImageBundle images = (DemoImageBundle) GWT.create(DemoImageBundle.class);

  AbstractImagePrototype createAccount_212x89();
  AbstractImagePrototype createAccount_hover_212x89();
  AbstractImagePrototype createAccount_disabled_212x89();

  AbstractImagePrototype ratePhotos_172x89();
  AbstractImagePrototype ratePhotos_hover_172x89();
  AbstractImagePrototype ratePhotos_disabled_172x89();

  AbstractImagePrototype uploadPhotos_189x89();
  AbstractImagePrototype uploadPhotos_hover_189x89();
  AbstractImagePrototype uploadPhotos_disabled_189x89();

}

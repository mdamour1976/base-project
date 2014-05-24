package org.damour.base.client.ui.ckeditor;

import com.google.gwt.user.client.ui.SimplePanel;

public class CKEditor extends SimplePanel {

  private String id;

  public CKEditor(String id) {
    this.id = id;
    getElement().setId(id);
  }

  public void setup(int width, int height) {
    wrap(id, width, height);
  }

  public String getData() {
    return getData(id);
  }

  public void setData(String data) {
    setData(id, data);
  }

  private native void wrap(String id, int width, int height)
  /*-{
    $wnd.CKEDITOR.replace(id, { width: width, height: height });
  }-*/;

  private native String getData(String id)
  /*-{
    return $wnd.CKEDITOR.instances[id].getData();
  }-*/;

  private native void setData(String id, String data)
  /*-{
    $wnd.CKEDITOR.instances[id].setData(data);
  }-*/;

}

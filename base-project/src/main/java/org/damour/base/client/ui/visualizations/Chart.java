package org.damour.base.client.ui.visualizations;

import org.damour.base.client.BaseApplication;
import org.damour.base.client.ui.IGenericCallback;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.SimplePanel;

public abstract class Chart extends SimplePanel {

  private static boolean loaded = false;

  static {
    IGenericCallback<JavaScriptObject> callback = new IGenericCallback<JavaScriptObject>() {
      public void invoke(JavaScriptObject object) {
        loaded = true;
      }
    };
    BaseApplication.loadjsfile("//www.google.com/jsapi", callback);
  }

  public Chart() {
  }

  public static boolean isLoaded() {
    return loaded;
  }

  public static void setLoaded(boolean loaded) {
    Chart.loaded = loaded;
  }

  public abstract void draw(final String jsonData, final long fetchTime, final String[] headers, final String[] attributes);
}

package org.damour.base.client.utils;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

public class CursorUtils {

  public static void setBusyCursor(Widget widget) {
    setBusyCursor(widget.getElement());
  }

  public static void setBusyCursor(Element element) {
    element.getStyle().setCursor(Cursor.WAIT);
  }

  public static void setDefaultCursor(Widget widget) {
    setDefaultCursor(widget.getElement());
  }

  public static void setDefaultCursor(Element element) {
    element.getStyle().setCursor(Cursor.DEFAULT);
  }

  public static void setHandCursor(Widget widget) {
    setHandCursor(widget.getElement());
  }

  public static void setHandCursor(Element element) {
    element.getStyle().setCursor(Cursor.POINTER);
  }

  public static native void preventTextSelection(Element ele)
  /*-{
    ele.onselectstart=function() {return false};
    ele.ondragstart=function() {return false};
    ele.onmousedown=function() {return false};
  }-*/;
  
}

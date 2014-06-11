package org.damour.base.client.ui.share;

import java.util.List;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.ui.permalink.PermaLinkBuilder;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.SimplePanel;

public class TwitterWidget extends SimplePanel {

  public TwitterWidget(PermissibleObject permissibleObject, List<String> ignoredParameters) {
    Frame frame = new Frame();

    String url = URL.encodeQueryString(PermaLinkBuilder.getLink(permissibleObject, ignoredParameters));
    
    frame.setUrl("http://platform.twitter.com/widgets/tweet_button.html?count=horizontal&url=" + url);
    frame.getElement().setAttribute("frameBorder", "0");
    frame.getElement().setAttribute("allowTransparency", "true");
    frame.getElement().setAttribute("scrolling", "no");
    frame.getElement().getStyle().setHeight(20, Unit.PX);
    frame.getElement().getStyle().setWidth(120, Unit.PX);
    //frame.getElement().getStyle().setHeight(130, Unit.PX);
    // DOM.setStyleAttribute(frame.getElement(), "backgroundColor", "#e3e3e3");
    setWidget(frame);
  }
}

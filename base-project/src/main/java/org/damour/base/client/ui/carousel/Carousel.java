package org.damour.base.client.ui.carousel;

import org.damour.base.client.utils.StringUtils;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.SimplePanel;

public class Carousel extends SimplePanel {

  String id = "carousel-" + System.currentTimeMillis();
  Element carouselIndicators = DOM.createElement("ol");
  Element carouselInner = DOM.createDiv();
  Element carouselControlLeft = DOM.createDiv();
  Element carouselControlRight = DOM.createDiv();

  public Carousel() {
    super();
    getElement().setId(id);
    getElement().setClassName("gwt-carousel");
    getElement().addClassName("carousel");
    getElement().addClassName("slide");
    getElement().setAttribute("data-ride", "carousel");

    carouselIndicators.setClassName("carousel-indicators");
    carouselInner.setClassName("carousel-inner");
    carouselControlLeft.setClassName("carousel-control");
    carouselControlLeft.addClassName("left");
    carouselControlLeft.addClassName("hand");
    carouselControlLeft.setAttribute("href", "#" + id);
    carouselControlLeft.setAttribute("data-slide", "prev");
    carouselControlRight.setClassName("carousel-control");
    carouselControlRight.addClassName("right");
    carouselControlRight.addClassName("hand");
    carouselControlRight.setAttribute("href", "#" + id);
    carouselControlRight.setAttribute("data-slide", "next");

    Element leftGlyph = DOM.createSpan();
    leftGlyph.addClassName("glyphicon");
    leftGlyph.addClassName("glyphicon-chevron-left");
    carouselControlLeft.appendChild(leftGlyph);

    Element rightGlyph = DOM.createSpan();
    rightGlyph.addClassName("glyphicon");
    rightGlyph.addClassName("glyphicon-chevron-right");
    carouselControlRight.appendChild(rightGlyph);
    
    getElement().appendChild(carouselIndicators);
    getElement().appendChild(carouselInner);
    getElement().appendChild(carouselControlLeft);
    getElement().appendChild(carouselControlRight);
  }

  public void addItem(Element item) {
    this.addItem(item, null, null, null);
  }
  
  public void addItem(Element item, String heading, String text, String color) {
    Element div = DOM.createDiv();
    div.setClassName("item");

    Element li = DOM.createElement("li");
    li.setAttribute("data-target", "#" + id);
    li.setAttribute("data-slide-to", "" + carouselInner.getChildCount());
    carouselIndicators.appendChild(li);

    if (carouselInner.getChildCount() == 0) {
      div.addClassName("active");
      li.addClassName("active");
    }
    div.appendChild(item);
    
    if (!StringUtils.isEmpty(heading) || !StringUtils.isEmpty(text)) {
      Element caption = DOM.createDiv();
      caption.setClassName("carousel-caption");
      if (!StringUtils.isEmpty(heading)) {
        Element h3 = DOM.createElement("h3");
        h3.setInnerText(heading);
        if (!StringUtils.isEmpty(color)) {
          h3.getStyle().setColor(color);
        }
        caption.appendChild(h3);
      }
      if (!StringUtils.isEmpty(text)) {
        Element p = DOM.createElement("p");
        p.setInnerText(text);
        if (!StringUtils.isEmpty(color)) {
          p.getStyle().setColor(color);
        }
        caption.appendChild(p);
      }
      div.appendChild(caption);
    }
    
    
    carouselInner.appendChild(div);
  }

  public void init() {
    this.init(id);
  }

  private native void init(String id)
  /*-{
    $wnd.$("#" + id).carousel();
  }-*/;

}
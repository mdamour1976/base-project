package org.damour.base.client.ui.carousel;

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
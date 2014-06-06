package org.damour.base.client.ui.email;

import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.objects.PermissibleObject;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class EmailWidget extends HorizontalPanel {

  private Image mailIcon = new Image(BaseImageBundle.images.email16x16());

  private PermissibleObject permissibleObject;
  private String message;
  private String subject;

  private ClickHandler clickHandler = new ClickHandler() {
    public void onClick(ClickEvent event) {
      EmailDialog emailDialog = new EmailDialog(permissibleObject, subject, message);
      emailDialog.center();
    }
  };

  public EmailWidget(PermissibleObject permissibleObject, String subject, String message, boolean showLabel, boolean labelOnLeft) {
    this.permissibleObject = permissibleObject;
    this.message = message;
    this.subject = subject;

    setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    Label emailLabel = new Label("Email");
    if (showLabel && labelOnLeft) {
      mailIcon.getElement().getStyle().setMarginLeft(3, Unit.PX);
    } else if (showLabel && !labelOnLeft) {
      mailIcon.getElement().getStyle().setMarginRight(3, Unit.PX);
    }
    if (showLabel) {
      if (labelOnLeft) {
        add(emailLabel);
        add(mailIcon);
      } else {
        add(mailIcon);
        add(emailLabel);
      }
    } else {
      add(mailIcon);
    }
    mailIcon.addClickHandler(clickHandler);
    emailLabel.addClickHandler(clickHandler);
    mailIcon.setTitle("Email this!");
    emailLabel.setTitle("Email this!");
    emailLabel.getElement().getStyle().setCursor(Cursor.POINTER);
    mailIcon.getElement().getStyle().setCursor(Cursor.POINTER);
  }

  public PermissibleObject getPermissibleObject() {
    return permissibleObject;
  }

  public void setPermissibleObject(PermissibleObject permissibleObject) {
    this.permissibleObject = permissibleObject;
  }

}

package org.damour.base.client.ui.dialogs;

import org.damour.base.client.BaseApplication;
import org.damour.base.client.objects.AdvertisingInfo;
import org.damour.base.client.service.ResourceCache;
import org.damour.base.client.ui.authentication.AuthenticationHandler;
import org.damour.base.client.utils.StringUtils;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AdvertiseDialog extends PromptDialogBox implements IDialogValidatorCallback, IDialogCallback {

  private static TextBox contactName = new TextBox();
  private static TextBox email = new TextBox();
  private static TextBox company = new TextBox();
  private static TextBox phone = new TextBox();
  private static TextArea comments = new TextArea();

  public AdvertiseDialog() {
    super("Advertising", "Submit", null, "Cancel", false, true);
    setAllowEnterSubmit(false);

    contactName.setWidth("350px");
    email.setWidth("350px");
    company.setWidth("350px");
    phone.setWidth("350px");
    comments.setVisibleLines(4);
    comments.setWidth("450px");

    if (AuthenticationHandler.getInstance().getUser() != null) {
      email.setText(AuthenticationHandler.getInstance().getUser().getEmail());
      contactName.setText(AuthenticationHandler.getInstance().getUser().getFirstname() + " " + AuthenticationHandler.getInstance().getUser().getLastname());
    }


    VerticalPanel vp = new VerticalPanel();
    vp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

    Label advertLabel = new Label(BaseApplication.getMessages().getString("advertiseWithUs", "Advertise With Us!"));
    advertLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
    vp.add(advertLabel);
    HTML advertiseDescription = new HTML(
        BaseApplication
            .getMessages()
            .getString(
                "advertiseDescription",
                "<BR>If you are interested in advertising on this website please fill out the form below. We will be happy to work with you regarding cost, ad placement and frequency.<BR><BR>"));
    vp.add(advertiseDescription);

    contactName.getElement().setAttribute("placeHolder", BaseApplication.getMessages().getString("contactName", "Contact Name"));
    email.getElement().setAttribute("placeHolder", BaseApplication.getMessages().getString("email", "Email"));
    company.getElement().setAttribute("placeHolder", BaseApplication.getMessages().getString("company", "Company"));
    phone.getElement().setAttribute("placeHolder", BaseApplication.getMessages().getString("phone", "Phone"));
    comments.getElement().setAttribute("placeHolder", BaseApplication.getMessages().getString("message", "Message"));

    FlexTable formTable = new FlexTable();
    formTable.setWidget(0, 0, contactName);
    formTable.setWidget(1, 0, email);
    formTable.setWidget(2, 0, company);
    formTable.setWidget(3, 0, phone);
    formTable.setWidget(4, 0, comments);

    vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    vp.add(formTable);

    vp.setSize("500px", "240px");

    setContent(vp);

    setValidatorCallback(this);
    setCallback(this);
  }

  public boolean validate() {
    String errorStr = "";
    if (StringUtils.isEmpty(contactName.getText())) {
      errorStr += "Contact Name is missing.<BR>";
    }
    if (StringUtils.isEmpty(email.getText())) {
      errorStr += "E-Mail address is missing.<BR>";
    }
    if (!StringUtils.isEmpty(errorStr)) {
      MessageDialogBox.alert("Error", errorStr);
    } else {
      // perform the submission
      MethodCallback<Boolean> callback = new MethodCallback<Boolean>() {
        public void onFailure(Method method, Throwable exception) {
          MessageDialogBox.alert("Error", exception.getMessage());
        }

        public void onSuccess(Method method, Boolean response) {
          if (response) {
            hide();
            MessageDialogBox.alert("Info", "Your submission has been sent.  Thank you.");
          } else {
            MessageDialogBox.alert("Error", "There was an error submitting your feedback.  Please try again later.");
          }
        }
      };

      AdvertisingInfo info = new AdvertisingInfo();
      info.setContactName(contactName.getText());
      info.setEmail(email.getText());
      info.setCompany(company.getText());
      info.setPhone(phone.getText());
      info.setComments(comments.getText());

      ResourceCache.getBaseResource().submitAdvertisingInfo(info, callback);
    }
    return false;
  }

  public void cancelPressed() {
    hide();
  }

  public void okPressed() {
  }

}

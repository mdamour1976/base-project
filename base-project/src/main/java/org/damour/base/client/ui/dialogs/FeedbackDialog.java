package org.damour.base.client.ui.dialogs;

import org.damour.base.client.BaseApplication;
import org.damour.base.client.objects.Feedback;
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

public class FeedbackDialog extends PromptDialogBox implements IDialogValidatorCallback, IDialogCallback {

  private static TextBox contactName = new TextBox();
  private static TextBox email = new TextBox();
  private static TextBox phone = new TextBox();
  private static TextArea comments = new TextArea();

  public FeedbackDialog() {
    super(BaseApplication.getMessages().getString("feedback", "Feedback"), "Submit", null, "Cancel", false, true);
    super.setAllowEnterSubmit(false);

    contactName.setWidth("350px");
    email.setWidth("350px");
    phone.setWidth("350px");
    comments.setVisibleLines(4);
    comments.setWidth("450px");    
    
    if (AuthenticationHandler.getInstance().getUser() != null) {
      email.setText(AuthenticationHandler.getInstance().getUser().getEmail());
      contactName.setText(AuthenticationHandler.getInstance().getUser().getFirstname() + " " + AuthenticationHandler.getInstance().getUser().getLastname());
    }

    VerticalPanel vp = new VerticalPanel();
    vp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

    Label feedbackLabel = new Label(BaseApplication.getMessages().getString("feedbackTitle", "Give Us Feedback"));
    feedbackLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
    vp.add(feedbackLabel);
    HTML feedbackDescription = new HTML(
        BaseApplication
            .getMessages()
            .getString(
                "feedbackDescription",
                "<BR>If you are reporting a bug, please include as much detail as you can so that our engineers can work to resolve the problem as quickly as possible.<BR><BR>If you are submitting a feature request or enhancement, we will attempt to work with you if we accept your idea.  You may keep your description brief.<BR><BR>For other comments and criticisms, simply fill out as much of the form as you want.  <BR><BR>"));
    vp.add(feedbackDescription);

    contactName.getElement().setAttribute("placeHolder", BaseApplication.getMessages().getString("contactName", "Contact Name"));
    email.getElement().setAttribute("placeHolder", BaseApplication.getMessages().getString("email", "Email"));
    phone.getElement().setAttribute("placeHolder", BaseApplication.getMessages().getString("phone", "Phone"));
    comments.getElement().setAttribute("placeHolder", BaseApplication.getMessages().getString("message", "Message"));
    
    FlexTable formTable = new FlexTable();
    formTable.setWidget(0, 0, contactName);
    formTable.setWidget(1, 0, email);
    formTable.setWidget(2, 0, phone);
    formTable.setWidget(3, 0, comments);

    vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    vp.add(formTable);

    vp.setSize("500px", "240px");

    setContent(vp);

    setValidatorCallback(this);
    setCallback(this);
  }

  public boolean validate() {
    String errorStr = "";
    if (StringUtils.isEmpty(email.getText())) {
      errorStr += "E-Mail address is missing.<BR>";
    }
    if (StringUtils.isEmpty(comments.getText())) {
      errorStr += "Comments are missing.<BR>";
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
      Feedback feedback = new Feedback();
      feedback.setContactName(contactName.getText());
      feedback.setEmail(email.getText());
      feedback.setPhone(phone.getText());
      feedback.setComments(comments.getText());
      ResourceCache.getBaseResource().submitFeedback(feedback, callback);
    }
    return false;
  }

  public void cancelPressed() {
    hide();
  }

  public void okPressed() {
  }

}

package org.damour.base.client.ui.dialogs;

import java.util.Date;

import org.damour.base.client.objects.Feedback;
import org.damour.base.client.service.ResourceCache;
import org.damour.base.client.ui.authentication.AuthenticationHandler;
import org.damour.base.client.ui.buttons.Button;
import org.damour.base.client.ui.datepicker.MyDatePicker;
import org.damour.base.client.utils.StringUtils;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

public class FeedbackPanel extends VerticalPanel {

  private TextBox contactName = new TextBox();
  private TextBox email = new TextBox();
  private TextBox phone = new TextBox();
  private TextArea comments = new TextArea();
  private Button sendButton = new Button("Send");
  private DateBox datePicker;
  private String subject;

  public FeedbackPanel(final String title, final String subject, boolean showDatePicker) {
    this.subject = subject;
    if (AuthenticationHandler.getInstance().getUser() != null) {
      email.setText(AuthenticationHandler.getInstance().getUser().getEmail());
      contactName.setText(AuthenticationHandler.getInstance().getUser().getFirstname() + " " + AuthenticationHandler.getInstance().getUser().getLastname());
    }
    email.getElement().setAttribute("placeHolder", "Email");
    contactName.getElement().setAttribute("placeHolder", "Name");
    phone.getElement().setAttribute("placeHolder", "Phone");
    comments.setVisibleLines(5);
    comments.getElement().setAttribute("placeHolder", "Message");

    contactName.setWidth("250px");
    phone.setWidth("250px");
    email.setWidth("250px");
    comments.setWidth("250px");

    setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

    Label feedbackLabel = new Label(title);
    feedbackLabel.setStyleName("feedbackTitle");
    add(feedbackLabel);

    FlexTable formTable = new FlexTable();

    int row = 0;
    formTable.setWidget(row++, 0, contactName);
    formTable.setWidget(row++, 0, email);
    formTable.setWidget(row++, 0, phone);
    if (showDatePicker) {
      DefaultFormat format = new DefaultFormat(DateTimeFormat.getFormat(PredefinedFormat.DATE_LONG));
      datePicker = new DateBox(new MyDatePicker(), new Date(), format);
      datePicker.setWidth("250px");
      datePicker.getElement().setAttribute("placeHolder", "Date");
      formTable.setWidget(row++, 0, datePicker);
    }
    formTable.setWidget(row++, 0, comments);

    sendButton.setCommand(new Command() {
      public void execute() {
        sendButton.setEnabled(false);
        submit();
      }
    });
    formTable.setWidget(row++, 1, sendButton);

    formTable.getCellFormatter().setAlignment(--row, 1, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);
    while (row > 0) {
      formTable.getFlexCellFormatter().setColSpan(--row, 0, 2);
    }

    setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    add(formTable);
  }

  public boolean submit() {
    String errorStr = "";
    if (StringUtils.isEmpty(email.getText()) && StringUtils.isEmpty(phone.getText())) {
      errorStr += "Please enter an Email address or phone number.<BR>";
    }
    if (StringUtils.isEmpty(comments.getText())) {
      errorStr += "Please enter a message in the form.<BR>";
    }
    if (!StringUtils.isEmpty(errorStr)) {
      MessageDialogBox.alert("Error", errorStr);
      sendButton.setEnabled(true);
    } else {
      // perform the submission
      MethodCallback<Boolean> callback = new MethodCallback<Boolean>() {
        public void onFailure(Method method, Throwable exception) {
          sendButton.setEnabled(true);
          MessageDialogBox.alert("Error", exception.getMessage());
        }

        public void onSuccess(Method method, Boolean response) {
          sendButton.setEnabled(true);
          if (response) {
            MessageDialogBox.alert("Info", "Your submission has been sent.  Thank you.");
            comments.setText("");
          } else {
            MessageDialogBox.alert("Error", "There was an error submitting your feedback.  Please try again later.");
          }
        }
      };
      Feedback feedback = new Feedback();
      feedback.setContactName(contactName.getText());
      feedback.setEmail(email.getText());
      feedback.setPhone(phone.getText());
      if (datePicker != null) {
        feedback.setDate(datePicker.getValue());
      }
      if (subject != null) {
        feedback.setSubject(subject.replaceAll("\\{date\\}", DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM).format(datePicker.getValue())));
      }
      feedback.setComments(comments.getText());
      ResourceCache.getBaseResource().submitFeedback(feedback, callback);
    }
    return false;
  }

}

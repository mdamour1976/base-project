package org.damour.base.client.ui.authentication;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.damour.base.client.BaseApplication;
import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.objects.StringWrapper;
import org.damour.base.client.objects.User;
import org.damour.base.client.service.ResourceCache;
import org.damour.base.client.ui.GlassPanel;
import org.damour.base.client.ui.buttons.Button;
import org.damour.base.client.ui.buttons.IconButton;
import org.damour.base.client.ui.datepicker.MyDatePicker;
import org.damour.base.client.ui.dialogs.IDialogCallback;
import org.damour.base.client.ui.dialogs.IDialogValidatorCallback;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.ui.dialogs.PromptDialogBox;
import org.damour.base.client.ui.password.SecurePasswordVerification;
import org.damour.base.client.utils.StringUtils;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DateBox.DefaultFormat;

public class AuthenticationHandler {

  private List<IAuthenticationListener> listeners = new ArrayList<IAuthenticationListener>();

  private User user;
  TextBox usernameTextBox = new TextBox();
  PasswordTextBox passwordTextBox = new PasswordTextBox();
  PasswordTextBox passwordConfirm = new PasswordTextBox();
  TextBox passwordHint = new TextBox();
  TextBox emailAddress = new TextBox();
  TextBox firstname = new TextBox();
  TextBox lastname = new TextBox();
  CheckBox disclaimerCheckBox = new CheckBox(BaseApplication.getMessages().getString("iHaveReadDisclaimer",
      "I have read and agree with the disclaimer statement"));
  DefaultFormat format = new DefaultFormat(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));
  DateBox dateBox = new DateBox(new MyDatePicker(), new Date(), format);

  final IconButton facebookLoginButton = new IconButton(BaseApplication.getMessages().getString("facebookLogin", "Login with Facebook"), false,
      BaseImageBundle.images.facebook16(), BaseImageBundle.images.facebook16(), BaseImageBundle.images.facebook16(), BaseImageBundle.images.facebook16());
  final Button signupButton = new Button(BaseApplication.getMessages().getString("signup", "Signup"));
  PromptDialogBox loginDialog = new PromptDialogBox(BaseApplication.getMessages().getString("login", "Login"), BaseApplication.getMessages().getString("login",
      "Login"), signupButton, BaseApplication.getMessages().getString("cancel", "Cancel"), false, true);
  PromptDialogBox accountDialog = new PromptDialogBox(BaseApplication.getMessages().getString("editAccount", "Edit Account"), BaseApplication.getMessages()
      .getString("submit", "Submit"), null, BaseApplication.getMessages().getString("cancel", "Cancel"), false, true);

  Label hintLink = new Label(BaseApplication.getMessages().getString("hint", "Hint"));

  Button readDisclaimer = new Button(BaseApplication.getMessages().getString("viewDisclaimer", "View Disclaimer"));

  Image captchaValidationImage = new Image();
  TextBox captchaValidationTextBox = new TextBox();

  private static AuthenticationHandler instance;

  public static AuthenticationHandler getInstance() {
    if (instance == null) {
      instance = new AuthenticationHandler();
    }
    return instance;
  }

  public static AuthenticationHandler getNewInstance() {
    return new AuthenticationHandler();
  }

  private AuthenticationHandler() {
    loginDialog.setContent(new FlexTable());

    final Date possibleBirthday = new Date();
    possibleBirthday.setYear(possibleBirthday.getYear() - 25);
    dateBox.setValue(possibleBirthday);

    usernameTextBox.setVisibleLength(30);
    passwordTextBox.setVisibleLength(30);
    passwordTextBox.addFocusHandler(new FocusHandler() {
      public void onFocus(FocusEvent event) {
        passwordTextBox.selectAll();
      }
    });

    passwordConfirm.setVisibleLength(20);
    passwordConfirm.addFocusHandler(new FocusHandler() {
      public void onFocus(FocusEvent event) {
        passwordConfirm.selectAll();
      }
    });

    loginDialog.setValidatorCallback(new IDialogValidatorCallback() {
      public boolean validate() {
        boolean valid = true;
        String validationMessage = "";
        if (usernameTextBox == null || "".equals(usernameTextBox.getText())) {
          validationMessage += BaseApplication.getMessages().getString("mustEnterUsername", "You must enter a username.");
          valid = false;
        }
        if (passwordTextBox == null || "".equals(passwordTextBox.getText())) {
          validationMessage += (usernameTextBox == null || "".equals(usernameTextBox.getText())) ? "<BR>" : ""
              + BaseApplication.getMessages().getString("mustEnterPassword", "You must enter a password.");
          valid = false;
        }
        if (!valid) {
          final MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("validationFailed", "Validation Failed"),
              validationMessage, true, true, true);
          dialog.center();
        }
        return valid;
      }
    });

    loginDialog.setCallback(new IDialogCallback() {
      public void okPressed() {
        login(usernameTextBox.getText(), passwordTextBox.getText(), false);
      }

      public void cancelPressed() {
      }
    });

    signupButton.addClickHandler(new ClickHandler() {
      public void onClick(final ClickEvent event) {
        loginDialog.hide();
        // create an account, prepopulate UI with values from login dialog (user/pass)
        showNewAccountDialog(true);
      }
    });

    facebookLoginButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        facebookLogin(AuthenticationHandler.this);
      }
    });

    hintLink.setTitle("Retrieve your password hint.");
    hintLink.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
    hintLink.setStyleName("link");
    hintLink.addClickHandler(new ClickHandler() {
      public void onClick(final ClickEvent event) {
        if (usernameTextBox.getText() == null || "".equals(usernameTextBox.getText())) {
          final MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("error", "Error"), BaseApplication.getMessages()
              .getString("enterUsername", "Enter your username."), true, true, true);
          dialog.center();
        } else {
          getPasswordHint(usernameTextBox.getText());
        }
      }
    });
    readDisclaimer.addClickHandler(new ClickHandler() {

      public void onClick(final ClickEvent event) {
        String companyName = BaseApplication.getMessages().getString("companyName", "the Company");
        String disclaimerText = "By using this website, you acknowledge that you have read and agree to these terms.  Department staff may revise these terms periodically.  If you continue to use this website after changes are made to these terms, it will mean that you accept such changes.  If at any time you do not wish to accept the Terms, you may choose not to use this website.";
        disclaimerText += "<BR><BR>The information contained in this website is for general information purposes only. The information is provided by {0} and whilst we endeavour to keep the information up-to-date and correct, we make no representations or warranties of any kind, express or implied, about the completeness, accuracy, reliability, suitability or availability with respect to the website or the information, products, services, or related graphics contained on the website for any purpose. Any reliance you place on such information is therefore strictly at your own risk.";
        disclaimerText += "<BR><BR>In no event will we be liable for any loss or damage including without limitation, indirect or consequential loss or damage, or any loss or damage whatsoever arising from loss of data or profits arising out of, or in connection with, the use of this website.";
        disclaimerText += "<BR><BR>Through this website you are able to link to other websites which are not under the control of {0}. We have no control over the nature, content and availability of those sites. The inclusion of any links does not necessarily imply a recommendation or endorse the views expressed within them.";
        disclaimerText += "<BR><BR>Every effort is made to keep the website up and running smoothly. However, {0} takes no responsibility for, and will not be liable for, the website being temporarily unavailable due to technical issues beyond our control.";
        disclaimerText = BaseApplication.getMessages().getString("disclaimerText", disclaimerText, companyName);
        final MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("disclaimerAndTerms", "Disclaimer & Terms of Use"),
            disclaimerText, true, true, true);
        dialog.setWidth("600px");
        dialog.center();
      }
    });
    disclaimerCheckBox.setTitle(BaseApplication.getMessages().getString("disclaimerAgree", "You must read and agree in order to continue."));
    // present account dialog
    accountDialog.setText(BaseApplication.getMessages().getString("newAccount", "New Account"));

    passwordHint.setTitle(BaseApplication.getMessages().getString("hintTitle", "Give yourself a hint in case you forget your password"));
    passwordHint.setVisibleLength(40);

    emailAddress.setTitle(BaseApplication.getMessages().getString("enterEmail", "Enter your email address"));
    emailAddress.setVisibleLength(40);

    firstname.setTitle(BaseApplication.getMessages().getString("enterFirstname", "Enter your firstname"));
    lastname.setTitle(BaseApplication.getMessages().getString("enterLastname", "Enter your lastname"));

    captchaValidationImage.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent sender) {
        createCaptchaImage();
      }
    });
  }

  public void showLoginDialog(boolean forceRecenter) {

    Label signonMsgLabel = new Label(
        BaseApplication.getMessages().getString("toSignOnEnterUserAndPass", "To sign on, enter your username and password below."), true);
    signonMsgLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

    usernameTextBox.setReadOnly(false);
    // present login dialog
    final FlexTable contentPanel = (FlexTable) loginDialog.getContent();
    int row = 0;
    contentPanel.setWidget(row++, 0, new HTML("&nbsp;"));
    contentPanel.setWidget(row, 0, signonMsgLabel);
    contentPanel.getFlexCellFormatter().setColSpan(row++, 0, 3);

    Label orLabel = new Label("or");
    orLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    contentPanel.setWidget(row, 0, orLabel);
    contentPanel.getFlexCellFormatter().setColSpan(row++, 0, 3);

    contentPanel.setWidget(row, 0, facebookLoginButton);
    contentPanel.getFlexCellFormatter().setColSpan(row, 0, 3);
    contentPanel.getFlexCellFormatter().setHorizontalAlignment(row++, 0, HasHorizontalAlignment.ALIGN_CENTER);

    contentPanel.setWidget(row++, 0, new HTML("&nbsp;"));

    Label usernameLabel = new Label(BaseApplication.getMessages().getString("usernameColon", "Username:"));
    usernameLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    contentPanel.setWidget(row, 0, usernameLabel);
    contentPanel.setWidget(row, 1, usernameTextBox);
    contentPanel.setWidget(row, 2, hintLink);
    contentPanel.getCellFormatter().setHorizontalAlignment(row++, 2, HasHorizontalAlignment.ALIGN_LEFT);
    Label passwordLabel = new Label(BaseApplication.getMessages().getString("passwordColon", "Password:"));
    passwordLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    contentPanel.setWidget(row, 0, passwordLabel);
    contentPanel.setWidget(row, 1, passwordTextBox);
    contentPanel.setWidget(row, 2, new HTML("&nbsp;"));

    if (forceRecenter) {
      loginDialog.center();
    }
    usernameTextBox.setFocus(true);
  }

  public void getPasswordHint(final String username) {
    final MethodCallback<StringWrapper> callback = new MethodCallback<StringWrapper>() {

      public void onFailure(Method method, Throwable exception) {
        final MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("error", "Error"), exception.getMessage(), true, true, true);
        dialog.center();
      }

      public void onSuccess(Method method, StringWrapper hint) {
        if (hint == null) {
          MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("error", "Error"), "Could not retrieve password hint.", true,
              true, true);
          dialog.center();
        } else {
          MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("passwordHint", "Password Hint"), BaseApplication
              .getMessages().getString("yourPasswordHintIs", "Your password hint is: <b>{0}</b>", hint.toString()), true, true, true);
          dialog.center();
        }
      };
    };
    ResourceCache.getUserResource().getLoginHint(username, callback);
  }

  public void showNewAccountDialog(final boolean showLoginIfCancelPressed) {
    usernameTextBox.setReadOnly(false);

    final HTML usernameLabel = new HTML("<b>" + BaseApplication.getMessages().getString("username", "Username") + "</b>");
    usernameLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML passwordLabel = new HTML("<b>" + BaseApplication.getMessages().getString("password", "Password") + "</b>");
    passwordLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML passwordConfirmLabel = new HTML("<b>" + BaseApplication.getMessages().getString("confirmPassword", "Confirm Password") + "</b>");
    passwordConfirmLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML passwordStrengthLabel = new HTML(BaseApplication.getMessages().getString("passwordStrength", "Password Strength"));
    passwordStrengthLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML passwordHintLabel = new HTML("<b>" + BaseApplication.getMessages().getString("passwordHint", "Password Hint") + "</b>");
    passwordHintLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML firstnameLabel = new HTML("<b>" + BaseApplication.getMessages().getString("firstname", "Firstname") + "</b>");
    firstnameLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML lastnameLabel = new HTML("<b>" + BaseApplication.getMessages().getString("lastname", "Lastname") + "</b>");
    lastnameLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML emailLabel = new HTML("<b>" + BaseApplication.getMessages().getString("email", "Email") + "</b>");
    emailLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML birthdayLabel = new HTML("<b>" + BaseApplication.getMessages().getString("birthday", "Birthday") + "</b>");
    birthdayLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final FlexTable contentPanel = new FlexTable();
    int row = 0;
    contentPanel.setWidget(row, 0, usernameLabel);
    contentPanel.setWidget(row, 1, usernameTextBox);
    contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
    row++;

    HorizontalPanel passwordPanel = new HorizontalPanel();
    passwordPanel.add(passwordTextBox);
    passwordPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    passwordPanel.add(new SecurePasswordVerification(true, passwordTextBox, passwordConfirm));

    contentPanel.setWidget(row, 0, passwordLabel);
    contentPanel.setWidget(row, 1, passwordPanel);
    contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
    row++;

    contentPanel.setWidget(row, 0, passwordConfirmLabel);
    contentPanel.setWidget(row, 1, passwordConfirm);
    contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
    row++;

    // use password widgets
    // contentPanel.setWidget(row, 0, passwordStrengthLabel);
    // contentPanel.setWidget(row, 1, new SecurePasswordVerification(passwordTextBox, passwordConfirm));
    // contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
    // row++;

    contentPanel.setWidget(row, 0, passwordHintLabel);
    contentPanel.setWidget(row, 1, passwordHint);
    contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
    row++;

    contentPanel.setWidget(row, 0, firstnameLabel);
    contentPanel.setWidget(row, 1, firstname);
    contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
    row++;

    contentPanel.setWidget(row, 0, lastnameLabel);
    contentPanel.setWidget(row, 1, lastname);
    contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
    row++;

    contentPanel.setWidget(row, 0, emailLabel);
    contentPanel.setWidget(row, 1, emailAddress);
    contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
    row++;

    contentPanel.setWidget(row, 0, birthdayLabel);
    contentPanel.setWidget(row, 1, dateBox);
    contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
    row++;

    contentPanel.setWidget(row++, 1, new HTML("<HR>"));
    contentPanel.setText(row, 1, BaseApplication.getMessages().getString("captchaInstructions", "Type the characters you see in the picture below."));
    contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
    row++;

    createCaptchaImage();
    captchaValidationImage.setTitle(BaseApplication.getMessages().getString("captchaTitle", "Click to load a new validation image"));
    captchaValidationImage.setStyleName("captchaImage");
    contentPanel.setWidget(row, 1, captchaValidationImage);
    contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
    row++;

    contentPanel.setWidget(row, 1, captchaValidationTextBox);
    contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
    row++;

    contentPanel.setWidget(row++, 1, new HTML("<HR>"));
    HorizontalPanel disclaimerButtonPanel = new HorizontalPanel();
    disclaimerButtonPanel.add(readDisclaimer);
    contentPanel.setWidget(row, 1, disclaimerButtonPanel);
    contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
    row++;

    contentPanel.setWidget(row, 1, disclaimerCheckBox);
    contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
    row++;

    accountDialog.setCallback(new IDialogCallback() {
      public void okPressed() {
        boolean validationFailed = false;
        String validationMessage = "";
        if (usernameTextBox.getText() == null || "".equals(usernameTextBox.getText())) {
          validationMessage += BaseApplication.getMessages().getString("mustEnterUsername", "You must enter a username.") + "<BR>";
          validationFailed = true;
        }
        if (passwordTextBox.getText() == null || "".equals(passwordTextBox.getText())) {
          validationMessage += BaseApplication.getMessages().getString("mustEnterPassword", "You must enter a password and a confirmation password.") + "<BR>";
          validationFailed = true;
        }
        if (!passwordTextBox.getText().equals(passwordConfirm.getText())) {
          validationMessage += BaseApplication.getMessages().getString("passwordsDoNotMatch", "Passwords do not match.") + "<BR>";
          validationFailed = true;
        }
        if (emailAddress.getText() == null || "".equals(emailAddress.getText()) || emailAddress.getText().indexOf("@") == -1) {
          validationMessage += BaseApplication.getMessages().getString("mustEnterValidEmail", "You must enter a valid email address.") + "<BR>";
          validationFailed = true;
        }
        if (dateBox.getValue() == null) {
          validationMessage += BaseApplication.getMessages().getString("mustEnterBirthdate", "You must enter your birthdate.") + "<BR>";
          validationFailed = true;
        }

        if (StringUtils.isEmpty(captchaValidationTextBox.getText())) {
          validationMessage += BaseApplication.getMessages().getString("captchaValidationFailed", "You must enter validation text.") + "<BR>";
          validationFailed = true;
        }

        if (!disclaimerCheckBox.getValue()) {
          validationMessage += BaseApplication.getMessages().getString("mustReadDisclaimer",
              "You must read and agree with the disclaimer statement to continue.")
              + "<BR>";
          validationFailed = true;
        }
        if (validationFailed) {
          final MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("validationFailed", "Validation Failed"),
              validationMessage, true, true, true);
          dialog.setCallback(new IDialogCallback() {
            public void okPressed() {
              accountDialog.center();
            }

            public void cancelPressed() {
            }
          });
          dialog.center();
          return;
        }
        createNewAccount(usernameTextBox.getText(), firstname.getText(), lastname.getText(), passwordTextBox.getText(), passwordHint.getText(),
            emailAddress.getText(), dateBox.getValue().getTime());
      }

      public void cancelPressed() {
        if (showLoginIfCancelPressed) {
          showLoginDialog(true);
        }
      }
    });

    accountDialog.setContent(contentPanel);
    accountDialog.setText(BaseApplication.getMessages().getString("newAccount", "New Account"));
    accountDialog.center();
    usernameTextBox.setFocus(true);
  }

  private void createCaptchaImage() {
    captchaValidationImage.setUrl(BaseApplication.getSettings().getString("CaptchaService", BaseApplication.CAPTCHA_SERVICE_PATH) + "?attempt="
        + System.currentTimeMillis());
  }

  public void showEditAccountDialog(final User user) {
    this.user = user;
    passwordTextBox.setText("");
    passwordConfirm.setText("");
    usernameTextBox.setReadOnly(true);

    final HTML usernameLabel = new HTML("<b>" + BaseApplication.getMessages().getString("username", "Username") + "</b>");
    usernameLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML passwordLabel = new HTML("<b>" + BaseApplication.getMessages().getString("password", "Password") + "</b>");
    passwordLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML passwordConfirmLabel = new HTML("<b>" + BaseApplication.getMessages().getString("confirmPassword", "Confirm Password") + "</b>");
    passwordConfirmLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML passwordHintLabel = new HTML("<b>" + BaseApplication.getMessages().getString("passwordHint", "Password Hint") + "</b>");
    passwordHintLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML passwordStrengthLabel = new HTML(BaseApplication.getMessages().getString("passwordStrength", "Password Strength"));
    passwordStrengthLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML firstnameLabel = new HTML("<b>" + BaseApplication.getMessages().getString("firstname", "Firstname") + "</b>");
    firstnameLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML lastnameLabel = new HTML("<b>" + BaseApplication.getMessages().getString("lastname", "Lastname") + "</b>");
    lastnameLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML emailLabel = new HTML("<b>" + BaseApplication.getMessages().getString("email", "Email") + "</b>");
    emailLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final HTML birthdayLabel = new HTML("<b>" + BaseApplication.getMessages().getString("birthday", "Birthday") + "</b>");
    birthdayLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

    final FlexTable contentPanel = new FlexTable();
    int row = 0;
    contentPanel.setWidget(row, 0, usernameLabel);
    contentPanel.setWidget(row, 1, usernameTextBox);
    usernameTextBox.setText(user.getUsername());
    contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
    row++;

    if (!user.isFacebook()) {
      HorizontalPanel passwordPanel = new HorizontalPanel();
      passwordPanel.add(passwordTextBox);
      passwordPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
      passwordPanel.add(new SecurePasswordVerification(true, passwordTextBox, passwordConfirm));

      contentPanel.setWidget(row, 0, passwordLabel);
      contentPanel.setWidget(row, 1, passwordPanel);
      contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
      row++;

      contentPanel.setWidget(row, 0, passwordConfirmLabel);
      contentPanel.setWidget(row, 1, passwordConfirm);
      contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
      row++;

      contentPanel.setWidget(row, 0, passwordHintLabel);
      contentPanel.setWidget(row, 1, passwordHint);
      passwordHint.setText(user.getPasswordHint());
      contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
      row++;
    }

    contentPanel.setWidget(row, 0, firstnameLabel);
    contentPanel.setWidget(row, 1, firstname);
    firstname.setText(user.getFirstname());
    contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
    row++;

    contentPanel.setWidget(row, 0, lastnameLabel);
    contentPanel.setWidget(row, 1, lastname);
    lastname.setText(user.getLastname());
    contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
    row++;

    contentPanel.setWidget(row, 0, emailLabel);
    contentPanel.setWidget(row, 1, emailAddress);
    emailAddress.setText(user.getEmail());
    contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
    row++;

    contentPanel.setWidget(row, 0, birthdayLabel);

    Date date = new Date(user.getBirthday());
    dateBox.setValue(date);
    contentPanel.setWidget(row, 1, dateBox);
    contentPanel.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_LEFT);
    row++;

    accountDialog.setCallback(new IDialogCallback() {
      public void okPressed() {
        boolean validationFailed = false;
        String validationMessage = "";

        if (passwordTextBox.getText() != null && !"".equals(passwordTextBox.getText()) && !passwordTextBox.getText().equals(passwordConfirm.getText())) {
          validationMessage += BaseApplication.getMessages().getString("mustEnterMatchingPasswords",
              "You must enter a matching password and confirmation password.");
          validationMessage += "<BR>";
          validationFailed = true;
        }
        if (emailAddress.getText() == null || "".equals(emailAddress.getText()) || emailAddress.getText().indexOf("@") == -1) {
          validationMessage += BaseApplication.getMessages().getString("mustEnterValidEmail", "You must enter a valid email address.");
          validationMessage += "<BR>";
          validationFailed = true;
        }
        if (dateBox.getValue() == null) {
          validationMessage += BaseApplication.getMessages().getString("mustEnterBirthdate", "You must enter your birthdate.");
          validationMessage += "<BR>";
          validationFailed = true;
        }
        if (validationFailed) {
          final MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("validationFailed", "Validation Failed"),
              validationMessage, true, true, true);
          dialog.setCallback(new IDialogCallback() {
            public void okPressed() {
              accountDialog.center();
            }

            public void cancelPressed() {
            }
          });
          dialog.center();
          return;
        }
        user.setUsername(usernameTextBox.getText());
        user.setFirstname(firstname.getText());
        user.setLastname(lastname.getText());
        user.setPasswordHint(passwordHint.getText());
        user.setEmail(emailAddress.getText());
        user.setBirthday(dateBox.getValue().getTime());
        editAccount(user, passwordTextBox.getText());
      }

      public void cancelPressed() {
      }
    });

    accountDialog.setContent(contentPanel);
    accountDialog.setText(BaseApplication.getMessages().getString("editAccountSettings", "Edit Account Settings"));
    accountDialog.center();
    passwordTextBox.setFocus(true);
  }

  public void login(final String username, final String password, final boolean facebook) {
    final MethodCallback<User> loginCallback = new MethodCallback<User>() {
      public void onFailure(Method method, Throwable exception) {
        MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("error", "Error"), exception.getMessage(), true, true, true);
        dialog.setCallback(new IDialogCallback() {
          public void okPressed() {
            loginDialog.center();
          }

          public void cancelPressed() {
          }
        });
        dialog.center();
      }

      public void onSuccess(Method method, User response) {
        AuthenticationHandler.this.user = response;
        if (user == null) {
          MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("error", "Error"), BaseApplication.getMessages().getString(
              "invalidUsernameOrPassword", "Invalid Username or Password."), true, true, true);
          dialog.center();
        } else {
          loginDialog.hide();
          fireSetAuthenticatedUser(user);
        }
      };
    };
    ResourceCache.getUserResource().login(username, password, facebook, loginCallback);
  }

  public void createNewAccount(final String username, final String firstname, final String lastname, final String password, final String passwordHint,
      final String email, final long birthday) {
    final MethodCallback<User> loginCallback = new MethodCallback<User>() {

      public void onFailure(Method method, Throwable exception) {
        MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("error", "Error"), BaseApplication.getMessages().getString(
            "couldNotCreateAccount", "Could not create new account. {0}", exception.getMessage()), true, true, true);
        dialog.setCallback(new IDialogCallback() {
          public void okPressed() {
            accountDialog.center();
          }

          public void cancelPressed() {
          }
        });
        dialog.center();
      }

      public void onSuccess(Method method, User response) {
        AuthenticationHandler.this.user = response;
        if (user == null) {
          MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("error", "Error"), BaseApplication.getMessages().getString(
              "couldNotCreateAccount", "Could not create new account.  Try entering a different username."), true, true, true);
          dialog.center();
        } else if (user.isValidated()) {
          accountDialog.hide();
          fireSetAuthenticatedUser(user);
        } else if (!user.isValidated()) {
          MessageDialogBox
              .alert(BaseApplication
                  .getMessages()
                  .getString(
                      "confirmEmailForValidation",
                      "<BR>Thank you for signing up. <BR><BR>We just sent you a confirmation email. <BR><BR>Please click the link inside the email and your account will be activated.<BR><BR>"));
        }
      };
    };
    User user = new User();
    user.setUsername(username);
    user.setFirstname(firstname);
    user.setLastname(lastname);
    user.setPasswordHint(passwordHint);
    user.setEmail(email);
    user.setBirthday(birthday);
    ResourceCache.getUserResource().createOrEditAccount(user, password, captchaValidationTextBox.getText().toUpperCase(), loginCallback);
  }

  public void editAccount(User user, String password) {
    final MethodCallback<User> loginCallback = new MethodCallback<User>() {
      public void onFailure(Method method, Throwable exception) {
        MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("error", "Error"), exception.getMessage(), true, true, true);
        dialog.setCallback(new IDialogCallback() {
          public void okPressed() {
            accountDialog.center();
          }

          public void cancelPressed() {
          }
        });
        dialog.center();
      }

      public void onSuccess(Method method, User user) {
        AuthenticationHandler.this.user = user;
        if (user == null) {
          MessageDialogBox dialog = new MessageDialogBox(BaseApplication.getMessages().getString("error", "Error"), BaseApplication.getMessages().getString(
              "couldNotEditAccount", "Could not edit account."), true, true, true);
          dialog.center();
        } else {
          accountDialog.hide();
          fireSetAuthenticatedUser(user);
        }
      };
    };

    ResourceCache.getUserResource().createOrEditAccount(user, password, null, loginCallback);
  }

  public void logout() {
    GlassPanel.setVisible(true);

    final MethodCallback<Boolean> loginCallback = new MethodCallback<Boolean>() {
      public void onFailure(Method method, Throwable exception) {
        GlassPanel.setVisible(false);
        Window.open("/", "_top", "");
      }

      public void onSuccess(Method method, Boolean response) {
        GlassPanel.setVisible(false);
        AuthenticationHandler.this.user = null;
        fireLoggedOut();
      };
    };
    ResourceCache.getUserResource().logout(loginCallback);
  }

  public void handleUserAuthentication(final boolean forcePrompt) {
    final MethodCallback<User> isAuthenticatedCallback = new MethodCallback<User>() {
      public void onFailure(Method method, Throwable caught) {
        if (forcePrompt) {
          // try to login and get data
          showLoginDialog(true);
        } else {
          if (hasFacebookCredentials()) {
            try {
              facebookLogin(AuthenticationHandler.this);
            } catch (Throwable t) {
              fireSetAuthenticatedUser(null);
            }
          } else {
            fireSetAuthenticatedUser(null);
          }
        }
      }

      public void onSuccess(Method method, User user) {
        AuthenticationHandler.this.user = user;
        if (user == null && forcePrompt) {
          // try to login and get data
          showLoginDialog(true);
        } else {
          fireSetAuthenticatedUser(user);
        }
      };
    };
    ResourceCache.getUserResource().getAuthenticatedUser(isAuthenticatedCallback);
  }

  // LoginListener events
  public void fireLoggedOut() {
    for (IAuthenticationListener listener : listeners) {
      listener.loggedOut();
    }
  }

  public void fireSetAuthenticatedUser(User user) {
    for (IAuthenticationListener listener : listeners) {
      listener.setAuthenticatedUser(user);
    }
  }

  public void addLoginListener(IAuthenticationListener listener) {
    listeners.add(listener);
  }

  public void removeLoginListener(IAuthenticationListener listener) {
    listeners.remove(listener);
  }

  /**
   * @return the user
   */
  public User getUser() {
    return user;
  }

  /**
   * @param user
   *          the user to set
   */
  public void setUser(User user) {
    this.user = user;
  }

  public static native boolean hasFacebookCredentials()
  /*-{
    return window.top.FB.getUserID() != null && window.top.FB.getAccessToken() != null;
  }-*/;

  private static native void facebookLogin(AuthenticationHandler authHandler)
  /*-{
    window.top.FB.login(function() {
      window.top.FB.api('/me', function(response) { 
        authHandler.@org.damour.base.client.ui.authentication.AuthenticationHandler::login(Ljava/lang/String;Ljava/lang/String;Z)(response.id, window.top.FB.getAccessToken(), true);
      });
    },{scope: 'email'});
  }-*/;

}

package org.damour.base.client;

import java.util.Date;

import org.damour.base.client.images.BaseImageBundle;
import org.damour.base.client.objects.User;
import org.damour.base.client.ui.admin.AdministratorPanel;
import org.damour.base.client.ui.admin.commands.CreateGroupCommand;
import org.damour.base.client.ui.admin.commands.JoinLeaveGroupsCommand;
import org.damour.base.client.ui.admin.commands.ManageMyGroupsCommand;
import org.damour.base.client.ui.admin.commands.ManagePendingGroupJoinsCommand;
import org.damour.base.client.ui.authentication.AuthenticationHandler;
import org.damour.base.client.ui.authentication.IAuthenticationListener;
import org.damour.base.client.ui.buttons.ComboMenuButton;
import org.damour.base.client.ui.buttons.MenuButtonCommand;
import org.damour.base.client.ui.buttons.ToolbarButton;
import org.damour.base.client.ui.dialogs.AdvertiseDialog;
import org.damour.base.client.ui.dialogs.FeedbackDialog;
import org.damour.base.client.ui.dialogs.PrivacyPolicyDialog;
import org.damour.base.client.ui.dialogs.PromptDialogBox;
import org.damour.base.client.ui.repository.FileManagerPanel;
import org.damour.base.client.ui.toolbar.ToolBar;
import org.damour.base.client.utils.StringUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class BaseApplicationUI extends BaseApplication implements IAuthenticationListener {

  private DeckPanel applicationContentDeck = new DeckPanel();
  private ToolBar applicationToolBar = new ToolBar();

  private User authenticatedUser = null;

  private Widget adminPanel;

  public void loadModule() {
    applicationContentDeck.setHeight("100%");
    applicationContentDeck.setWidth("100%");
    // applicationContentDeck.getElement().getStyle().setOverflow(Overflow.AUTO);
    applicationContentDeck.setAnimationEnabled(true);
    if ("true".equals(getSettings().getString("dockToolbars"))) {
      DockLayoutPanel applicationPanel = new DockLayoutPanel(Unit.PX);
      if ("true".equals(getSettings().getString("showApplicationToolbar"))) {
        applicationPanel.addNorth(buildApplicationToolBar(), Double.parseDouble(getSettings().getString("applicationToolbarHeight", "29")));
      }
      addNorthWidgets(applicationPanel);

      if ("true".equals(getSettings().getString("showApplicationFooter"))) {
        applicationPanel.addSouth(buildFooterPanel(), Double.parseDouble(getSettings().getString("applicationFooterHeight", "60")));
      }
      addSouthWidgets(applicationPanel);

      applicationPanel.add(applicationContentDeck);

      RootLayoutPanel.get().clear();
      RootLayoutPanel.get().add(applicationPanel);
    } else {
      VerticalPanel applicationPanel = new VerticalPanel();
      applicationPanel.setWidth("100%");

      if ("true".equals(getSettings().getString("showApplicationToolbar"))) {
        applicationPanel.add(buildApplicationToolBar());
      }
      addNorthWidgets(applicationPanel);

      applicationPanel.add(applicationContentDeck);

      if ("true".equals(getSettings().getString("showApplicationFooter"))) {
        applicationPanel.add(buildFooterPanel());
      }
      addSouthWidgets(applicationPanel);

      RootPanel.get("content").clear();
      RootPanel.get("content").add(applicationPanel);
    }

    AuthenticationHandler.getInstance().addLoginListener(this);
    AuthenticationHandler.getInstance().handleUserAuthentication(false);

    loadApplication();
  }

  public ToolbarButton buildLoginButton() {
    ToolbarButton loginLink = new ToolbarButton(getMessages().getString("login", "Login"));
    loginLink.setTitle(getMessages().getString("loginTitle", "Login or Create a New Account"));
    loginLink.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        AuthenticationHandler.getInstance().handleUserAuthentication(true);
      }
    });
    return loginLink;
  }

  public ToolbarButton buildLogoutButton() {
    ToolbarButton logoutLink = new ToolbarButton(getMessages().getString("logout", "Logout"));
    logoutLink.setTitle(getMessages().getString("logout", "Logout"));
    logoutLink.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        AuthenticationHandler.getInstance().logout();
      }
    });
    return logoutLink;
  }

  public Widget buildWelcomeLabel() {
    HorizontalPanel container = new HorizontalPanel();
    container.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    Label welcomeLabel = new Label(getMessages().getString("welcome", "Welcome"), false);
    if (getAuthenticatedUser() != null && StringUtils.isEmpty(getAuthenticatedUser().getFirstname())) {
      welcomeLabel.setText(welcomeLabel.getText() + " " + getAuthenticatedUser().getUsername());
    } else if (getAuthenticatedUser() != null) {
      welcomeLabel.setText(welcomeLabel.getText() + " " + getAuthenticatedUser().getFirstname());
    }
    welcomeLabel.setStyleName("welcomeLabel");
    container.add(welcomeLabel);
    if (getAuthenticatedUser() != null && getAuthenticatedUser().isFacebook()) {
      container.add(new HTML("&nbsp;"));
      Image fbImage = new Image(BaseImageBundle.images.facebook16());
      fbImage.setTitle("You are logged in via Facebook");
      VerticalPanel imagePanel = new VerticalPanel();
      HTML space = new HTML("");
      space.getElement().getStyle().setHeight(3, Unit.PX);
      imagePanel.add(space);
      imagePanel.add(fbImage);
      imagePanel.setCellVerticalAlignment(fbImage, HasVerticalAlignment.ALIGN_MIDDLE);
      container.add(imagePanel);
    }
    return container;
  }

  public Widget buildProfileButton(boolean enabled) {

    if ("true".equals(getSettings().getString("showProfileAsButton", "false"))) {
      ToolbarButton profileButton = new ToolbarButton(getMessages().getString("profile", "Profile"), new Command() {
        public void execute() {
          // it is possible the user is 'stale', but HIGHLY unlikely
          AuthenticationHandler.getInstance().showEditAccountDialog(getAuthenticatedUser());
        }
      });
      profileButton.setEnabled(enabled);
      return profileButton;
    }

    MenuBar profileMenu = new MenuBar(true);

    MenuItem editAccountMenuItem = new MenuItem(getMessages().getString("account", "Account"), new MenuButtonCommand() {
      public void execute() {
        popup.hide();
        // it is possible the user is 'stale', but HIGHLY unlikely
        AuthenticationHandler.getInstance().showEditAccountDialog(getAuthenticatedUser());
      }
    });
    editAccountMenuItem.setTitle(getMessages().getString("editYourAccount", "Edit Your Account"));
    profileMenu.addItem(editAccountMenuItem);

    MenuItem myFilesMenuItem = new MenuItem(getMessages().getString("fileManager", "File Manager"), new MenuButtonCommand() {
      public void execute() {
        GWT.runAsync(new RunAsyncCallback() {
          public void onFailure(Throwable reason) {
          }

          public void onSuccess() {
            popup.hide();
            PromptDialogBox dialogBox = new PromptDialogBox(getMessages().getString("fileManager", "File Manager"), getMessages().getString("close", "Close"),
                null, null, false, false);
            // create a new filemanager for this user
            FileManagerPanel fileManager = new FileManagerPanel(getMessages().getString("fileManager", "File Manager"));
            dialogBox.setContent(fileManager);
            dialogBox.center();
          }
        });
      }
    });
    myFilesMenuItem.setTitle(getMessages().getString("manageFiles", "Manage Files"));
    profileMenu.addItem(myFilesMenuItem);

    ComboMenuButton menuButton = new ComboMenuButton(getMessages().getString("profile", "Profile"), profileMenu);
    menuButton.setEnabled(enabled);
    menuButton.setTitle("Edit account, profile, photos and more");
    return menuButton;
  }

  public ComboMenuButton buildManageGroupsButton(boolean enabled) {
    MenuBar groupsMenu = new MenuBar(true);
    MenuItem manageGroupsMenuItem = new MenuItem("Manage My Groups", new ManageMyGroupsCommand(getAuthenticatedUser()));
    manageGroupsMenuItem.setTitle("Manage groups which you administer");
    groupsMenu.addItem(manageGroupsMenuItem);
    groupsMenu.addItem("Manage Group Requests", new ManagePendingGroupJoinsCommand(getAuthenticatedUser()));
    groupsMenu.addItem("Create New Group", new CreateGroupCommand(getAuthenticatedUser(), null));
    groupsMenu.addItem("Join/Leave Groups", new JoinLeaveGroupsCommand(getAuthenticatedUser()));

    ComboMenuButton menuButton = new ComboMenuButton(getMessages().getString("groups", "Groups"), groupsMenu);
    menuButton.setEnabled(enabled);
    menuButton.setTitle("Create, manage or join groups");
    return menuButton;
  }

  public ToolbarButton buildAdminButton() {
    final ToolbarButton adminLink = new ToolbarButton(getMessages().getString("administration", "Administration"));
    adminLink.addClickHandler(new ClickHandler() {
      public void onClick(final ClickEvent event) {
        loadAdmin(true);
      }
    });
    return adminLink;
  }

  public void loadAdmin(final boolean addHistoryItem) {
    if (getAuthenticatedUser() == null || !getAuthenticatedUser().isAdministrator()) {
      return;
    }
    if (addHistoryItem) {
      History.newItem("view=admin", false);
    }
    GWT.runAsync(new RunAsyncCallback() {
      public void onFailure(Throwable reason) {
      }

      public void onSuccess() {
        if (adminPanel == null) {
          adminPanel = new AdministratorPanel(getAuthenticatedUser());
          applicationContentDeck.add(adminPanel);
        }
        ((AdministratorPanel) adminPanel).activate();
        applicationContentDeck.showWidget(applicationContentDeck.getWidgetIndex(adminPanel));
        Timer t = new Timer() {
          public void run() {
            adminPanel.getElement().getParentElement().getStyle().setOverflow(Overflow.AUTO);
          }
        };
        t.schedule(500);
      }
    });
  }

  public Widget buildFooterPanel() {
    VerticalPanel footerPanel = new VerticalPanel();
    footerPanel.setStyleName("footerPanel");
    footerPanel.setWidth("100%");

    FlexTable footerLinkPanel = new FlexTable();
    footerLinkPanel.setCellPadding(0);
    footerLinkPanel.setCellSpacing(5);
    footerLinkPanel.setStyleName("footerLinkPanel");

    Label advertiseLink = new Label(getMessages().getString("advertise", "Advertise"), false);
    advertiseLink.setTitle(getMessages().getString("advertiseTitle", "Advertise With Us"));
    advertiseLink.setStyleName("footerLink");
    advertiseLink.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        // show advertise with us dialog
        AdvertiseDialog ad = new AdvertiseDialog();
        ad.center();
      }
    });

    Label feedbackLink = new Label(getMessages().getString("feedback", "Feedback"), false);
    feedbackLink.setTitle(getMessages().getString("feedbackTitle", "Give Us Feedback"));
    feedbackLink.setStyleName("footerLink");
    feedbackLink.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        // show advertise with us dialog
        FeedbackDialog fd = new FeedbackDialog();
        fd.center();
      }
    });

    Label privacyLink = new Label(getMessages().getString("privacy", "Privacy"), false);
    privacyLink.setTitle(getMessages().getString("privacyTitle", "View Our Privacy Policy"));
    privacyLink.setStyleName("footerLink");
    privacyLink.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        // show privacy dialog
        PrivacyPolicyDialog ppd = new PrivacyPolicyDialog();
        ppd.center();
      }
    });

    int linkCol = -1;
    footerLinkPanel.setWidth("100%");
    footerLinkPanel.setWidget(0, ++linkCol, new Label("", false));
    footerLinkPanel.getCellFormatter().setWidth(0, linkCol, "50%");
    footerLinkPanel.setWidget(0, ++linkCol, advertiseLink);
    footerLinkPanel.setWidget(0, ++linkCol, feedbackLink);
    footerLinkPanel.setWidget(0, ++linkCol, privacyLink);
    footerLinkPanel.setWidget(0, ++linkCol, new Label("", false));
    footerLinkPanel.getCellFormatter().setWidth(0, linkCol, "50%");

    footerPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    footerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    footerPanel.add(footerLinkPanel);

    FlexTable footerBorderPanel = new FlexTable();
    footerBorderPanel.setCellPadding(0);
    footerBorderPanel.setCellSpacing(0);
    footerBorderPanel.setWidth("100%");
    footerBorderPanel.setStyleName("footerBorderPanel");
    footerBorderPanel.getCellFormatter().setHeight(0, 0, "12px");
    footerPanel.add(footerBorderPanel);

    VerticalPanel footerGradientPanelWrapper = new VerticalPanel();
    footerGradientPanelWrapper.setWidth("100%");
    footerGradientPanelWrapper.setStyleName("footerGradientPanel");

    final FlexTable footerGradientPanel = new FlexTable();
    footerGradientPanel.setCellPadding(0);
    footerGradientPanel.setCellSpacing(0);
    footerGradientPanel.setWidth("100%");

    linkCol = -1;
    int row = -1;

    DateTimeFormat yearFormat = DateTimeFormat.getFormat("yyyy");
    String date = yearFormat.format(new Date());
    String company = BaseApplication.getMessages().getString("companyName", "Your Company");
    String copyright = "Copyright &#169 2007-" + date + " " + company + ".  All rights reserved.";

    footerGradientPanel.setHTML(++row, ++linkCol, getMessages().getString("copyrightFooter", copyright, new String[] { date, company }));
    footerGradientPanel.getCellFormatter().setHorizontalAlignment(row, linkCol, HasHorizontalAlignment.ALIGN_CENTER);

    footerGradientPanelWrapper.add(footerGradientPanel);
    footerPanel.add(footerGradientPanelWrapper);
    return footerPanel;
  }

  // override if desired
  public Widget buildApplicationToolBar() {
    getApplicationToolBar().clear();

    if ("true".equals(getSettings().getString("showWelcomeOnToolbar", "true"))) {
      getApplicationToolBar().addPadding(5);
      getApplicationToolBar().add(buildWelcomeLabel());
    }
    getApplicationToolBar().addPadding(5);
    getApplicationToolBar().add(buildProfileButton(isAuthenticated()));
    if ("true".equals(getSettings().getString("showGroupsOnToolbar", "true"))) {
      getApplicationToolBar().add(buildManageGroupsButton(isAuthenticated()));
    }

    customizeApplicationToolBarLeft(getApplicationToolBar());
    getApplicationToolBar().addFiller(100);
    customizeApplicationToolBarRight(getApplicationToolBar());

    if (isAuthenticated()) {
      if (getAuthenticatedUser().isAdministrator()) {
        getApplicationToolBar().add(buildAdminButton());
      }
      getApplicationToolBar().add(buildLogoutButton());
    } else {
      getApplicationToolBar().add(buildLoginButton());
    }
    getApplicationToolBar().addPadding(5);
    return getApplicationToolBar();
  }

  // override
  public void customizeApplicationToolBarLeft(final ToolBar toolbar) {
  }

  // override
  public void customizeApplicationToolBarRight(final ToolBar toolbar) {
  }

  // override
  public void addNorthWidgets(Panel applicationPanel) {
  }

  // override
  public void addSouthWidgets(Panel applicationPanel) {
  }

  public DeckPanel getApplicationContentDeck() {
    return applicationContentDeck;
  }

  public ToolBar getApplicationToolBar() {
    return applicationToolBar;
  }

  public boolean isAuthenticated() {
    return authenticatedUser != null;
  }

  public void setAuthenticatedUser(User user) {
    this.authenticatedUser = user;
    if ("true".equals(getSettings().getString("showApplicationToolbar"))) {
      buildApplicationToolBar();
    }
  }

  public User getAuthenticatedUser() {
    return authenticatedUser;
  }

  public void loggedOut() {
    authenticatedUser = null;
    adminPanel = null;
    buildApplicationToolBar();
  }

  // override this
  public void loadApplication() {
  }

}

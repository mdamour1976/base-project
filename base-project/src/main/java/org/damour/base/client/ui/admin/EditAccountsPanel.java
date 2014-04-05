package org.damour.base.client.ui.admin;

import java.util.HashMap;
import java.util.List;

import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.client.service.ResourceCache;
import org.damour.base.client.ui.IGenericCallback;
import org.damour.base.client.ui.buttons.Button;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class EditAccountsPanel extends FlexTable implements IAdminPanel, ChangeHandler, IGenericCallback<User> {

  EditAccountPanel editUserPanel;
  ListBox usersList = new ListBox();
  String lastListSelection;
  HashMap<String, User> userMap = new HashMap<String, User>();
  List<User> users;
  List<UserGroup> groups;
  IAdminCallback callback;

  public EditAccountsPanel(IAdminCallback callback, List<UserGroup> groups, List<User> users, User user) {
    this.callback = callback;
    this.users = users;
    this.groups = groups;
    buildUI();
    if (users == null) {
      fetchUsers();
    } else {
      populateUI();
    }
  }

  private void buildUI() {
    usersList.setVisibleItemCount(15);
    usersList.addChangeHandler(this);
    usersList.addItem("Loading...");
    setHeight("100%");
    setWidth("100%");

    VerticalPanel buttonPanel = new VerticalPanel();
    buttonPanel.add(new Button("New..."));
    buttonPanel.add(new Button("Delete"));

    Button refreshButton = new Button("Refresh");
    refreshButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        usersList.clear();
        usersList.addItem("Loading...");
        setWidget(0, 1, new Label());
        fetchUsers();
      }
    });

    FlexTable usersListPanel = new FlexTable();
    usersListPanel.setWidget(0, 0, usersList);
    usersListPanel.setWidget(0, 1, buttonPanel);
    usersListPanel.setWidget(1, 0, refreshButton);

    CaptionPanel captionPanel = new CaptionPanel("Users");
    captionPanel.setContentWidget(usersListPanel);

    setWidget(0, 0, captionPanel);
    setWidget(0, 1, new Label());
    setWidget(0, 2, new Label());
    getFlexCellFormatter().setRowSpan(0, 1, 2);
    getFlexCellFormatter().setRowSpan(0, 2, 2);
    getCellFormatter().setWidth(0, 2, "100%");
    getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT);
    getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
  }

  private void populateUI() {
    setWidget(0, 1, new Label());
    usersList.clear();
    userMap.clear();
    for (int i = 0; i < users.size(); i++) {
      User user = users.get(i);
      usersList.addItem(user.getUsername());
      if (lastListSelection != null && lastListSelection.equals(user.getUsername())) {
        usersList.setSelectedIndex(i);
      }
      userMap.put(user.getUsername(), user);
    }
    onChange(new com.google.gwt.event.dom.client.ChangeEvent() {
      public Object getSource() {
        return usersList;
      }
    });
  }

  private void fetchUsers() {
    MethodCallback<List<User>> getUsersCallback = new MethodCallback<List<User>>() {
      public void onSuccess(Method method, List<User> users) {
        EditAccountsPanel.this.users = users;
        populateUI();
        if (callback != null) {
          // the user list is updated
          callback.usersFetched(users);
        }
      }

      public void onFailure(Method method, Throwable exception) {
      }
    };
    ResourceCache.getUserResource().getUsers(getUsersCallback);
  }

  public void onChange(ChangeEvent event) {
    if (usersList.getSelectedIndex() >= 0) {
      setWidget(0, 1, new Label("Loading..."));
      User user = userMap.get(usersList.getItemText(usersList.getSelectedIndex()));
      lastListSelection = user.getUsername();
      editUserPanel = new EditAccountPanel(this, user);
      CaptionPanel captionPanel = new CaptionPanel("Edit Account");
      captionPanel.setContentWidget(editUserPanel);
      setWidget(0, 1, captionPanel);
    }
  }

  public void invoke(User user) {
    userMap.put(user.getUsername(), user);
    usersList.setItemText(usersList.getSelectedIndex(), user.getUsername());

    // replace
    for (int i = 0; i < users.size(); i++) {
      User tmpUser = users.get(i);
      if (tmpUser.getId().equals(user.getId())) {
        users.set(i, user);
      }
    }

    if (callback != null) {
      // the user is updated
      callback.updateUser(user);
      // the user list is also updated
      callback.usersFetched(users);
    }
  }

  public List<User> getUsers() {
    return users;
  }

  public void setUsers(List<User> users) {
    this.users = users;
    populateUI();
  }

  public List<UserGroup> getUserGroups() {
    return groups;
  }

  public void setUserGroups(List<UserGroup> groups) {
    this.groups = groups;
    populateUI();
  }
}

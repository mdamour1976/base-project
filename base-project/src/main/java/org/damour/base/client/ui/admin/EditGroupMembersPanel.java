package org.damour.base.client.ui.admin;

import java.util.HashMap;
import java.util.List;

import org.damour.base.client.objects.GroupMembership;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.client.service.ResourceCache;
import org.damour.base.client.ui.IGenericCallback;
import org.damour.base.client.ui.buttons.Button;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class EditGroupMembersPanel extends FlexTable implements ChangeHandler {

  final ListBox allUsersListBox = new ListBox();
  final ListBox membersListBox = new ListBox();
  Button removeButton = new Button();
  Button addButton = new Button();

  IAdminCallback adminCallback;
  IGenericCallback<UserGroup> callback;
  List<User> allUsers;
  List<User> members;
  UserGroup group;
  HashMap<String, User> userMap = new HashMap<String, User>();

  public EditGroupMembersPanel(IAdminCallback adminCallback, final IGenericCallback<UserGroup> callback, final List<User> users, final UserGroup group) {

    this.adminCallback = adminCallback;
    this.callback = callback;
    this.allUsers = users;
    this.group = group;

    allUsersListBox.addItem("Loading...");
    membersListBox.addItem("Loading...");
    allUsersListBox.setVisibleItemCount(15);
    membersListBox.setVisibleItemCount(15);
    allUsersListBox.addChangeHandler(this);
    membersListBox.addChangeHandler(this);

    removeButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        final int index = membersListBox.getSelectedIndex();
        final String username = membersListBox.getItemText(index);
        final User user = userMap.get(username);
        final MethodCallback<Void> deleteUserCallback = new MethodCallback<Void>() {
          public void onFailure(Method method, Throwable exception) {
            MessageDialogBox dialog = new MessageDialogBox("Error", exception.getMessage(), true, true, true);
            dialog.center();
          }

          public void onSuccess(Method method, Void response) {
            members.remove(user);
            populateUI();
            try {
              if (index < membersListBox.getItemCount()) {
                membersListBox.setSelectedIndex(index);
              } else {
                membersListBox.setSelectedIndex(index - 1);
              }
            } catch (Exception e) {
            }
            onChange(new com.google.gwt.event.dom.client.ChangeEvent() {
              public Object getSource() {
                return membersListBox;
              }
            });
          };
        };
        ResourceCache.getGroupResource().deleteUser(user.getId(), group.getId(), deleteUserCallback);
      }
    });
    removeButton.setText(" < ");
    removeButton.setTitle("Remove Member");

    addButton.setText(" > ");
    addButton.setTitle("Add Member");
    addButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        final int index = allUsersListBox.getSelectedIndex();
        final String username = allUsersListBox.getItemText(index);
        final User user = userMap.get(username);
        final MethodCallback<GroupMembership> addUserCallback = new MethodCallback<GroupMembership>() {

          public void onFailure(Method method, Throwable exception) {
            MessageDialogBox dialog = new MessageDialogBox("Error", exception.getMessage(), true, true, true);
            dialog.center();
          }

          public void onSuccess(Method method, GroupMembership response) {
            members.add(user);
            populateUI();
            try {
              if (index < allUsersListBox.getItemCount()) {
                allUsersListBox.setSelectedIndex(index);
              } else {
                allUsersListBox.setSelectedIndex(index - 1);
              }
            } catch (Exception e) {
            }
            onChange(new com.google.gwt.event.dom.client.ChangeEvent() {
              public Object getSource() {
                return allUsersListBox;
              }
            });
          };
        };
        ResourceCache.getGroupResource().addUserToGroup(user.getId(), group.getId(), addUserCallback);
      }
    });

    VerticalPanel buttonPanel = new VerticalPanel();
    buttonPanel.add(addButton);
    buttonPanel.add(removeButton);

    // build ui
    int row = 0;
    setWidget(++row, 0, new Label("All Users", false));
    setWidget(row, 1, new Label());
    setWidget(row, 2, new Label("Group Members", false));

    setWidget(++row, 0, allUsersListBox);
    setWidget(row, 1, buttonPanel);
    setWidget(row, 2, membersListBox);

    fetchGroupMembers();
  }

  private void populateUI() {
    allUsersListBox.clear();
    membersListBox.clear();
    if (allUsers != null) {
      for (User user : allUsers) {
        userMap.put(user.getUsername(), user);
        allUsersListBox.addItem(user.getUsername());
      }
    }
    if (members != null) {
      for (User user : members) {
        for (int i = 0; i < allUsersListBox.getItemCount(); i++) {
          if (allUsersListBox.getItemText(i).equals(user.getUsername())) {
            allUsersListBox.removeItem(i);
          }
        }
        userMap.put(user.getUsername(), user);
        membersListBox.addItem(user.getUsername());
      }
    }
    // fire event to init button state
    onChange(null);
  }

  public void fetchGroupMembers() {
    final MethodCallback<List<User>> getGroupMemsCallback = new MethodCallback<List<User>>() {
      public void onFailure(Method method, Throwable caught) {
        MessageDialogBox dialog = new MessageDialogBox("Error", caught.getMessage(), true, true, true);
        dialog.center();
      }

      public void onSuccess(Method method, List<User> members) {
        EditGroupMembersPanel.this.members = members;
        if (members == null) {
          MessageDialogBox dialog = new MessageDialogBox("Error", "Could not get users in group.", true, true, true);
          dialog.center();
        } else {
          if (allUsers == null) {
            fetchUsers();
          } else {
            populateUI();
          }
        }
      };
    };
    ResourceCache.getGroupResource().getUsers(group.getId(), getGroupMemsCallback);
  }

  private void fetchUsers() {
    MethodCallback<List<User>> getUsersCallback = new MethodCallback<List<User>>() {
      public void onSuccess(Method method, List<User> users) {
        EditGroupMembersPanel.this.allUsers = users;
        populateUI();
        if (adminCallback != null) {
          adminCallback.usersFetched(users);
        }
      }

      public void onFailure(Method method, Throwable exception) {
      }
    };
    ResourceCache.getUserResource().getUsers(getUsersCallback);
  }

  public void onChange(ChangeEvent event) {
    if (allUsersListBox.getSelectedIndex() != -1) {
      addButton.setEnabled(true);
    } else {
      addButton.setEnabled(false);
    }
    if (membersListBox.getSelectedIndex() != -1) {
      removeButton.setEnabled(true);
    } else {
      removeButton.setEnabled(false);
    }
  }

}

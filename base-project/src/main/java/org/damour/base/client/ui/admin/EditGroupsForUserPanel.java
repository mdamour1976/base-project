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

public class EditGroupsForUserPanel extends FlexTable implements ChangeHandler {

  final ListBox allGroupsListBox = new ListBox();
  final ListBox groupsForUserListBox = new ListBox();
  Button removeButton = new Button();
  Button addButton = new Button();

  IAdminCallback adminCallback;
  IGenericCallback<User> callback;
  List<UserGroup> allGroups;
  List<UserGroup> groupsForUser;
  User user;
  HashMap<String, UserGroup> groupMap = new HashMap<String, UserGroup>();

  boolean ignoreCallbackEvents = false;

  public EditGroupsForUserPanel(IAdminCallback adminCallback, IGenericCallback<User> callback, final List<UserGroup> allGroups, final User user) {
    this.adminCallback = adminCallback;
    this.callback = callback;
    this.allGroups = allGroups;
    this.user = user;
    initUI();
    fetchGroupsForUser();
  }

  private void initUI() {
    // build ui
    VerticalPanel buttonPanel = new VerticalPanel();
    removeButton.setText(" < ");
    removeButton.setTitle("Remove Group Membership");
    addButton.setText(" > ");
    addButton.setTitle("Add Group Membership");
    addButton.addClickHandler(new ClickHandler() {

      public void onClick(ClickEvent event) {
        addGroupMembership();
      }
    });
    removeButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        removeGroupMembership();
      }
    });
    buttonPanel.add(addButton);
    buttonPanel.add(removeButton);

    allGroupsListBox.addChangeHandler(this);
    groupsForUserListBox.addChangeHandler(this);
    allGroupsListBox.setVisibleItemCount(10);
    groupsForUserListBox.setVisibleItemCount(10);

    int row = 0;
    setWidget(++row, 0, new Label("All Groups", false));
    setWidget(row, 1, new Label());
    setWidget(row, 2, new Label("Memberships", false));

    setWidget(++row, 0, allGroupsListBox);
    setWidget(row, 1, buttonPanel);
    setWidget(row, 2, groupsForUserListBox);

    // update button state
    onChange(null);
  }

  private void populateUI() {
    groupsForUserListBox.clear();
    allGroupsListBox.clear();
    groupMap.clear();
    for (UserGroup userGroup : allGroups) {
      allGroupsListBox.addItem(userGroup.getName());
      groupMap.put(userGroup.getName(), userGroup);
    }
    for (UserGroup userGroup : groupsForUser) {
      for (int i = 0; i < allGroupsListBox.getItemCount(); i++) {
        if (allGroupsListBox.getItemText(i).equals(userGroup.getName())) {
          allGroupsListBox.removeItem(i);
        }
      }
      groupsForUserListBox.addItem(userGroup.getName());
      groupMap.put(userGroup.getName(), userGroup);
    }
    onChange(null);
  }

  private void addGroupMembership() {
    final int index = allGroupsListBox.getSelectedIndex();
    final String groupName = allGroupsListBox.getItemText(index);
    final UserGroup group = groupMap.get(groupName);
    final MethodCallback<GroupMembership> addUserCallback = new MethodCallback<GroupMembership>() {
      public void onFailure(Method method, Throwable exception) {
        MessageDialogBox dialog = new MessageDialogBox("Error", exception.getMessage(), true, true, true);
        dialog.center();
      }

      public void onSuccess(Method method, GroupMembership membership) {
        groupsForUser.add(membership.getUserGroup());
        populateUI();
        try {
          if (index < allGroupsListBox.getItemCount()) {
            allGroupsListBox.setSelectedIndex(index);
          } else {
            allGroupsListBox.setSelectedIndex(index - 1);
          }
        } catch (Exception e) {
        }
        onChange(null);
      };
    };
    ResourceCache.getGroupResource().addUserToGroup(user.getId(), group.getId(), addUserCallback);
  }

  private void removeGroupMembership() {
    final int index = groupsForUserListBox.getSelectedIndex();
    if (index == -1) {
      return;
    }
    final String groupName = groupsForUserListBox.getItemText(index);
    final UserGroup group = groupMap.get(groupName);
    final MethodCallback<Void> deleteUserCallback = new MethodCallback<Void>() {

      public void onFailure(Method method, Throwable exception) {
        MessageDialogBox dialog = new MessageDialogBox("Error", exception.getMessage(), true, true, true);
        dialog.center();
      }

      public void onSuccess(Method method, Void response) {
        groupsForUser.remove(group);
        populateUI();
        try {
          if (index < groupsForUserListBox.getItemCount()) {
            groupsForUserListBox.setSelectedIndex(index);
          } else {
            groupsForUserListBox.setSelectedIndex(index - 1);
          }
        } catch (Exception e) {
        }
        onChange(null);
      };
    };
    ResourceCache.getGroupResource().deleteUser(user.getId(), group.getId(), deleteUserCallback);
  }

  private void fetchGroupsForUser() {
    final MethodCallback<List<UserGroup>> getGroupsForUserCallback = new MethodCallback<List<UserGroup>>() {
      public void onFailure(Method method, Throwable exception) {
      }

      public void onSuccess(Method method, List<UserGroup> groupsForUser) {
        EditGroupsForUserPanel.this.groupsForUser = groupsForUser;
        if (allGroups == null) {
          final MethodCallback<List<UserGroup>> getGroupsCallback = new MethodCallback<List<UserGroup>>() {
            public void onFailure(Method method, Throwable caught) {
            }

            public void onSuccess(Method method, List<UserGroup> groups) {
              allGroups = groups;
              populateUI();
              if (callback != null) {
                ignoreCallbackEvents = true;
                adminCallback.userGroupsFetched(groups);
                ignoreCallbackEvents = false;
              }
            };
          };
          ResourceCache.getGroupResource().getGroups(getGroupsCallback);
        } else {
          populateUI();
        }
      };
    };
    ResourceCache.getGroupResource().getGroups(user.getUsername(), getGroupsForUserCallback);
  }

  public void onChange(ChangeEvent event) {
    if (allGroupsListBox.getSelectedIndex() != -1) {
      addButton.setEnabled(true);
    } else {
      addButton.setEnabled(false);
    }
    if (groupsForUserListBox.getSelectedIndex() != -1) {
      removeButton.setEnabled(true);
    } else {
      removeButton.setEnabled(false);
    }
  }
}

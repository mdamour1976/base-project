package org.damour.base.client.ui.admin;

import java.util.HashMap;
import java.util.List;

import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.client.service.ResourceCache;
import org.damour.base.client.ui.IGenericCallback;
import org.damour.base.client.ui.buttons.Button;
import org.damour.base.client.ui.dialogs.IDialogCallback;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.ui.dialogs.PromptDialogBox;
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

public class EditGroupsPanel extends FlexTable implements IAdminPanel, ChangeHandler, IGenericCallback<UserGroup> {
  EditGroupPanel editGroupPanel;
  EditGroupMembersPanel editGroupMembersPanel;
  ListBox groupsList = new ListBox();
  Button deleteGroupButton = new Button("Delete");
  Button editGroupButton = new Button("Edit");

  String lastListSelection;
  HashMap<String, UserGroup> groupMap = new HashMap<String, UserGroup>();
  IAdminCallback callback;
  User user;
  List<User> users;
  List<UserGroup> groups;
  boolean editMembers = false;
  boolean showAddEditRemove = false;

  public EditGroupsPanel(IAdminCallback callback, List<UserGroup> groups, List<User> users, User user, boolean showAddEditRemove, boolean editMembers) {
    this.callback = callback;
    this.user = user;
    this.users = users;
    this.groups = groups;
    this.showAddEditRemove = showAddEditRemove;
    this.editMembers = editMembers;
    if (user == null) {
      return;
    }

    if (callback == null) {
      // if there is no callback, let's make one so that anything
      // this dialog spawns will at least keep us up to date with anything
      // it has fetched/refreshed/updated
      this.callback = new IAdminCallback() {
        public void updateUser(User user) {
        }

        public void updateUserGroup(UserGroup group) {
        }

        public void userGroupsFetched(List<UserGroup> groups) {
        }

        public void usersFetched(List<User> users) {
          EditGroupsPanel.this.users = users;
        }
      };
    }

    buildUI();
    if (groups == null) {
      fetchGroups();
    } else {
      populateUI();
    }
  }

  private void buildUI() {
    groupsList.setVisibleItemCount(15);
    groupsList.addChangeHandler(this);
    groupsList.addItem("Loading...");
    setHeight("100%");
    setWidth("100%");

    VerticalPanel buttonPanel = new VerticalPanel();

    Button newGroupButton = new Button("New...");
    newGroupButton.setTitle("Create a New Group");
    newGroupButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        final UserGroup group = new UserGroup();
        group.setOwner(user);
        editGroupPanel = new EditGroupPanel(callback, EditGroupsPanel.this, users, group, false, true);
        final PromptDialogBox editGroupDialogBox = new PromptDialogBox("Create New Group", "OK", null, "Cancel", false, true);
        editGroupDialogBox.setContent(editGroupPanel);
        editGroupDialogBox.setCallback(new IDialogCallback() {
          public void okPressed() {
            if (!editGroupPanel.apply()) {
              editGroupDialogBox.center();
            } else {
              populateUI();
              if (callback != null) {
                callback.userGroupsFetched(groups);
              }
            }
          }

          public void cancelPressed() {
          }
        });
        editGroupDialogBox.center();
      }
    });

    deleteGroupButton.setTitle("Delete Group");
    deleteGroupButton.setEnabled(false);
    deleteGroupButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {

        if (groupsList.getSelectedIndex() < 0) {
          return;
        }

        final UserGroup group = groupMap.get(groupsList.getItemText(groupsList.getSelectedIndex()));

        final PromptDialogBox deleteGroupDialogBox = new PromptDialogBox("Confirm", "Yes", null, "No", false, true);
        deleteGroupDialogBox.setContent(new Label("Delete Group: " + group.getName() + "?"));
        deleteGroupDialogBox.setCallback(new IDialogCallback() {
          public void okPressed() {
            final MethodCallback<Void> deleteGroupCallback = new MethodCallback<Void>() {

              public void onFailure(Method method, Throwable exception) {
                MessageDialogBox dialog = new MessageDialogBox("Error", exception.getMessage(), true, true, true);
                dialog.center();
              }

              public void onSuccess(Method method, Void response) {
                groups.remove(group);
                lastListSelection = null;
                populateUI();
                if (callback != null) {
                  callback.userGroupsFetched(groups);
                }
              };
            };
            ResourceCache.getGroupResource().deleteGroup(group.getId(), deleteGroupCallback);

          }

          public void cancelPressed() {
          }
        });
        deleteGroupDialogBox.center();
      }
    });
    editGroupButton.setTitle("Edit Group Settings");
    editGroupButton.setEnabled(false);
    editGroupButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        if (groupsList.getSelectedIndex() < 0) {
          return;
        }
        final UserGroup group = groupMap.get(groupsList.getItemText(groupsList.getSelectedIndex()));
        editGroupPanel = new EditGroupPanel(callback, EditGroupsPanel.this, users, group, false, true);
        final PromptDialogBox editGroupDialogBox = new PromptDialogBox("Edit Group", "OK", null, "Cancel", false, true);
        editGroupDialogBox.setContent(editGroupPanel);
        editGroupDialogBox.setCallback(new IDialogCallback() {
          public void okPressed() {
            if (!editGroupPanel.apply()) {
              editGroupDialogBox.center();
            } else {
              if (callback != null) {
                callback.userGroupsFetched(groups);
              }
            }
          }

          public void cancelPressed() {
          }
        });
        editGroupDialogBox.center();
      }
    });
    buttonPanel.add(newGroupButton);
    buttonPanel.add(editGroupButton);
    buttonPanel.add(deleteGroupButton);

    Button refreshButton = new Button("Refresh");
    refreshButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        groupsList.clear();
        groupsList.addItem("Loading...");
        setWidget(0, 1, new Label());
        fetchGroups();
      }
    });

    FlexTable groupsListPanel = new FlexTable();
    groupsListPanel.setWidget(0, 0, groupsList);
    if (showAddEditRemove) {
      groupsListPanel.setWidget(0, 1, buttonPanel);
    }
    groupsListPanel.setWidget(1, 0, refreshButton);
    CaptionPanel captionPanel = new CaptionPanel("Groups");
    captionPanel.setContentWidget(groupsListPanel);

    setWidget(0, 0, captionPanel);
    getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);
    getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
  }

  private void populateUI() {
    setWidget(0, 1, new Label());
    groupsList.clear();
    groupMap.clear();
    for (int i = 0; i < groups.size(); i++) {
      UserGroup group = groups.get(i);
      groupsList.addItem(group.getName());
      if (lastListSelection != null && lastListSelection.equals(group.getName())) {
        groupsList.setSelectedIndex(i);
      }
      groupMap.put(group.getName(), group);
    }
    onChange(new com.google.gwt.event.dom.client.ChangeEvent() {
      public Object getSource() {
        return groupsList;
      }
    });
  }

  private void fetchGroups() {
    final MethodCallback<List<UserGroup>> getGroupsCallback = new MethodCallback<List<UserGroup>>() {
      public void onFailure(Method method, Throwable caught) {
      }

      public void onSuccess(Method method, List<UserGroup> groups) {
        EditGroupsPanel.this.groups = groups;
        populateUI();
        if (callback != null) {
          callback.userGroupsFetched(groups);
        }
      };
    };
    ResourceCache.getGroupResource().getOwnedGroups(user.getUsername(), getGroupsCallback);
  }

  public void onChange(ChangeEvent event) {
    if (groupsList.getSelectedIndex() >= 0) {
      deleteGroupButton.setEnabled(true);
      editGroupButton.setEnabled(true);
      setWidget(0, 1, new Label("Loading..."));
      UserGroup group = groupMap.get(groupsList.getItemText(groupsList.getSelectedIndex()));
      lastListSelection = group.getName();

      if (editMembers) {
        CaptionPanel groupMembersCaptionPanel = new CaptionPanel("Edit Group Members");
        editGroupMembersPanel = new EditGroupMembersPanel(callback, this, users, group);
        groupMembersCaptionPanel.setContentWidget(editGroupMembersPanel);
        setWidget(0, 1, groupMembersCaptionPanel);
      } else {
        CaptionPanel groupCaptionPanel = new CaptionPanel("Edit Group");
        editGroupPanel = new EditGroupPanel(callback, this, users, group, true, true);
        groupCaptionPanel.setContentWidget(editGroupPanel);
        setWidget(0, 1, groupCaptionPanel);
      }
    } else {
      deleteGroupButton.setEnabled(false);
      editGroupButton.setEnabled(false);
    }
  }

  public void invoke(UserGroup group) {
    groupMap.put(group.getName(), group);
    lastListSelection = group.getName();

    if (groupsList.getSelectedIndex() != -1) {
      UserGroup tmpGroup = groupMap.get(groupsList.getItemText(groupsList.getSelectedIndex()));
      if (tmpGroup.getId().equals(group.getId())) {
        groupsList.setItemText(groupsList.getSelectedIndex(), group.getName());
      } else {
        groups.add(group);
        groupsList.addItem(group.getName());
        groupsList.setSelectedIndex(groupsList.getItemCount() - 1);
        onChange(new com.google.gwt.event.dom.client.ChangeEvent() {
          public Object getSource() {
            return groupsList;
          }
        });
      }
    } else {
      groups.add(group);
      groupsList.addItem(group.getName());
      groupsList.setSelectedIndex(groupsList.getItemCount() - 1);
      onChange(new com.google.gwt.event.dom.client.ChangeEvent() {
        public Object getSource() {
          return groupsList;
        }
      });
    }

    // replace
    for (int i = 0; i < groups.size(); i++) {
      UserGroup tmpGroup = groups.get(i);
      if (tmpGroup.getId().equals(group.getId())) {
        groups.set(i, group);
      }
    }

    if (callback != null) {
      // the group is updated
      callback.updateUserGroup(group);
      // the group list is also updated
      callback.userGroupsFetched(groups);
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

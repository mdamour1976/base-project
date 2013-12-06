package org.damour.base.client.ui.admin;

import java.util.List;

import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.client.service.BaseServiceCache;
import org.damour.base.client.ui.tabs.BaseTabPanel;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AdministratorPanel extends VerticalPanel implements IAdminCallback {

  EditAccountsPanel editUsersPanel;
  EditGroupsPanel editGroupsPanel;
  EditGroupsPanel editGroupsMembersPanel;
  EditGroupsForUsersPanel editGroupsForUsersPanel;
  HibernateAdminPanel hibernateAdminPanel;
  HQLPanel hqlPanel;
  ReferralPanel referralPanel;
  MemoryAdminPanel memoryAdminPanel;

  List<UserGroup> groups;
  List<User> users;
  User user;

  boolean activated = false;

  public AdministratorPanel(final User user) {
    this.user = user;
    if (user == null || !user.isAdministrator()) {
      return;
    }
    setHeight("100%");
    setWidth("100%");
  }

  public void loadObjects(final User user) {
    final AsyncCallback<List<UserGroup>> groupsCallback = new AsyncCallback<List<UserGroup>>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(final List<UserGroup> newgroups) {
        AdministratorPanel.this.groups = newgroups;
        final BaseTabPanel adminTabPanel = new BaseTabPanel();
        adminTabPanel.setWidth("100%");
        adminTabPanel.setHeight("100%");
        add(adminTabPanel);
        editUsersPanel = new EditAccountsPanel(AdministratorPanel.this, groups, users, user);
        adminTabPanel.addTab("Accounts", "Accounts", false, editUsersPanel);
        editGroupsPanel = new EditGroupsPanel(AdministratorPanel.this, groups, users, user, true, false);
        adminTabPanel.addTab("Groups", "Groups", false, editGroupsPanel);
        editGroupsMembersPanel = new EditGroupsPanel(AdministratorPanel.this, groups, users, user, false, true);
        adminTabPanel.addTab("Groups -> Users", "Groups -> Users", false, editGroupsMembersPanel);
        editGroupsForUsersPanel = new EditGroupsForUsersPanel(AdministratorPanel.this, groups, users, user);
        adminTabPanel.addTab("Users -> Groups", "Users -> Groups", false, editGroupsForUsersPanel);
        referralPanel = new ReferralPanel();
        adminTabPanel.addTab("Referrals", "Referrals", false, new ScrollPanel(referralPanel));
        hibernateAdminPanel = new HibernateAdminPanel();
        adminTabPanel.addTab("Hibernate", "Hibernate", false, hibernateAdminPanel);
        hqlPanel = new HQLPanel();
        adminTabPanel.addTab("HQL", "HQL", false, hqlPanel);
        memoryAdminPanel = new MemoryAdminPanel();
        adminTabPanel.addTab("Memory", "Memory", false, memoryAdminPanel);
        adminTabPanel.selectTab(0);
      };
    };

    final AsyncCallback<List<User>> callback = new AsyncCallback<List<User>>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(List<User> users) {
        AdministratorPanel.this.users = users;
        BaseServiceCache.getService().getGroups(groupsCallback);
      };
    };
    BaseServiceCache.getService().getUsers(callback);

  }

  public void updateUser(User updatedUser) {
    if (user.getId().equals(updatedUser.getId())) {
      user.setUsername(updatedUser.getUsername());
      user.setPasswordHash(updatedUser.getPasswordHash());
      user.setPasswordHint(updatedUser.getPasswordHint());
      user.setFirstname(updatedUser.getFirstname());
      user.setLastname(updatedUser.getLastname());
      user.setEmail(updatedUser.getEmail());
      user.setBirthday(updatedUser.getBirthday());
      user.setAdministrator(updatedUser.isAdministrator());
      user.setValidated(updatedUser.isValidated());
    }
  }

  public void updateUserGroup(UserGroup group) {
  }

  public void userGroupsFetched(List<UserGroup> groups) {
    this.groups = groups;
  }

  public void usersFetched(List<User> users) {
    this.users = users;
  }

  public void activate() {
    if (!activated) {
      loadObjects(user);
      activated = true;
    }
  }

}

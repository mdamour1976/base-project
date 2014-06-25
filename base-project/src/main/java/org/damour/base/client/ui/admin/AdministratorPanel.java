package org.damour.base.client.ui.admin;

import java.util.List;

import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.client.service.ResourceCache;
import org.damour.base.client.ui.tabs.BaseTabPanel;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

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
  SystemAdminPanel systemAdminPanel;
  LogPanel logPanel;

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
    final MethodCallback<List<UserGroup>> groupsCallback = new MethodCallback<List<UserGroup>>() {
      public void onFailure(Method method, Throwable exception) {
      }

      public void onSuccess(Method method, List<UserGroup> newgroups) {
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
        systemAdminPanel = new SystemAdminPanel();
        adminTabPanel.addTab("System", "System", false, systemAdminPanel);
        logPanel = new LogPanel();
        adminTabPanel.addTab("Logs", "Logs", false, logPanel);
        adminTabPanel.selectTab(0);
      }
    };

    ResourceCache.getUserResource().getUsers(new MethodCallback<List<User>>() {
      public void onSuccess(Method method, List<User> users) {
        AdministratorPanel.this.users = users;
        ResourceCache.getGroupResource().getGroups(groupsCallback);
      }

      public void onFailure(Method method, Throwable exception) {
      }
    });
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

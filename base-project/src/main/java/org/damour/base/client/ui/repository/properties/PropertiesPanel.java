package org.damour.base.client.ui.repository.properties;

import java.util.ArrayList;
import java.util.List;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Permission;
import org.damour.base.client.service.ResourceCache;
import org.damour.base.client.ui.tabs.BaseTabPanel;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;

public class PropertiesPanel extends SimplePanel {

  public static enum VIEW {
    GENERAL, USERPERMS, GROUPPERMS
  };

  private PermissibleObject permissibleObject;
  private List<Permission> permissions;

  GeneralPanel generalPanel = null;
  PermissionsPanel userPermissionsPanel = null;
  PermissionsPanel groupPermissionsPanel = null;
  BaseTabPanel tabPanel = new BaseTabPanel();
  VIEW defaultView = VIEW.GENERAL;

  public PropertiesPanel(PermissibleObject permissibleObject, List<Permission> permissions, VIEW view) {
    this.permissibleObject = permissibleObject;
    this.permissions = permissions;
    this.defaultView = view;
    buildUI();
    if (permissions == null) {
      fetchPermissions();
    } else {
      populateUI();
    }
  }

  private void buildUI() {
    generalPanel = new GeneralPanel(permissibleObject);
    userPermissionsPanel = new PermissionsPanel(permissibleObject, new ArrayList<Permission>(), true, false);
    groupPermissionsPanel = new PermissionsPanel(permissibleObject, new ArrayList<Permission>(), false, true);

    tabPanel.setWidth("100%");
    tabPanel.setHeight("300px");
    setWidget(tabPanel);
  }

  private void populateUI() {
    tabPanel.closeAllTabs();
    tabPanel.addTab("General", "General", false, generalPanel);
    tabPanel.addTab("User Permissions", "User Permissions", false, userPermissionsPanel);
    tabPanel.addTab("Groups Permissions", "Groups Permissions", false, groupPermissionsPanel);
    if (defaultView.equals(VIEW.GENERAL)) {
      tabPanel.selectTab(0);
    } else if (defaultView.equals(VIEW.USERPERMS)) {
      tabPanel.selectTab(1);
    } else if (defaultView.equals(VIEW.GROUPPERMS)) {
      tabPanel.selectTab(2);
    }
    userPermissionsPanel.setPermissions(permissions);
    userPermissionsPanel.populateUI();
    groupPermissionsPanel.setPermissions(permissions);
    groupPermissionsPanel.populateUI();
  }

  private void handleFetchFailure() {
    tabPanel.closeAllTabs();
    tabPanel.addTab("General", "General", false, generalPanel);
    tabPanel.selectTab(0);
  }

  private void fetchPermissions() {
    MethodCallback<List<Permission>> callback = new MethodCallback<List<Permission>>() {

      public void onFailure(Method method, Throwable exception) {
        handleFetchFailure();
      }

      public void onSuccess(Method method, List<Permission> inPermissions) {
        permissions = inPermissions;
        populateUI();
      }
    };
    ResourceCache.getPermissibleResource().getPermissions(permissibleObject.getId(), callback);
  }

  public void apply(AsyncCallback<Void> callback) {
    generalPanel.apply(callback);
    userPermissionsPanel.apply(callback);
    groupPermissionsPanel.apply(callback);
  }

}

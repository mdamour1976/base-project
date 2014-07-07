package org.damour.base.client.ui.admin;

import java.util.Date;

import org.damour.base.client.objects.MemoryStats;
import org.damour.base.client.service.ResourceCache;
import org.damour.base.client.ui.buttons.Button;
import org.damour.base.client.utils.StringUtils;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SystemAdminPanel extends VerticalPanel {

  FlexTable statsTable = new FlexTable();
  NumberFormat nf = NumberFormat.getDecimalFormat();
  NumberFormat pf = NumberFormat.getPercentFormat();

  public SystemAdminPanel() {
    initUI();
    fetchMemoryStats();
  }

  private void initUI() {
    statsTable.setText(0, 0, "Loading...");
    statsTable.setBorderWidth(1);
    statsTable.setCellSpacing(5);
    add(statsTable);
    Button refreshButton = new Button("Refresh");
    refreshButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        fetchMemoryStats();
      }
    });
    Button gcButton = new Button("Request GC");
    gcButton.setTitle("Request Garbage Collection");
    gcButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        requestGarbageCollection();
      }
    });

    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.add(refreshButton);
    buttonPanel.add(gcButton);
    add(buttonPanel);
  }

  public void populateUI(MemoryStats stats) {
    statsTable.clear();
    int row = 0;

    statsTable.setWidget(row, 0, new Label("Startup Date"));
    statsTable.setWidget(row, 1, new Label("Uptime"));
    statsTable.setWidget(row, 2, new Label("Max Memory"));
    statsTable.setWidget(row, 3, new Label("Allocated Memory"));
    statsTable.setWidget(row, 4, new Label("Used Memory"));
    statsTable.setWidget(row, 5, new Label("Free Memory"));
    statsTable.setWidget(row, 6, new Label("% Free Memory"));
    statsTable.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 2, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 3, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 4, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 5, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 6, HasHorizontalAlignment.ALIGN_RIGHT);

    row++;
    Date startupDate = new Date(stats.getStartupDate());
    statsTable.setWidget(row, 0, new Label(startupDate.toString()));
    statsTable.setWidget(row, 1, new Label(StringUtils.getPrettyDuration(stats.getUptime())));
    statsTable.setWidget(row, 2, new Label(nf.format(stats.getMaxMemory())));
    statsTable.setWidget(row, 3, new Label(nf.format(stats.getTotalMemory())));
    statsTable.setWidget(row, 4, new Label(nf.format(stats.getTotalMemory() - stats.getFreeMemory())));
    statsTable.setWidget(row, 5, new Label(nf.format(stats.getFreeMemory())));
    statsTable.setWidget(row, 6, new Label(pf.format((float) stats.getFreeMemory() / (float) stats.getTotalMemory())));
    statsTable.getCellFormatter().setHorizontalAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 1, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 2, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 3, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 4, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 5, HasHorizontalAlignment.ALIGN_RIGHT);
    statsTable.getCellFormatter().setHorizontalAlignment(row, 6, HasHorizontalAlignment.ALIGN_RIGHT);
  }

  private void fetchMemoryStats() {
    for (int row = 1; row < statsTable.getRowCount(); row++) {
      statsTable.setWidget(row, 0, new Label("."));
      statsTable.setWidget(row, 1, new Label("."));
      statsTable.setWidget(row, 2, new Label("."));
      statsTable.setWidget(row, 3, new Label("."));
      statsTable.setWidget(row, 4, new Label("."));
      statsTable.setWidget(row, 5, new Label("."));
      statsTable.setWidget(row, 6, new Label("."));
    }
    final MethodCallback<MemoryStats> callback = new MethodCallback<MemoryStats>() {
      public void onFailure(Method method, Throwable caught) {
        Window.alert(caught.getMessage());
      }

      public void onSuccess(Method method, MemoryStats stats) {
        populateUI(stats);
      };
    };
    ResourceCache.getBaseResource().getMemoryStats(callback);
  }

  private void requestGarbageCollection() {
    for (int row = 1; row < statsTable.getRowCount(); row++) {
      statsTable.setWidget(row, 0, new Label("."));
      statsTable.setWidget(row, 1, new Label("."));
      statsTable.setWidget(row, 2, new Label("."));
      statsTable.setWidget(row, 3, new Label("."));
      statsTable.setWidget(row, 4, new Label("."));
      statsTable.setWidget(row, 5, new Label("."));
      statsTable.setWidget(row, 6, new Label("."));
    }    
    final MethodCallback<MemoryStats> callback = new MethodCallback<MemoryStats>() {
      public void onFailure(Method method, Throwable caught) {
        Window.alert(caught.getMessage());
      }

      public void onSuccess(Method method, MemoryStats stats) {
        populateUI(stats);
      };
    };
    ResourceCache.getBaseResource().requestGarbageCollection(callback);
  }

}

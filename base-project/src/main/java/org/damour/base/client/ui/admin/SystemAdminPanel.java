package org.damour.base.client.ui.admin;

import java.util.Date;
import java.util.List;

import org.damour.base.client.objects.SystemStats;
import org.damour.base.client.service.ResourceCache;
import org.damour.base.client.ui.buttons.Button;
import org.damour.base.client.ui.visualizations.LineChart;
import org.damour.base.client.utils.StringUtils;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SystemAdminPanel extends VerticalPanel {

  FlexTable statsTable = new FlexTable();
  NumberFormat nf = NumberFormat.getDecimalFormat();
  NumberFormat pf = NumberFormat.getPercentFormat();
  ListBox chartPicker = new ListBox(false);
  SimplePanel chartHolder = new SimplePanel();
  LineChart cpuFullChart;
  LineChart cpuFullAutoChart;
  LineChart cpuLast5Chart;
  LineChart cpuLast5AutoChart;
  LineChart cpuLast10Chart;
  LineChart cpuLast10AutoChart;
  LineChart cpuLast30Chart;
  LineChart cpuLast30AutoChart;
  LineChart memoryChart;
  LineChart memoryLast5Chart;
  LineChart memoryLast10Chart;
  LineChart memoryLast30Chart;

  String jsonData = null;

  public SystemAdminPanel() {
    initUI();
    fetchSystemStats();
  }

  private void initUI() {
    statsTable.setText(0, 0, "Loading...");
    statsTable.setBorderWidth(1);
    statsTable.setCellSpacing(5);
    add(statsTable);
    Button refreshButton = new Button("Refresh");
    refreshButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        fetchSystemStats();
      }
    });
    Button gcButton = new Button("Request GC");
    gcButton.setTitle("Request Garbage Collection");
    gcButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        requestGarbageCollection();
      }
    });

    chartPicker.setVisibleItemCount(1);
    chartPicker.addItem("Memory", "memory");
    chartPicker.addItem("Memory (Last 5 minutes)", "memory5");
    chartPicker.addItem("Memory (Last 10 minutes)", "memory10");
    chartPicker.addItem("Memory (Last 30 minutes)", "memory30");
    chartPicker.addItem("CPU (Full History)", "cpuf");
    chartPicker.addItem("CPU (Full History/Auto Scale)", "cpufa");
    chartPicker.addItem("CPU (Last 5 minutes)", "cpu5");
    chartPicker.addItem("CPU (Last 5 minutes/Auto Scale)", "cpu5a");
    chartPicker.addItem("CPU (Last 10 minutes)", "cpu10");
    chartPicker.addItem("CPU (Last 10 minutes/Auto Scale)", "cpu10a");
    chartPicker.addItem("CPU (Last 30 minutes)", "cpu30");
    chartPicker.addItem("CPU (Last 30 minutes/Auto Scale)", "cpu30a");
    chartPicker.addChangeHandler(new ChangeHandler() {
      public void onChange(ChangeEvent event) {
        selectChart();
      }
    });

    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.add(refreshButton);
    buttonPanel.add(gcButton);
    add(buttonPanel);

    add(chartPicker);
    add(chartHolder);
  }

  public void populateUI(SystemStats stats) {
    statsTable.clear();
    int row = 0;

    statsTable.setWidget(row, 0, new Label("Startup Date"));
    statsTable.setWidget(row, 1, new Label("Uptime"));
    statsTable.setWidget(row, 2, new Label("Max Memory"));
    statsTable.setWidget(row, 3, new Label("Allocated Memory"));
    statsTable.setWidget(row, 4, new Label("Used Memory"));
    statsTable.setWidget(row, 5, new Label("Free Memory"));
    statsTable.setWidget(row, 6, new Label("% Free Memory"));
    for (int i = 0; i <= 6; i++) {
      statsTable.getCellFormatter().setHorizontalAlignment(row, i, HasHorizontalAlignment.ALIGN_RIGHT);
    }

    row++;
    Date startupDate = new Date(stats.getStartupDate());
    statsTable.setWidget(row, 0, new Label(startupDate.toString()));
    statsTable.setWidget(row, 1, new Label(StringUtils.getPrettyDuration(stats.getUptime())));
    statsTable.setWidget(row, 2, new Label(nf.format(stats.getMaxMemory())));
    statsTable.setWidget(row, 3, new Label(nf.format(stats.getTotalMemory())));
    statsTable.setWidget(row, 4, new Label(nf.format(stats.getTotalMemory() - stats.getFreeMemory())));
    statsTable.setWidget(row, 5, new Label(nf.format(stats.getFreeMemory())));
    statsTable.setWidget(row, 6, new Label(pf.format((float) stats.getFreeMemory() / (float) stats.getTotalMemory())));
    for (int i = 0; i <= 6; i++) {
      statsTable.getCellFormatter().setHorizontalAlignment(row, i, HasHorizontalAlignment.ALIGN_RIGHT);
    }
  }

  private void selectChart() {
    if (chartPicker.getValue(chartPicker.getSelectedIndex()).equalsIgnoreCase("memory")) {
      if (memoryChart == null) {
        memoryChart = new LineChart() {
          protected native JavaScriptObject convertData(String jsonData, String[] headers, String[] fields)
          /*-{
            var d = eval(jsonData);
          
            headers.unshift("x");
            var dd = [headers];
            
            for (var i=0;i<d.length;i++) {
              var stat = d[i];
              var a = stat.totalMemory;
              var u = stat.totalMemory-stat.freeMemory;
              var t = (new Date(stat.time)).toLocaleTimeString();
              dd.push([t, a, u]);
            }
            
            var data = $wnd.google.visualization.arrayToDataTable(dd);
            return data;
          }-*/;
        };
        memoryChart.draw(jsonData, new String[] { "Allocated Memory", "Used Memory" }, null);
      }
      chartHolder.setWidget(memoryChart);
    } else if (chartPicker.getValue(chartPicker.getSelectedIndex()).equalsIgnoreCase("memory5")) {
      if (memoryLast5Chart == null) {
        memoryLast5Chart = new LineChart() {
          protected native JavaScriptObject convertData(String jsonData, String[] headers, String[] fields)
          /*-{
            var d = eval(jsonData);
          
            headers.unshift("x");
            var dd = [headers];

            var fiveMinutesAgo = (new Date()).getTime() - (5*60*1000);
            
            for (var i=0;i<d.length;i++) {
              var stat = d[i];
              if (stat.time >= fiveMinutesAgo) {
                var a = stat.totalMemory;
                var u = stat.totalMemory-stat.freeMemory;
                var t = (new Date(stat.time)).toLocaleTimeString();
                dd.push([t, a, u]);
              }
            }
            
            var data = $wnd.google.visualization.arrayToDataTable(dd);
            return data;
          }-*/;
        };
        memoryLast5Chart.draw(jsonData, new String[] { "Allocated Memory", "Used Memory" }, null);
      }
      chartHolder.setWidget(memoryLast5Chart);
    } else if (chartPicker.getValue(chartPicker.getSelectedIndex()).equalsIgnoreCase("memory10")) {
      if (memoryLast10Chart == null) {
        memoryLast10Chart = new LineChart() {
          protected native JavaScriptObject convertData(String jsonData, String[] headers, String[] fields)
          /*-{
            var d = eval(jsonData);
          
            headers.unshift("x");
            var dd = [headers];

            var fiveMinutesAgo = (new Date()).getTime() - (10*60*1000);
            
            for (var i=0;i<d.length;i++) {
              var stat = d[i];
              if (stat.time >= fiveMinutesAgo) {
                var a = stat.totalMemory;
                var u = stat.totalMemory-stat.freeMemory;
                var t = (new Date(stat.time)).toLocaleTimeString();
                dd.push([t, a, u]);
              }
            }
            
            var data = $wnd.google.visualization.arrayToDataTable(dd);
            return data;
          }-*/;
        };
        memoryLast10Chart.draw(jsonData, new String[] { "Allocated Memory", "Used Memory" }, null);
      }
      chartHolder.setWidget(memoryLast10Chart);
    } else if (chartPicker.getValue(chartPicker.getSelectedIndex()).equalsIgnoreCase("memory30")) {
      if (memoryLast30Chart == null) {
        memoryLast30Chart = new LineChart() {
          protected native JavaScriptObject convertData(String jsonData, String[] headers, String[] fields)
          /*-{
            var d = eval(jsonData);
          
            headers.unshift("x");
            var dd = [headers];

            var fiveMinutesAgo = (new Date()).getTime() - (30*60*1000);
            
            for (var i=0;i<d.length;i++) {
              var stat = d[i];
              if (stat.time >= fiveMinutesAgo) {
                var a = stat.totalMemory;
                var u = stat.totalMemory-stat.freeMemory;
                var t = (new Date(stat.time)).toLocaleTimeString();
                dd.push([t, a, u]);
              }
            }
            
            var data = $wnd.google.visualization.arrayToDataTable(dd);
            return data;
          }-*/;
        };
        memoryLast30Chart.draw(jsonData, new String[] { "Allocated Memory", "Used Memory" }, null);
      }
      chartHolder.setWidget(memoryLast30Chart);
    } else if (chartPicker.getValue(chartPicker.getSelectedIndex()).equalsIgnoreCase("cpuf")) {
      if (cpuFullChart == null) {
        cpuFullChart = new LineChart() {
          protected native JavaScriptObject convertData(String jsonData, String[] headers, String[] fields)
          /*-{
            var d = eval(jsonData);
          
            headers.unshift("x");
            var dd = [headers];
            
            for (var i=0;i<d.length;i++) {
              var cpuStat = d[i].cpuStats;
              var p = cpuStat.processCpuLoad*100;
              var s =  cpuStat.systemCpuLoad*100;
              var t = (new Date(cpuStat.time)).toLocaleTimeString();
              dd.push([t, (p<0)?0:p, (s<0)?0:s]);
            }
            
            var data = $wnd.google.visualization.arrayToDataTable(dd);
            return data;
          }-*/;
        };
        cpuFullChart.setvAxisMinValue(0);
        cpuFullChart.setvAxisMaxValue(100);
        cpuFullChart.draw(jsonData, new String[] { "Process", "System" }, new String[] { "time", "processCpuLoad", "systemCpuLoad" });
      }
      chartHolder.setWidget(cpuFullChart);
    } else if (chartPicker.getValue(chartPicker.getSelectedIndex()).equalsIgnoreCase("cpufa")) {
      if (cpuFullAutoChart == null) {
        cpuFullAutoChart = new LineChart() {
          protected native JavaScriptObject convertData(String jsonData, String[] headers, String[] fields)
          /*-{
            var d = eval(jsonData);
          
            headers.unshift("x");
            var dd = [headers];
            
            for (var i=0;i<d.length;i++) {
              var cpuStat = d[i].cpuStats;
              var p = cpuStat.processCpuLoad*100;
              var s =  cpuStat.systemCpuLoad*100;
              var t = (new Date(cpuStat.time)).toLocaleTimeString();
              dd.push([t, (p<0)?0:p, (s<0)?0:s]);
            }
            
            var data = $wnd.google.visualization.arrayToDataTable(dd);
            return data;
          }-*/;
        };
        cpuFullAutoChart.draw(jsonData, new String[] { "Process", "System" }, new String[] { "time", "processCpuLoad", "systemCpuLoad" });
      }
      chartHolder.setWidget(cpuFullAutoChart);
    } else if (chartPicker.getValue(chartPicker.getSelectedIndex()).equalsIgnoreCase("cpu5")) {
      if (cpuLast5Chart == null) {
        cpuLast5Chart = new LineChart() {
          protected native JavaScriptObject convertData(String jsonData, String[] headers, String[] fields)
          /*-{
            var d = eval(jsonData);
          
            headers.unshift("x");
            var dd = [headers];

            var fiveMinutesAgo = (new Date()).getTime() - (5*60*1000);
            
            for (var i=0;i<d.length;i++) {
              var cpuStat = d[i].cpuStats;
              if (cpuStat.time >= fiveMinutesAgo) {
                var p = cpuStat.processCpuLoad*100;
                var s =  cpuStat.systemCpuLoad*100;
                var t = (new Date(cpuStat.time)).toLocaleTimeString();
                dd.push([t, (p<0)?0:p, (s<0)?0:s]);
              }
            }
            
            var data = $wnd.google.visualization.arrayToDataTable(dd);
            return data;
          }-*/;
        };
        cpuLast5Chart.setvAxisMinValue(0);
        cpuLast5Chart.setvAxisMaxValue(100);
        cpuLast5Chart.draw(jsonData, new String[] { "Process", "System" }, new String[] { "time", "processCpuLoad", "systemCpuLoad" });
      }
      chartHolder.setWidget(cpuLast5Chart);
    } else if (chartPicker.getValue(chartPicker.getSelectedIndex()).equalsIgnoreCase("cpu5a")) {
      if (cpuLast5AutoChart == null) {
        cpuLast5AutoChart = new LineChart() {
          protected native JavaScriptObject convertData(String jsonData, String[] headers, String[] fields)
          /*-{
            var d = eval(jsonData);
          
            headers.unshift("x");
            var dd = [headers];

            var fiveMinutesAgo = (new Date()).getTime() - (5*60*1000);
            
            for (var i=0;i<d.length;i++) {
              var cpuStat = d[i].cpuStats;
              if (cpuStat.time >= fiveMinutesAgo) {
                var p = cpuStat.processCpuLoad*100;
                var s =  cpuStat.systemCpuLoad*100;
                var t = (new Date(cpuStat.time)).toLocaleTimeString();
                dd.push([t, (p<0)?0:p, (s<0)?0:s]);
              }
            }
            
            var data = $wnd.google.visualization.arrayToDataTable(dd);
            return data;
          }-*/;
        };
        cpuLast5AutoChart.draw(jsonData, new String[] { "Process", "System" }, new String[] { "time", "processCpuLoad", "systemCpuLoad" });
      }
      chartHolder.setWidget(cpuLast5AutoChart);
    } else if (chartPicker.getValue(chartPicker.getSelectedIndex()).equalsIgnoreCase("cpu10")) {
      if (cpuLast10Chart == null) {
        cpuLast10Chart = new LineChart() {
          protected native JavaScriptObject convertData(String jsonData, String[] headers, String[] fields)
          /*-{
            var d = eval(jsonData);
          
            headers.unshift("x");
            var dd = [headers];

            var minutesAgo = (new Date()).getTime() - (10*60*1000);
            
            for (var i=0;i<d.length;i++) {
              var cpuStat = d[i].cpuStats;
              if (cpuStat.time >= minutesAgo) {
                var p = cpuStat.processCpuLoad*100;
                var s =  cpuStat.systemCpuLoad*100;
                var t = (new Date(cpuStat.time)).toLocaleTimeString();
                dd.push([t, (p<0)?0:p, (s<0)?0:s]);
              }
            }
            
            var data = $wnd.google.visualization.arrayToDataTable(dd);
            return data;
          }-*/;
        };
        cpuLast10Chart.setvAxisMinValue(0);
        cpuLast10Chart.setvAxisMaxValue(100);
        cpuLast10Chart.draw(jsonData, new String[] { "Process", "System" }, new String[] { "time", "processCpuLoad", "systemCpuLoad" });
      }
      chartHolder.setWidget(cpuLast10Chart);
    } else if (chartPicker.getValue(chartPicker.getSelectedIndex()).equalsIgnoreCase("cpu10a")) {
      if (cpuLast10AutoChart == null) {
        cpuLast10AutoChart = new LineChart() {
          protected native JavaScriptObject convertData(String jsonData, String[] headers, String[] fields)
          /*-{
            var d = eval(jsonData);
          
            headers.unshift("x");
            var dd = [headers];

            var minutesAgo = (new Date()).getTime() - (10*60*1000);
            
            for (var i=0;i<d.length;i++) {
              var cpuStat = d[i].cpuStats;
              if (cpuStat.time >= minutesAgo) {
                var p = cpuStat.processCpuLoad*100;
                var s =  cpuStat.systemCpuLoad*100;
                var t = (new Date(cpuStat.time)).toLocaleTimeString();
                dd.push([t, (p<0)?0:p, (s<0)?0:s]);
              }
            }
            
            var data = $wnd.google.visualization.arrayToDataTable(dd);
            return data;
          }-*/;
        };
        cpuLast10AutoChart.draw(jsonData, new String[] { "Process", "System" }, new String[] { "time", "processCpuLoad", "systemCpuLoad" });
      }
      chartHolder.setWidget(cpuLast10AutoChart);
    } else if (chartPicker.getValue(chartPicker.getSelectedIndex()).equalsIgnoreCase("cpu30")) {
      if (cpuLast30Chart == null) {
        cpuLast30Chart = new LineChart() {
          protected native JavaScriptObject convertData(String jsonData, String[] headers, String[] fields)
          /*-{
            var d = eval(jsonData);
          
            headers.unshift("x");
            var dd = [headers];

            var minutesAgo = (new Date()).getTime() - (30*60*1000);
            
            for (var i=0;i<d.length;i++) {
              var cpuStat = d[i].cpuStats;
              if (cpuStat.time >= minutesAgo) {
                var p = cpuStat.processCpuLoad*100;
                var s =  cpuStat.systemCpuLoad*100;
                var t = (new Date(cpuStat.time)).toLocaleTimeString();
                dd.push([t, (p<0)?0:p, (s<0)?0:s]);
              }
            }
            
            var data = $wnd.google.visualization.arrayToDataTable(dd);
            return data;
          }-*/;
        };
        cpuLast30Chart.setvAxisMinValue(0);
        cpuLast30Chart.setvAxisMaxValue(100);
        cpuLast30Chart.draw(jsonData, new String[] { "Process", "System" }, new String[] { "time", "processCpuLoad", "systemCpuLoad" });
      }
      chartHolder.setWidget(cpuLast30Chart);
    } else if (chartPicker.getValue(chartPicker.getSelectedIndex()).equalsIgnoreCase("cpu30a")) {
      if (cpuLast30AutoChart == null) {
        cpuLast30AutoChart = new LineChart() {
          protected native JavaScriptObject convertData(String jsonData, String[] headers, String[] fields)
          /*-{
            var d = eval(jsonData);
          
            headers.unshift("x");
            var dd = [headers];

            var minutesAgo = (new Date()).getTime() - (30*60*1000);
            
            for (var i=0;i<d.length;i++) {
              var cpuStat = d[i].cpuStats;
              if (cpuStat.time >= minutesAgo) {
                var p = cpuStat.processCpuLoad*100;
                var s =  cpuStat.systemCpuLoad*100;
                var t = (new Date(cpuStat.time)).toLocaleTimeString();
                dd.push([t, (p<0)?0:p, (s<0)?0:s]);
              }
            }
            
            var data = $wnd.google.visualization.arrayToDataTable(dd);
            return data;
          }-*/;
        };
        cpuLast30AutoChart.draw(jsonData, new String[] { "Process", "System" }, new String[] { "time", "processCpuLoad", "systemCpuLoad" });
      }
      chartHolder.setWidget(cpuLast30AutoChart);
    }
  }

  private void fetchSystemStats() {
    for (int row = 1; row < statsTable.getRowCount(); row++) {
      for (int i = 0; i <= 6; i++) {
        statsTable.setWidget(row, i, new Label("."));
      }
    }
    final MethodCallback<List<SystemStats>> callback = new MethodCallback<List<SystemStats>>() {
      public void onFailure(Method method, Throwable caught) {
        Window.alert(caught.getMessage());
      }

      public void onSuccess(Method method, List<SystemStats> stats) {
        populateUI(stats.get(stats.size() - 1));
        jsonData = method.getResponse().getText();
        cpuFullChart = null;
        cpuFullAutoChart = null;
        cpuLast5Chart = null;
        cpuLast5AutoChart = null;
        cpuLast10Chart = null;
        cpuLast10AutoChart = null;
        cpuLast30Chart = null;
        cpuLast30AutoChart = null;
        memoryChart = null;
        memoryLast5Chart = null;
        memoryLast10Chart = null;
        memoryLast30Chart = null;
        selectChart();
      };
    };
    ResourceCache.getBaseResource().getSystemStats(0L, null, null, callback);
  }

  private void requestGarbageCollection() {
    for (int row = 1; row < statsTable.getRowCount(); row++) {
      for (int i = 0; i <= 6; i++) {
        statsTable.setWidget(row, i, new Label("."));
      }
    }
    final MethodCallback<SystemStats> callback = new MethodCallback<SystemStats>() {
      public void onFailure(Method method, Throwable caught) {
        Window.alert(caught.getMessage());
      }

      public void onSuccess(Method method, SystemStats stats) {
        populateUI(stats);
      };
    };
    ResourceCache.getBaseResource().requestGarbageCollection(callback);
  }

}

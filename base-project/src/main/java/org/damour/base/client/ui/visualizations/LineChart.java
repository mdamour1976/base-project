package org.damour.base.client.ui.visualizations;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Timer;

public class LineChart extends Chart {

  private static boolean loaded = false;

  private int vAxisMaxValue = 0;
  private int vAxisMinValue = 0;

  static {
    if (Chart.isLoaded()) {
      load();
    } else {
      Timer t = new Timer() {
        public void run() {
          if (Chart.isLoaded()) {
            load();
            cancel();
          }
        }
      };
      t.scheduleRepeating(250);
    }
  }

  private static native void load()
  /*-{
    var setLoaded = function() {
      @org.damour.base.client.ui.visualizations.LineChart::loaded = true;
    }
    $wnd.google.load('visualization', '1', {'callback':setLoaded, 'packages':['corechart']});
  }-*/;

  public LineChart() {
  }

  public void draw(final String jsonData, final long fetchTime, final String[] headers, final String[] attributes) {
    if (loaded) {
      draw(convertData(jsonData, fetchTime, headers, attributes));
    } else {
      Timer t = new Timer() {
        public void run() {
          if (loaded) {
            draw(convertData(jsonData, fetchTime, headers, attributes));
            cancel();
          }
        }
      };
      t.scheduleRepeating(250);
    }
  }

  protected native JavaScriptObject convertData(String jsonData, long fetchTime, String[] headers, String[] fields)
  /*-{
    var d = eval(jsonData);
    
    headers.unshift("x");
    var dd = [headers];
    
    for (var i=0;i<d.length;i++) {
      var values = [];
      for (var j=0;j<fields.length;j++) {
        values.push(d[i][fields[j]]);
      }      
      dd.push(values);
    }
    
    var data = $wnd.google.visualization.arrayToDataTable(dd);
    return data;
  }-*/;

  private native void draw(JavaScriptObject data)
  /*-{
      var ele = this.@org.damour.base.client.ui.visualizations.LineChart::getElement()();
      var chart = new $wnd.google.visualization.LineChart(ele);
      var vAxisMinValue = this.@org.damour.base.client.ui.visualizations.LineChart::vAxisMinValue;
      var vAxisMaxValue = this.@org.damour.base.client.ui.visualizations.LineChart::vAxisMaxValue;
      chart.draw(data, { vAxis: { maxValue: vAxisMaxValue, minValue: vAxisMinValue }, hAxis: { slantedText: true }, curveType: "none", width: 800, height: 600 });      
  }-*/;

  public int getvAxisMaxValue() {
    return vAxisMaxValue;
  }

  public void setvAxisMaxValue(int vAxisMaxValue) {
    this.vAxisMaxValue = vAxisMaxValue;
  }

  public int getvAxisMinValue() {
    return vAxisMinValue;
  }

  public void setvAxisMinValue(int vAxisMinValue) {
    this.vAxisMinValue = vAxisMinValue;
  }
}

package org.damour.base.client.soundmanager;

import java.util.HashMap;

import com.google.gwt.core.client.JavaScriptObject;

public class HTML5AudioPlayer {

  private static HashMap<String, JavaScriptObject> sounds = new HashMap<String, JavaScriptObject>();
  private static HTML5AudioPlayer instance = new HTML5AudioPlayer();

  public static HTML5AudioPlayer getInstance() {
    return instance;
  }

  private HTML5AudioPlayer() {
  }

  public void createSound(String soundName, String url) {
    JavaScriptObject snd = nativeCreateSound(soundName, url);
    sounds.put(soundName, snd);
  }

  public void playSound(String soundName) {
    JavaScriptObject snd = sounds.get(soundName);
    nativePlaySound(snd);
  }

  private native JavaScriptObject nativeCreateSound(String soundName, String url)
  /*-{
    var snd = new $wnd.buzz.sound(url, {
      formats: [ "mp3", "ogg", "aac", "wav" ]
    });
    return snd;
  }-*/;

  private native void nativePlaySound(JavaScriptObject snd)
  /*-{
    snd.play();
  }-*/;

}

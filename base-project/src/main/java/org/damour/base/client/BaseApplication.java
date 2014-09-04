package org.damour.base.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.damour.base.client.localization.IResourceBundleLoadCallback;
import org.damour.base.client.localization.ResourceBundle;
import org.damour.base.client.objects.Referral;
import org.damour.base.client.objects.User;
import org.damour.base.client.service.ResourceCache;
import org.damour.base.client.ui.IGenericCallback;
import org.damour.base.client.ui.authentication.AuthenticationHandler;
import org.damour.base.client.ui.dialogs.MessageDialogBox;
import org.damour.base.client.utils.StringUtils;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

public class BaseApplication implements EntryPoint, StartupListener {

  public static final String CAPTCHA_SERVICE_PATH = "/servlet/org.damour.base.server.CaptchaImageGeneratorService";
  public static final String FILE_UPLOAD_SERVICE_PATH = "/servlet/org.damour.base.server.FileUploadService";
  public static final String GET_FILE_SERVICE_PATH = "/files/";

  private static boolean loading = false;
  private static boolean initialized = false;
  private static List<StartupListener> startupListeners = new ArrayList<StartupListener>();

  private static ResourceBundle settings = null;
  private static ResourceBundle messages = null;

  private static Referral referral = null;

  public String getModuleName() {
    return "baseproject";
  }

  public static native String getApplicationName()
  /*-{
    return window.top.applicationName;
  }-*/;

  public static native void setApplicationInitialized()
  /*-{
    window.top.applicationInitialized = true;
  }-*/;

  public static native boolean isApplicationInitialized()
  /*-{
    if (window.top.applicationInitialized == undefined) {
      return false;
    }
    return window.top.applicationInitialized;
  }-*/;

  private static HashMap<String, JavaScriptObject> loadedJsMap = new HashMap<String, JavaScriptObject>();

  public static JavaScriptObject loadjsfile(String src, IGenericCallback<JavaScriptObject> loadedCallback) {
    JavaScriptObject script = loadedJsMap.get(src);
    if (script == null) {
      script = _loadjsfile(src, loadedCallback);
      loadedJsMap.put(src, script);
    } else {
      loadedCallback.invoke(script);  
    }
    return script;
  }

  private static native JavaScriptObject _loadjsfile(String src, IGenericCallback<JavaScriptObject> loadedCallback)
  /*-{
    var fileref=$doc.createElement('script');
    fileref.setAttribute("type","text/javascript");
    fileref.setAttribute("src", src);
    $doc.getElementsByTagName("head")[0].appendChild(fileref);
    fileref.onload = function () {
      loadedCallback.@org.damour.base.client.ui.IGenericCallback::invoke(Ljava/lang/Object;)(fileref);
    }
    return fileref;
  }-*/;

  /**
   * The use of this method determines if this module should be loaded. The reason this was introduced was to allow modules which extend/embed this module to
   * prevent the script from loading altogether.
   * 
   * @return whether or not to load the module
   */
  public boolean attemptToLoadModule() {
    return (!isApplicationInitialized()) && getModuleName().equalsIgnoreCase(getApplicationName());
  }

  public void onModuleLoad() {

    if (Window.Navigator.getUserAgent().toLowerCase().indexOf("msie 9") != -1) {
      RootPanel.getBodyElement().addClassName("IE9");
    } else if (Window.Navigator.getUserAgent().toLowerCase().indexOf("msie") != -1) {
      RootPanel.getBodyElement().addClassName("IE");
    }

    if (!attemptToLoadModule()) {
      return;
    }

    RootPanel adContent = RootPanel.get("adContent");
    if (adContent != null) {
      adContent.removeFromParent();
      adContent.setVisible(false);
      adContent.setHeight("0px");
    }

    setApplicationInitialized();

    addStartupListener(this);

    GWT.runAsync(new RunAsyncCallback() {

      public void onSuccess() {
        Referral referral = new Referral();
        referral.referralURL = Document.get().getReferrer();
        if (StringUtils.isEmpty(referral.referralURL)) {
          referral.referralURL = Window.Location.getHref();
        }
        referral.url = Window.Location.getHref();
        ResourceCache.getReferralResource().submitReferral(referral, new MethodCallback<Referral>() {

          public void onSuccess(Method method, Referral result) {
            BaseApplication.referral = result;
          }

          public void onFailure(Method method, Throwable caught) {
          }
        });
      }

      public void onFailure(Throwable reason) {
      }
    });

    if (!loading) {
      loading = true;

      // this is an account validation mode
      if (!StringUtils.isEmpty(Window.Location.getParameter("u")) && !StringUtils.isEmpty(Window.Location.getParameter("v"))) {
        addStartupListener(new StartupListener() {
          public void loadModule() {
            String username = Window.Location.getParameter("u");
            String validationCode = Window.Location.getParameter("v");
            ResourceCache.getUserResource().submitAccountValidation(username, validationCode, new MethodCallback<User>() {

              public void onFailure(Method method, Throwable exception) {
                MessageDialogBox.alert(exception.getMessage());
              }

              public void onSuccess(Method method, User user) {
                if (user != null && user.isValidated()) {
                  AuthenticationHandler.getInstance().setUser(user);
                  AuthenticationHandler.getInstance().handleUserAuthentication(false);
                  MessageDialogBox.alert("Account validation successful.", new IGenericCallback<Void>() {
                    public void invoke(Void object) {
                      Window.Location.assign(Window.Location.getHref().substring(0, Window.Location.getHref().indexOf("?")));
                    }
                  });

                } else {
                  MessageDialogBox.alert("Could not validate account.");
                }
              }
            });
          }
        });
      }
      ResourceBundle.clearCache();
      // load settings, then messages
      loadSettings(new IGenericCallback<Void>() {
        public void invoke(Void object) {
          // now load messages
          loadMessages();
        }
      });
    }
  }

  public static boolean isInitialized() {
    return initialized;
  }

  public void loadSettings(final IGenericCallback<Void> callback) {
    settings = new ResourceBundle();
    settings.loadBundle("settings/", "settings", false, new IResourceBundleLoadCallback() {
      public void bundleLoaded(String bundleName) {
        final ResourceBundle settings_override = new ResourceBundle();
        settings_override.loadBundle("settings/", "settings_override", false, new IResourceBundleLoadCallback() {
          public void bundleLoaded(String bundleName) {
            settings.mergeResourceBundle(settings_override);
            callback.invoke(null);
          }
        });
      }
    });
  }

  public void loadMessages() {
    // when the bundle is loaded, it will fire an event
    // calling our bundleLoaded
    messages = new ResourceBundle();
    messages.loadBundle("messages", "messages", true, new IResourceBundleLoadCallback() {
      public void bundleLoaded(String bundleName) {
        final ResourceBundle messages_override = new ResourceBundle();
        messages_override.loadBundle("messages", "messages_override", true, new IResourceBundleLoadCallback() {
          public void bundleLoaded(String bundleName) {
            messages.mergeResourceBundle(messages_override);
            clearLoadingIndicator();
            initialized = true;
            fireStartupListeners();
          }
        });
      }
    });
  }

  private static void addStartupListener(StartupListener listener) {
    if (isInitialized()) {
      listener.loadModule();
    } else {
      startupListeners.add(listener);
    }
  }

  private static void fireStartupListeners() {
    for (StartupListener startupListener : startupListeners) {
      startupListener.loadModule();
    }
  }

  private static void clearLoadingIndicator() {
    RootPanel loadingPanel = RootPanel.get("loading");
    if (loadingPanel != null) {
      loadingPanel.removeFromParent();
      loadingPanel.setVisible(false);
      loadingPanel.setHeight("0px");
    }
  }

  public static ResourceBundle getSettings() {
    return settings;
  }

  public static ResourceBundle getMessages() {
    return messages;
  }

  public static Referral getReferral() {
    return referral;
  }

  // override this
  public void loadModule() {
  }

}

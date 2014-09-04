package org.damour.base.server;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.damour.base.client.objects.CpuStats;
import org.damour.base.client.objects.SystemStats;
import org.damour.base.client.utils.StringUtils;
import org.damour.base.server.hibernate.HibernateUtil;

import com.sun.management.OperatingSystemMXBean;

public class BaseSystem {

  public static final String DEFAULT_SMTP_HOST = "relay-hosting.secureserver.net";
  private static final long startupDate = System.currentTimeMillis();
  private static String domainName = "" + startupDate;
  private static boolean isDomainNameSet = false;
  private static ClassLoader classLoader = null;
  private static Properties settings = null;

  private static final int SYSTEM_STAT_INTERVAL = 10000; // interval
  private static final int SYSTEM_STAT_SIZE = (24 * 60 * 60 * 1000) / SYSTEM_STAT_INTERVAL; // one day in 30/second intervals
  private static ArrayList<SystemStats> systemStats = new ArrayList<SystemStats>();

  static {
    try {
      setBaseClassLoader(BaseSystem.class.getClassLoader());
    } catch (Throwable t) {
      Logger.log(t);
      try {
        BaseSystem bs = new BaseSystem();
        setBaseClassLoader(bs.getClass().getClassLoader());
      } catch (Throwable tt) {
        Logger.log(tt);
      }
    }

    Thread t = new Thread(new Runnable() {
      public void run() {
        while (true) {
          try {
            systemStats.add(getSystemStat());
            while (systemStats.size() > SYSTEM_STAT_SIZE) {
              systemStats.remove(0);
            }
            Thread.sleep(SYSTEM_STAT_INTERVAL);
          } catch (Throwable t) {
          }
        }
      }
    });
    t.setDaemon(true);
    t.start();
  }

  private static OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

  public static SystemStats getSystemStat() {
    SystemStats stat = new SystemStats();

    stat.setMaxMemory(Runtime.getRuntime().maxMemory());
    stat.setTotalMemory(Runtime.getRuntime().totalMemory());
    stat.setFreeMemory(Runtime.getRuntime().freeMemory());
    stat.setStartupDate(BaseSystem.getStartupDate());
    stat.setUptime(System.currentTimeMillis() - BaseSystem.getStartupDate());
    stat.setCores(Runtime.getRuntime().availableProcessors());

    stat.setArch(osBean.getArch());
    stat.setFreePhysicalMemorySize(osBean.getFreePhysicalMemorySize());
    stat.setFreeSwapSpaceSize(osBean.getFreeSwapSpaceSize());
    stat.setOsName(osBean.getName());
    stat.setTotalPhysicalMemorySize(osBean.getTotalPhysicalMemorySize());
    stat.setTotalSwapSpaceSize(osBean.getTotalSwapSpaceSize());
    stat.setVersion(osBean.getVersion());

    CpuStats cpuStat = new CpuStats();
    cpuStat.setProcessCpuLoad(osBean.getProcessCpuLoad());
    cpuStat.setProcessCpuTime(osBean.getProcessCpuTime());
    cpuStat.setSystemCpuLoad(osBean.getSystemCpuLoad());
    cpuStat.setSystemLoadAverage(osBean.getSystemLoadAverage());
    stat.setCpuStats(cpuStat);

    return stat;
  }

  public static String getBaseUrl(HttpServletRequest request) {
    StringBuffer sb = new StringBuffer();
    sb.append(request.getScheme());
    sb.append("://");
    sb.append(request.getServerName());
    if (request.getServerPort() != 80) {
      sb.append(":");
      sb.append(request.getServerPort());
    }
    return sb.toString();
  }

  public static String patchURL(final String unsafeURL) {
    if (unsafeURL == null) {
      return null;
    }
    String safeURL = unsafeURL;
    safeURL = safeURL.replaceAll("\"", "").replaceAll("\'", "").replaceAll("\\?", "-").replaceAll(" ", "-");
    safeURL = safeURL.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\\[", "").replaceAll("\\]", "");
    safeURL = safeURL.replaceAll("\\$", "").replaceAll("\\@", "-").replaceAll("\\!", "-").replaceAll("\\&", "-");
    safeURL = safeURL.replaceAll("\\+", "-").replaceAll("\\<", "").replaceAll("\\>", "").replaceAll("/", "-");
    safeURL = safeURL.replaceAll(":", "-").replaceAll("\\{", "").replaceAll("\\}", "").replaceAll(",", "-");
    safeURL = safeURL.replaceAll(";", "-").replaceAll("`", "");
    while (safeURL.contains("--")) {
      safeURL = safeURL.replaceAll("--", "-");
    }
    try {
      return java.net.URLEncoder.encode(safeURL, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      Logger.log(e);
      return safeURL;
    }
  }

  public static String getDomainName() {
    return domainName;
  }

  public static String getDomainName(HttpServletRequest request) {
    if (!isDomainNameSet) {
      isDomainNameSet = true;
      domainName = request.getServerName();
      // if (domainName.lastIndexOf(".") > domainName.indexOf(".")) {
      // // remove subdomain
      // domainName = domainName.substring(domainName.indexOf(".") + 1);
      // }
    }
    return domainName;
  }

  public static void setDomainName(String domainName) {
    BaseSystem.domainName = domainName;
  }

  public static String getTempDir() {
    return File.separatorChar + "tmp" + File.separatorChar + BaseSystem.getDomainName() + File.separatorChar;
  }

  public static long getStartupDate() {
    return startupDate;
  }

  public static void setBaseClassLoader(final ClassLoader classLoader) {
    if (BaseSystem.classLoader != classLoader) {
      Logger.log("BaseSystem:setBaseClassLoader = " + classLoader.getClass().getName());
      BaseSystem.classLoader = classLoader;
    }
  }

  public static ClassLoader getBaseClassLoader() {
    if (classLoader == null) {
      return BaseSystem.class.getClassLoader();
    }
    return classLoader;
  }

  public static synchronized void reset() {
    settings = null;
    HibernateUtil.resetHibernate();
    Logger.resetLogger();
  }

  public static boolean requireAccountValidation() {
    return "true".equalsIgnoreCase(getSettings().getProperty("requireAccountValidation"));
  }

  private static IEmailService emailService;

  public static IEmailService getEmailService() {
    if (emailService == null) {
      try {
        if (!StringUtils.isEmpty(getSettings().getProperty("IEmailServiceImpl"))) {
          emailService = (IEmailService) Class.forName(getSettings().getProperty("IEmailServiceImpl")).newInstance();
        } else {
          emailService = new EmailHelper();
        }
      } catch (Throwable t) {
        Logger.log(t);
        try {
          emailService = new EmailHelper();
        } catch (Throwable tt) {
          Logger.log(t);
        }
      }
    }
    return emailService;
  }

  public static String getSmtpHost() {
    String smtpHost = getSettings().getProperty("smtpHost");
    if (StringUtils.isEmpty(smtpHost)) {
      smtpHost = DEFAULT_SMTP_HOST;
    }
    return smtpHost;
  }

  public static String getAdminEmailAddress() {
    String adminEmailAddress = getSettings().getProperty("adminEmailAddress");
    if (StringUtils.isEmpty(adminEmailAddress)) {
      adminEmailAddress = "admin@" + getDomainName();
    }
    return adminEmailAddress;
  }

  public static synchronized Properties getSettings() {
    if (settings != null) {
      return settings;
    }
    settings = new Properties();
    try {
      settings.load(BaseSystem.getBaseClassLoader().getResourceAsStream("settings.properties"));
      Logger.dump(settings);
    } catch (Throwable t) {
      Logger.log(t);
      try {
        settings.load(BaseSystem.class.getClassLoader().getResourceAsStream("settings.properties"));
        Logger.dump(settings);
      } catch (Throwable tt) {
        Logger.log(tt);
      }
    }
    Properties overrides = new Properties();
    try {
      overrides.load(BaseSystem.getBaseClassLoader().getResourceAsStream("settings_override.properties"));
      Logger.dump(overrides);
    } catch (Throwable t) {
      Logger.log(t);
      try {
        overrides.load(BaseSystem.class.getClassLoader().getResourceAsStream("settings_override.properties"));
        Logger.dump(overrides);
      } catch (Throwable tt) {
        Logger.log(tt);
      }
    }
    // add all overrides
    for (Object key : overrides.keySet()) {
      settings.put(key, overrides.get(key));
    }
    return settings;
  }

  public static ArrayList<SystemStats> getSystemStats() {
    return systemStats;
  }

  public static void setCpuStats(ArrayList<SystemStats> systemStats) {
    BaseSystem.systemStats = systemStats;
  }
}

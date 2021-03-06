package org.damour.base.server.resource;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.damour.base.client.objects.AdvertisingInfo;
import org.damour.base.client.objects.Email;
import org.damour.base.client.objects.Feedback;
import org.damour.base.client.objects.FileUploadStatus;
import org.damour.base.client.objects.HibernateStat;
import org.damour.base.client.objects.StringWrapper;
import org.damour.base.client.objects.SystemStats;
import org.damour.base.client.objects.User;
import org.damour.base.client.utils.StringUtils;
import org.damour.base.server.BaseSystem;
import org.damour.base.server.Logger;
import org.damour.base.server.hibernate.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;

@Path("/base")
public class BaseResource {

  public static HashMap<User, FileUploadStatus> fileUploadStatusMap = new HashMap<User, FileUploadStatus>();

  @GET
  @Path("/hibernate/stats")
  @Produces(MediaType.APPLICATION_JSON)
  public List<HibernateStat> getHibernateStats(@Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    List<HibernateStat> statsList = new ArrayList<HibernateStat>();

    User authUser = (new UserResource()).getAuthenticatedUser(httpRequest, httpResponse);
    if (authUser == null || !authUser.isAdministrator()) {
      return statsList;
    }

    Statistics stats = HibernateUtil.getInstance().getSessionFactory().getStatistics();

    String regionNames[] = stats.getSecondLevelCacheRegionNames();
    for (String regionName : regionNames) {
      SecondLevelCacheStatistics regionStat = stats.getSecondLevelCacheStatistics(regionName);

      HibernateStat newstat = new HibernateStat();
      newstat.setRegionName(regionName);
      newstat.setCachePuts(regionStat.getPutCount());
      newstat.setCacheHits(regionStat.getHitCount());
      newstat.setCacheMisses(regionStat.getMissCount());
      newstat.setMemoryUsed(regionStat.getSizeInMemory());
      newstat.setNumObjectsInMemory(regionStat.getElementCountInMemory());
      newstat.setNumObjectsOnDisk(regionStat.getElementCountOnDisk());
      statsList.add(newstat);
    }

    return statsList;
  }

  @POST
  @Path("/hibernate/reset")
  @Produces(MediaType.APPLICATION_JSON)
  public void resetHibernate(@Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    User authUser = (new UserResource()).getAuthenticatedUser(httpRequest, httpResponse);
    if (authUser != null && authUser.isAdministrator()) {
      HibernateUtil.resetHibernate();
    }
  }

  @DELETE
  @Path("/hibernate/evict/{classname}")
  public void evictClassFromCache(@PathParam("classname") String className, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    User authUser = (new UserResource()).getAuthenticatedUser(httpRequest, httpResponse);
    if (authUser != null && authUser.isAdministrator()) {

      try {
        if (className.equals("org.hibernate.cache.StandardQueryCache")) {
          HibernateUtil.getInstance().getSessionFactory().getCache().evictQueryRegions();
          HibernateUtil.getInstance().getSessionFactory().getCache().evictDefaultQueryRegion();
        } else {
          HibernateUtil.getInstance().getSessionFactory().getCache().evictEntityRegion(className);
        }
        Logger.log("Evicted: " + className);
      } catch (Throwable t) {
        Logger.log(t);
      }
    }
  }

  @GET
  @Path("/startupDate")
  @Produces(MediaType.APPLICATION_JSON)
  public Date getServerStartupDate() {
    return new Date(BaseSystem.getStartupDate());
  }

  @GET
  @Path("/uptime")
  @Produces(MediaType.TEXT_PLAIN)
  public long getUptime() {
    return System.currentTimeMillis() - BaseSystem.getStartupDate();
  }

  @POST
  @Path("/hibernate/executeHQL")
  @Produces(MediaType.TEXT_PLAIN)
  public String executeHQL(String query, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    return executeHQLInternal(query, false, httpRequest, httpResponse);
  }

  @POST
  @Path("/hibernate/executeUpdateHQL")
  @Produces(MediaType.TEXT_PLAIN)
  public String executeUpdateHQL(String query, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    return executeHQLInternal(query, true, httpRequest, httpResponse);
  }

  public String executeHQLInternal(String query, Boolean executeUpdate, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
    if (StringUtils.isEmpty(query)) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    Session session = HibernateUtil.getInstance().getSession();
    Transaction tx = null;
    try {
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser == null || !authUser.isAdministrator()) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      String result;
      if (executeUpdate) {
        tx = HibernateUtil.getInstance().getSession().beginTransaction();
        result = "{ \"rowsAffected\": " + session.createQuery(query).executeUpdate() + " }";
        tx.commit();
      } else {
        List<?> list = session.createQuery(query).list();
        result = "\"[";
        for (int i = 0; i < list.size(); i++) {
          if (i > 0) {
            result += ", ";
          }
          result += "{ " + list.get(i).toString() + " }";
        }
        result += "]\"";
      }

      return result;
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/system/stats")
  @Produces(MediaType.APPLICATION_JSON)
  public List<SystemStats> getSystemStats(@QueryParam("from") Long from, @QueryParam("to") Long to, @QueryParam("ago") Long ago,
      @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    List<SystemStats> stats = new ArrayList<SystemStats>();
    if (ago != null) {
      to = System.currentTimeMillis();
      from = to - ago;
    }
    if (to == null) {
      to = System.currentTimeMillis();
    }
    if (from != null && BaseSystem.getSystemStats().size() > 0) {
      for (SystemStats stat : BaseSystem.getSystemStats()) {
        if (stat.getTime() >= from && stat.getTime() <= to) {
          stats.add(stat);
        }
      }
    } else {
      stats.add(BaseSystem.getSystemStat());
    }
    return stats;
  }

  @POST
  @Path("/memory/gc")
  @Produces(MediaType.APPLICATION_JSON)
  public SystemStats requestGarbageCollection(@Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    User authUser = (new UserResource()).getAuthenticatedUser(httpRequest, httpResponse);
    if (authUser != null && authUser.isAdministrator()) {
      try {
        System.gc();
      } catch (Throwable t) {
        Logger.log(t);
      }
    }
    return getSystemStats(null, null, null, httpRequest, httpResponse).get(0);
  }

  @GET
  @Path("/ping")
  @Produces(MediaType.APPLICATION_JSON)
  public Long ping(@Context HttpServletRequest httpRequest) {
    Logger.log("Ping received from: " + httpRequest.getRemoteAddr());
    return System.currentTimeMillis();
  }

  @POST
  @Path("advertise")
  public Boolean submitAdvertisingInfo(AdvertisingInfo info, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    String text = "Contact Name: " + info.getContactName() + "<BR>";
    text += "E-Mail: " + info.getEmail() + "<BR>";
    text += "Company: " + info.getCompany() + "<BR>";
    text += "Phone: " + info.getPhone() + "<BR>";
    text += "Comments: " + info.getComments() + "<BR>";
    return BaseSystem.getEmailService().sendMessage(BaseSystem.getSmtpHost(), BaseSystem.getAdminEmailAddress(), info.getContactName(),
        BaseSystem.getAdminEmailAddress(), info.getContactName() + " is interested in advertising on " + BaseSystem.getDomainName(), text);
  }

  @POST
  @Path("feedback")
  public Boolean submitFeedback(Feedback feedback, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    String text = "Name: " + feedback.getContactName() + "<BR>";
    text += "E-Mail: " + feedback.getEmail() + "<BR>";
    text += "Phone: " + feedback.getPhone() + "<BR>";
    if (feedback.getDate() != null) {
      text += "Date: " + feedback.getDate() + "<BR>";
    }
    if (feedback.getMessage() != null) {
      text += feedback.getMessage() + "<BR>";
    }
    String subject = feedback.getContactName() + " has submitted feedback for " + httpRequest.getServerName();
    if (feedback.getSubject() != null) {
      subject = feedback.getSubject();
    }
    return BaseSystem.getEmailService().sendMessage(BaseSystem.getSmtpHost(), BaseSystem.getAdminEmailAddress(), feedback.getContactName(),
        BaseSystem.getAdminEmailAddress(), subject, text);
  }

  @POST
  @Path("email/{id}")
  public void sendEmail(@PathParam("id") Long id, Email email, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (email == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    Session session = HibernateUtil.getInstance().getSession();
    try {
      User authUser = null;
      try {
        authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      } catch (Throwable t) {
      }
      if (authUser != null) {
        email.setFromName(authUser.getFirstname() + " " + authUser.getLastname());
      }
      StringTokenizer st = new StringTokenizer(email.getToAddresses(), ";");
      while (st.hasMoreTokens()) {
        String toAddress = st.nextToken();
        String toName = st.nextToken();

        // replace {toAddress} with toAddress on server
        // replace {toName} with toName on server
        String tmpSubject = email.getSubject();
        tmpSubject = tmpSubject.replace("{toAddress}", toAddress); //$NON-NLS-1$ 
        tmpSubject = tmpSubject.replace("{toName}", toName); //$NON-NLS-1$ 

        String tmpMessage = email.getMessage();
        tmpMessage = tmpMessage.replace("{toAddress}", toAddress); //$NON-NLS-1$ 
        tmpMessage = tmpMessage.replace("{toName}", toName); //$NON-NLS-1$ 

        BaseSystem.getEmailService().sendMessage(BaseSystem.getSmtpHost(), BaseSystem.getAdminEmailAddress(), email.getFromName(), toAddress, tmpSubject,
            tmpMessage);
      }
    } catch (Throwable t) {
      Logger.log(t);
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      session.close();
    }
  }

  public FileUploadStatus getFileUploadStatus(@Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = HibernateUtil.getInstance().getSession();
    try {
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      FileUploadStatus status = fileUploadStatusMap.get(authUser);
      if (status == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }
      return status;
    } catch (Throwable t) {
      Logger.log(t);
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      session.close();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/log/{lines}")
  public StringWrapper getLog(@PathParam("lines") Long lines, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = HibernateUtil.getInstance().getSession();
    try {
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser == null || !authUser.isAdministrator()) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      String logString = null;
      if (lines != null && lines > 0) {
        // tail, by our definition, will just return the last 4k of text
        File logFile = new File(Logger.getLogName());
        FileInputStream logStream = new FileInputStream(logFile);
        if (logFile.length() > lines) {
          long skip = logFile.length() - lines;
          logStream.skip(skip);
        }
        logString = IOUtils.toString(logStream);
        logString = logString.substring(logString.indexOf("\n") + 1);
        logStream.close();
      } else {
        FileInputStream logStream = new FileInputStream(Logger.getLogName());
        logString = IOUtils.toString(logStream);
        logStream.close();
      }
      logString = Logger.convertStringToHTML(logString);
      return new StringWrapper(logString);
    } catch (Exception ex) {
      return new StringWrapper(Logger.convertStringToHTML(Logger.convertThrowableToString(ex)));
    } finally {
      session.close();
    }
  }

}
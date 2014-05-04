package org.damour.base.server.resource;

import java.util.ArrayList;
import java.util.Date;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.damour.base.client.exceptions.SimpleMessageException;
import org.damour.base.client.objects.AdvertisingInfo;
import org.damour.base.client.objects.Email;
import org.damour.base.client.objects.Feedback;
import org.damour.base.client.objects.HibernateStat;
import org.damour.base.client.objects.MemoryStats;
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
  @Path("/memory/stats")
  @Produces(MediaType.APPLICATION_JSON)
  public MemoryStats getMemoryStats(@Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) throws SimpleMessageException {
    MemoryStats stats = new MemoryStats();
    stats.setMaxMemory(Runtime.getRuntime().maxMemory());
    stats.setTotalMemory(Runtime.getRuntime().totalMemory());
    stats.setFreeMemory(Runtime.getRuntime().freeMemory());
    return stats;
  }

  @POST
  @Path("/memory/gc")
  @Produces(MediaType.APPLICATION_JSON)
  public MemoryStats requestGarbageCollection(@Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) throws SimpleMessageException {
    User authUser = (new UserResource()).getAuthenticatedUser(httpRequest, httpResponse);
    if (authUser != null && authUser.isAdministrator()) {
      try {
        System.gc();
      } catch (Throwable t) {
        Logger.log(t);
      }
    }
    return getMemoryStats(httpRequest, httpResponse);
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
    String text = "Contact Name: " + feedback.getContactName() + "<BR>";
    text += "E-Mail: " + feedback.getEmail() + "<BR>";
    text += "Phone: " + feedback.getPhone() + "<BR>";
    text += "Comments: " + feedback.getComments() + "<BR>";
    return BaseSystem.getEmailService().sendMessage(BaseSystem.getSmtpHost(), BaseSystem.getAdminEmailAddress(), feedback.getContactName(),
        BaseSystem.getAdminEmailAddress(), feedback.getContactName() + " has submitted feedback for " + BaseSystem.getDomainName(), text);
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
}
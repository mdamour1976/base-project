package org.damour.base.server.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.damour.base.client.exceptions.SimpleMessageException;
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
  public List<HibernateStat> getHibernateStats(@Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse)
      throws SimpleMessageException {
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
  public void resetHibernate(@Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) throws SimpleMessageException {
    User authUser = (new UserResource()).getAuthenticatedUser(httpRequest, httpResponse);
    if (authUser != null && authUser.isAdministrator()) {
      HibernateUtil.resetHibernate();
    }
  }

  @DELETE
  @Path("/hibernate/evict/{classname}")
  public void evictClassFromCache(@PathParam("classname") String className, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse)
      throws SimpleMessageException {
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
      throw new SimpleMessageException("Query not supplied.");
    }
    Session session = HibernateUtil.getInstance().getSession();
    User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
    if (authUser == null || !authUser.isAdministrator()) {
      throw new SimpleMessageException("Insufficient authorization.");
    }
    Transaction tx = null;
    try {
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
      throw new SimpleMessageException(t.getMessage());
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

}
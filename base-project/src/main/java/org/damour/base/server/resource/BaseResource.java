package org.damour.base.server.resource;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.damour.base.client.exceptions.LoginException;
import org.damour.base.client.exceptions.SimpleMessageException;
import org.damour.base.client.objects.GroupMembership;
import org.damour.base.client.objects.HibernateStat;
import org.damour.base.client.objects.MemoryStats;
import org.damour.base.client.objects.Page;
import org.damour.base.client.objects.PageInfo;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.PermissibleObjectTreeNode;
import org.damour.base.client.objects.PermissibleObjectTreeRequest;
import org.damour.base.client.objects.Permission.PERM;
import org.damour.base.client.objects.Referral;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.client.utils.StringUtils;
import org.damour.base.server.BaseService;
import org.damour.base.server.Logger;
import org.damour.base.server.hibernate.HibernateUtil;
import org.damour.base.server.hibernate.helpers.PageHelper;
import org.damour.base.server.hibernate.helpers.RepositoryHelper;
import org.damour.base.server.hibernate.helpers.SecurityHelper;
import org.damour.base.server.hibernate.helpers.UserHelper;
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
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      List<HibernateStat> statsList = new ArrayList<HibernateStat>();

      User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
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
    } finally {
      session.close();
    }
  }

  @POST
  @Path("/hibernate/reset")
  @Produces(MediaType.APPLICATION_JSON)
  public void resetHibernate(@Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser != null && authUser.isAdministrator()) {
        HibernateUtil.resetHibernate();
      }
    } finally {
      session.close();
    }
  }

  @DELETE
  @Path("/hibernate/evict/{classname}")
  public void evictClassFromCache(@PathParam("classname") String className, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse)
      throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser != null && authUser.isAdministrator()) {
        try {
          HibernateUtil.getInstance().getSessionFactory().getCache().evictEntityRegion(className);
          Logger.log("Evicted: " + className);
        } catch (Throwable t) {
          Logger.log(t);
        }
      }
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
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser != null && authUser.isAdministrator()) {
        try {
          System.gc();
        } catch (Throwable t) {
          Logger.log(t);
        }
      }
      return getMemoryStats(httpRequest, httpResponse);
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/ping")
  @Produces(MediaType.APPLICATION_JSON)
  public Long ping(@Context HttpServletRequest httpRequest) {
    Logger.log("Ping received from: " + httpRequest.getRemoteAddr());
    return System.currentTimeMillis();
  }

  @PUT
  @Path("/referral")
  public Referral submitReferral(Referral referral, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      if (referral == null) {
        throw new SimpleMessageException("Referral provided is null");
      }

      List<Referral> referrals = session.createQuery("from " + Referral.class.getSimpleName() + " where referralURL = '" + referral.getReferralURL() + "'")
          .setMaxResults(1).setCacheable(true).list();
      if (referrals.size() > 0) {
        referral = referrals.get(0);
        referral.counter++;
      } else {
        referral.counter = 1L;
      }
      referral.recentDate = System.currentTimeMillis();

      if (StringUtils.isEmpty(referral.getReferralURL())) {
        throw new SimpleMessageException("Referral does not contain referral url");
      }

      Transaction tx = session.beginTransaction();
      try {
        session.save(referral);
        tx.commit();
        return referral;
      } catch (Throwable t) {
        Logger.log(t);
        try {
          tx.rollback();
        } catch (Throwable tt) {
        }
        throw new SimpleMessageException(t.getMessage());
      }
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/referral/{id : .+}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Referral> getReferrals(@PathParam("id") Long id, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse)
      throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      List<Referral> referrals = null;
      if (id == null) {
        referrals = session.createQuery("from " + Referral.class.getSimpleName()).setCacheable(true).list();
      } else {
        referrals = session.createQuery("from " + Referral.class.getSimpleName() + " where subject.id = " + id).setCacheable(true).list();
      }
      return referrals;
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/referral")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Referral> getReferrals(@Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      return session.createQuery("from " + Referral.class.getSimpleName()).setCacheable(true).list();
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/authenticated-user")
  @Produces(MediaType.APPLICATION_JSON)
  public User getAuthenticatedUser(@Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();

      Cookie cookies[] = httpRequest.getCookies();
      Cookie userCookie = null;
      Cookie userAuthCookie = null;
      for (int i = 0; cookies != null && i < cookies.length; i++) {
        if (cookies[i].getName().equals("user") && !cookies[i].getValue().equals("")) {
          userCookie = cookies[i];
        } else if (cookies[i].getName().equals("auth") && !cookies[i].getValue().equals("")) {
          userAuthCookie = cookies[i];
        }
      }
      if (userCookie == null || userAuthCookie == null) {
        throw new LoginException("Could not get authenticated user.");
      }
      User user = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
      if (user == null) {
        BaseService.destroyAuthCookies(httpRequest, httpResponse);
        throw new LoginException("Could not get authenticated user.");
      }
      return user;
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/users")
  @Produces(MediaType.APPLICATION_JSON)
  public List<User> getUsers() throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      return SecurityHelper.getUsers(session);
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/user/{username : .+}")
  @Produces(MediaType.APPLICATION_JSON)
  public User getUser(@PathParam("username") String username) throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      return UserHelper.getUser(session, username);
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/usernames")
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> getUsernames() throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      // this is a non-admin function
      return SecurityHelper.getUsernames(session);
    } finally {
      session.close();
    }
  }

  @POST
  @Path("/users-in-group")
  @Produces(MediaType.APPLICATION_JSON)
  public List<User> getUsers(UserGroup group, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser == null) {
        throw new SimpleMessageException("User is not authenticated.");
      }
      group = (UserGroup) session.load(UserGroup.class, group.getId());
      // only the group owner, group members and administrator can see the users in a group
      if (authUser.isAdministrator() || authUser.equals(group.getOwner())) {
        return SecurityHelper.getUsersInUserGroup(session, group);
      }
      // now check the groups for the user against the group
      List<GroupMembership> memberships = SecurityHelper.getGroupMemberships(session, authUser);
      if (memberships.contains(group)) {
        return SecurityHelper.getUsersInUserGroup(session, group);
      }
      throw new SimpleMessageException("User is not authorized to list users in group.");
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/groups")
  @Produces(MediaType.APPLICATION_JSON)
  public List<UserGroup> getGroups(@Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
      // the admin can list all groups
      if (authUser != null && authUser.isAdministrator()) {
        return SecurityHelper.getUserGroups(session);
      }
      return SecurityHelper.getVisibleUserGroups(session);
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/groups/{username : .+}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<UserGroup> getGroups(@PathParam("username") String username, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse)
      throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User inUser = UserHelper.getUser(session, username);
      User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
      // the admin & actual user can list all groups for the user
      if (authUser != null && (authUser.isAdministrator() || authUser.equals(inUser))) {
        return SecurityHelper.getUserGroups(session, inUser);
      }
      // everyone else can only see visible groups for the user
      return SecurityHelper.getVisibleUserGroups(session, inUser);
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/owned-groups/{username : .+}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<UserGroup> getOwnedGroups(@PathParam("username") String username, @Context HttpServletRequest httpRequest,
      @Context HttpServletResponse httpResponse) throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
      // if we are the admin, and we are asking to list owned admin groups,
      // we show all groups to the admin
      if (authUser != null && authUser.isAdministrator()) {
        return SecurityHelper.getUserGroups(session);
      }
      User user = UserHelper.getUser(session, username);
      // the actual user can list all owned groups for the user
      if (authUser != null && authUser.equals(user)) {
        return SecurityHelper.getOwnedUserGroups(session, user);
      }
      // if we are not the admin or the actual user, we can only list the visible groups
      // to unknown people
      return SecurityHelper.getOwnedVisibleUserGroups(session, user);
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/objects/{id : .+}")
  @Produces(MediaType.APPLICATION_JSON)
  public PermissibleObject getPermissibleObject(@PathParam("id") Long id, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse)
      throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      if (id == null) {
        throw new SimpleMessageException("Id not supplied.");
      }
      User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
      try {
        PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);
        if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
          throw new SimpleMessageException("User is not authorized to get this content.");
        }
        return permissibleObject;
      } catch (Throwable t) {
        Logger.log(t);
        throw new SimpleMessageException(t.getMessage());
      }
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/objects/{id}/children/{objectType}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<PermissibleObject> getPermissibleObject(@PathParam("id") Long id, @PathParam("objectType") String objectType,
      @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      if (id == null) {
        throw new SimpleMessageException("Id not supplied.");
      }
      User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
      try {
        PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);
        if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
          throw new SimpleMessageException("User is not authorized to get this content.");
        }

        ArrayList<PermissibleObject> objects = new ArrayList<PermissibleObject>();
        Class<?> clazz = Class.forName(objectType);
        RepositoryHelper.getPermissibleObjects(session, authUser, objects, permissibleObject, clazz);
        return objects;

      } catch (Throwable t) {
        Logger.log(t);
        throw new SimpleMessageException(t.getMessage());
      }
    } finally {
      session.close();
    }
  }

  @POST
  @Path("/objects/tree")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public PermissibleObjectTreeNode getPermissibleObjectTree(PermissibleObjectTreeRequest request, @Context HttpServletRequest httpRequest,
      @Context HttpServletResponse httpResponse) throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
      PermissibleObjectTreeNode root = new PermissibleObjectTreeNode();
      PermissibleObject parent = request.getParent();
      if (parent != null) {
        parent = getPermissibleObject(parent.getId(), httpRequest, httpResponse);
      }
      User owner = request.getOwner();
      if (owner == null || owner.getId() == null) {
        owner = null;
      }
      RepositoryHelper.buildPermissibleObjectTreeNode(session, authUser, owner, BaseService.getVoterGUID(httpRequest, httpResponse), root, parent,
          request.getAcceptedClasses(), 0, request.getFetchDepth(), request.getMetaDataFetchDepth());
      return root;
    } finally {
      session.close();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("objects/page/{id}/{pageClassType}/{sortField}/{sortDescending}/{pageNumber}/{pageSize}")
  public Page<PermissibleObject> getPage(@PathParam("id") Long id, @PathParam("pageClassType") String pageClassType, @PathParam("sortField") String sortField,
      @PathParam("sortDescending") boolean sortDescending, @PathParam("pageNumber") int pageNumber, @PathParam("pageSize") int pageSize,
      @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
      try {
        Class<?> clazz = Class.forName(pageClassType);
        PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);
        if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
          throw new SimpleMessageException("User is not authorized to get this content.");
        }
        return PageHelper.getPage(session, permissibleObject, clazz, authUser, sortField, sortDescending, pageNumber, pageSize);
      } catch (Throwable t) {
        Logger.log(t);
        throw new SimpleMessageException(t.getMessage());
      }
    } finally {
      session.close();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("objects/page/{id}/{pageClassType}/{pageSize}")
  public PageInfo getPageInfo(@PathParam("id") Long id, @PathParam("pageClassType") String pageClassType, @PathParam("pageSize") int pageSize,
      @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) throws SimpleMessageException {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      try {
        User authUser = BaseService.getAuthenticatedUser(session, httpRequest, httpResponse);
        PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);
        if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
          throw new SimpleMessageException("User is not authorized to get this content.");
        }
        Class<?> clazz = Class.forName(pageClassType);
        return PageHelper.getPageInfo(session, permissibleObject, clazz, authUser, pageSize);
      } catch (Throwable t) {
        Logger.log(t);
        throw new SimpleMessageException(t.getMessage());
      }
    } finally {
      session.close();
    }
  }

}
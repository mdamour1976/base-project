package org.damour.base.server.resource;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.Permission.PERM;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserAdvisory;
import org.damour.base.client.objects.UserRating;
import org.damour.base.client.objects.UserThumb;
import org.damour.base.client.utils.StringUtils;
import org.damour.base.server.Logger;
import org.damour.base.server.hibernate.HibernateUtil;
import org.damour.base.server.hibernate.helpers.AdvisoryHelper;
import org.damour.base.server.hibernate.helpers.RatingHelper;
import org.damour.base.server.hibernate.helpers.SecurityHelper;
import org.damour.base.server.hibernate.helpers.ThumbHelper;
import org.hibernate.Session;
import org.hibernate.Transaction;

@Path("/rate")
public class RatingResource {
  @GET
  @Path("/{id}/rating")
  @Produces(MediaType.APPLICATION_JSON)
  public UserRating getUserRating(@PathParam("id") Long id, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (id == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);
      if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      // find rating based on remote address if needed
      return RatingHelper.getUserRating(session, permissibleObject, authUser, RatingHelper.getVoterGUID(httpRequest, httpResponse));
    } catch (Throwable t) {
      Logger.log(t);
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      session.close();
    }
  }

  @PUT
  @Path("/{id}/rating")
  @Produces(MediaType.APPLICATION_JSON)
  public UserRating setUserRating(@PathParam("id") Long id, int rating, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (id == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    Session session = null;
    Transaction tx = null;

    try {
      session = HibernateUtil.getInstance().getSession();
      tx = session.beginTransaction();
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);

      PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);

      if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }

      UserRating userRating = RatingHelper.getUserRating(session, permissibleObject, authUser, RatingHelper.getVoterGUID(httpRequest, httpResponse));
      // check if rating already exists
      if (userRating != null) {
        // TODO: consider changing the vote
        // simply subtract the previous amount and decrement the numRatingVotes and redivide
        throw new WebApplicationException(Response.Status.CONFLICT);
      }

      float totalRating = (float) permissibleObject.getNumRatingVotes() * permissibleObject.getAverageRating();
      totalRating += rating;
      permissibleObject.setNumRatingVotes(permissibleObject.getNumRatingVotes() + 1);
      float newAvg = totalRating / (float) permissibleObject.getNumRatingVotes();
      permissibleObject.setAverageRating(newAvg);
      session.save(permissibleObject);

      userRating = new UserRating();
      userRating.setPermissibleObject(permissibleObject);
      userRating.setRating(rating);
      userRating.setVoter(authUser);
      userRating.setVoterGUID(RatingHelper.getVoterGUID(httpRequest, httpResponse));

      session.save(userRating);
      tx.commit();
      return userRating;
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
  @Path("/{id}/next")
  @Produces(MediaType.APPLICATION_JSON)
  public PermissibleObject getNextUnratedPermissibleObject(String objectType, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (StringUtils.isEmpty(objectType)) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Session session = null;

    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);

      PermissibleObject object = RatingHelper.getNextUnratedPermissibleObject(session, objectType, authUser,
          RatingHelper.getVoterGUID(httpRequest, httpResponse));
      return object;
    } catch (Throwable t) {
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/{id}/advisory")
  @Produces(MediaType.APPLICATION_JSON)
  public UserAdvisory getUserAdvisory(@PathParam("id") Long id, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (id == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Session session = null;

    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = null;

      try {
        authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      } catch (Throwable t) {
      }

      PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);

      if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      // find rating based on remote address if needed
      return AdvisoryHelper.getUserAdvisory(session, permissibleObject, authUser, RatingHelper.getVoterGUID(httpRequest, httpResponse));
    } catch (Throwable t) {
      Logger.log(t);
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      session.close();
    }
  }

  @PUT
  @Path("/{id}/advisory")
  @Produces(MediaType.APPLICATION_JSON)
  public UserAdvisory setUserAdvisory(@PathParam("id") Long id, int advisory, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (id == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Session session = null;
    Transaction tx = null;

    try {
      session = HibernateUtil.getInstance().getSession();
      tx = session.beginTransaction();
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);

      PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);

      if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }

      // check if rating already exists
      UserAdvisory userAdvisory = AdvisoryHelper.getUserAdvisory(session, permissibleObject, authUser, RatingHelper.getVoterGUID(httpRequest, httpResponse));
      if (userAdvisory != null) {
        throw new WebApplicationException(Response.Status.CONFLICT);
      }

      float totalAdvisory = (float) permissibleObject.getNumAdvisoryVotes() * permissibleObject.getAverageAdvisory();
      totalAdvisory += advisory;
      permissibleObject.setNumAdvisoryVotes(permissibleObject.getNumAdvisoryVotes() + 1);
      float newAvg = totalAdvisory / (float) permissibleObject.getNumAdvisoryVotes();
      permissibleObject.setAverageAdvisory(newAvg);
      session.save(permissibleObject);

      userAdvisory = new UserAdvisory();
      userAdvisory.setPermissibleObject(permissibleObject);
      userAdvisory.setRating(advisory);
      userAdvisory.setVoter(authUser);
      userAdvisory.setVoterGUID(RatingHelper.getVoterGUID(httpRequest, httpResponse));

      session.save(userAdvisory);
      tx.commit();
      return userAdvisory;
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
  @Path("/{id}/thumb")
  @Produces(MediaType.APPLICATION_JSON)
  public UserThumb getUserThumb(@PathParam("id") Long id, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (id == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Session session = null;

    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = null;

      try {
        authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      } catch (Throwable t) {
      }

      PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);
      if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      // find thumb based on remote address if needed
      return ThumbHelper.getUserThumb(session, permissibleObject, authUser, RatingHelper.getVoterGUID(httpRequest, httpResponse));
    } catch (Throwable t) {
      Logger.log(t);
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      session.close();
    }

  }

  @PUT
  @Path("/{id}/thumb")
  @Produces(MediaType.APPLICATION_JSON)
  public UserThumb setUserThumb(@PathParam("id") Long id, boolean like, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (id == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Session session = null;
    Transaction tx = null;

    try {
      session = HibernateUtil.getInstance().getSession();
      tx = session.beginTransaction();
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);

      PermissibleObject permissibleObject = (PermissibleObject) session.load(PermissibleObject.class, id);

      if (!SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }

      UserThumb userThumb = ThumbHelper.getUserThumb(session, permissibleObject, authUser, RatingHelper.getVoterGUID(httpRequest, httpResponse));
      // check if thumb already exists
      if (userThumb != null) {
        // TODO: consider changing the vote
        // simply subtract the previous amount and decrement the numRatingVotes and redivide
        throw new WebApplicationException(Response.Status.CONFLICT);
      }

      if (like) {
        permissibleObject.setNumUpVotes(permissibleObject.getNumUpVotes() + 1);
      } else {
        permissibleObject.setNumDownVotes(permissibleObject.getNumDownVotes() + 1);
      }
      session.save(permissibleObject);

      userThumb = new UserThumb();
      userThumb.setPermissibleObject(permissibleObject);
      userThumb.setLikeThumb(like);
      userThumb.setVoter(authUser);
      userThumb.setVoterGUID(RatingHelper.getVoterGUID(httpRequest, httpResponse));

      session.save(userThumb);
      tx.commit();
      return userThumb;
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
  @Path("/mostRated/{classType}/{maxResults}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<PermissibleObject> getMostRated(@PathParam("classType") String classType, @PathParam("maxResults") int maxResults,
      @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (classType == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = null;
      try {
        authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      } catch (Throwable t) {
      }
      List<PermissibleObject> mostRated = new ArrayList<PermissibleObject>();
      String simpleClassName = Class.forName(classType).getSimpleName();
      List<PermissibleObject> list = session.createQuery("from " + simpleClassName + " where numRatingVotes > 0 order by numRatingVotes desc")
          .setMaxResults(maxResults).setCacheable(true).list();
      for (PermissibleObject permissibleObject : list) {
        if (SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
          mostRated.add(permissibleObject);
        }
      }
      return mostRated;
    } catch (Throwable t) {
      Logger.log(t);
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @Path("/top/{classType}/{minNumVotes}/{maxResults}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<PermissibleObject> getTopRated(@PathParam("classType") String classType, @PathParam("minNumVotes") int minNumVotes,
      @PathParam("maxResults") int maxResults, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (classType == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = null;

      try {
        authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      } catch (Throwable t) {
      }
      List<PermissibleObject> mostRated = new ArrayList<PermissibleObject>();
      String simpleClassName = Class.forName(classType).getSimpleName();
      List<PermissibleObject> list = session
          .createQuery("from " + simpleClassName + " where numRatingVotes >= " + minNumVotes + " order by averageRating desc").setMaxResults(maxResults)
          .setCacheable(true).list();
      for (PermissibleObject permissibleObject : list) {
        if (SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
          mostRated.add(permissibleObject);
        }
      }
      return mostRated;
    } catch (Throwable t) {
      Logger.log(t);
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @Path("/bottom/{classType}/{minNumVotes}/{maxResults}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<PermissibleObject> getBottomRated(@PathParam("classType") String classType, @PathParam("minNumVotes") int minNumVotes,
      @PathParam("maxResults") int maxResults, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (classType == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = null;

      try {
        authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      } catch (Throwable t) {
      }
      List<PermissibleObject> mostRated = new ArrayList<PermissibleObject>();
      String simpleClassName = Class.forName(classType).getSimpleName();
      List<PermissibleObject> list = session.createQuery("from " + simpleClassName + " where numRatingVotes >= " + minNumVotes + " order by averageRating asc")
          .setMaxResults(maxResults).setCacheable(true).list();
      for (PermissibleObject permissibleObject : list) {
        if (SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
          mostRated.add(permissibleObject);
        }
      }
      return mostRated;
    } catch (Throwable t) {
      Logger.log(t);
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @Path("/mostLiked/{classType}/{minNumVotes}/{maxResults}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<PermissibleObject> getMostLiked(@PathParam("classType") String classType, @PathParam("minNumVotes") int minNumVotes,
      @PathParam("maxResults") int maxResults, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (classType == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = null;

      try {
        authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      } catch (Throwable t) {
      }
      List<PermissibleObject> mostRated = new ArrayList<PermissibleObject>();
      String simpleClassName = Class.forName(classType).getSimpleName();
      List<PermissibleObject> list = session.createQuery("from " + simpleClassName + " where numUpVotes >= " + minNumVotes + " order by numUpVotes desc")
          .setMaxResults(maxResults).setCacheable(true).list();
      for (PermissibleObject permissibleObject : list) {
        if (SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
          mostRated.add(permissibleObject);
        }
      }
      return mostRated;
    } catch (Throwable t) {
      Logger.log(t);
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @Path("/mostDisliked/{classType}/{minNumVotes}/{maxResults}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<PermissibleObject> getMostDisliked(@PathParam("classType") String classType, @PathParam("minNumVotes") int minNumVotes,
      @PathParam("maxResults") int maxResults, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (classType == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = null;

      try {
        authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      } catch (Throwable t) {
      }
      List<PermissibleObject> mostRated = new ArrayList<PermissibleObject>();
      String simpleClassName = Class.forName(classType).getSimpleName();
      List<PermissibleObject> list = session.createQuery("from " + simpleClassName + " where numDownVotes >= " + minNumVotes + " order by numDownVotes desc")
          .setMaxResults(maxResults).setCacheable(true).list();
      for (PermissibleObject permissibleObject : list) {
        if (SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
          mostRated.add(permissibleObject);
        }
      }
      return mostRated;
    } catch (Throwable t) {
      Logger.log(t);
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @Path("/since/{classType}/{createdSinceMillis}/{maxResults}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<PermissibleObject> getCreatedSince(@PathParam("classType") String classType, @PathParam("createdSinceMillis") long createdSinceMillis,
      @PathParam("maxResults") int maxResults, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (classType == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = null;

      try {
        authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      } catch (Throwable t) {
      }
      List<PermissibleObject> mostRated = new ArrayList<PermissibleObject>();
      String simpleClassName = Class.forName(classType).getSimpleName();
      List<PermissibleObject> list = session
          .createQuery("from " + simpleClassName + " where creationDate >= " + createdSinceMillis + " order by creationDate desc").setMaxResults(maxResults)
          .setCacheable(true).list();
      for (PermissibleObject permissibleObject : list) {
        if (SecurityHelper.doesUserHavePermission(session, authUser, permissibleObject, PERM.READ)) {
          mostRated.add(permissibleObject);
        }
      }
      return mostRated;
    } catch (Throwable t) {
      Logger.log(t);
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

}

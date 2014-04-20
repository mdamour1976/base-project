package org.damour.base.server.hibernate.helpers;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserRating;
import org.damour.base.server.resource.UserResource;
import org.hibernate.Session;

public class RatingHelper {

  public static String getVoterGUID(HttpServletRequest request, HttpServletResponse response) {
    Cookie cookies[] = request.getCookies();
    String voterGUID = UUID.randomUUID().toString();
    boolean hasVoterGUID = false;
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("voterGUID".equals(cookie.getName())) {
          hasVoterGUID = true;
          voterGUID = cookie.getValue();
          break;
        }
      }
    }
    if (!hasVoterGUID) {
      Cookie voterGUIDCookie = new Cookie("voterGUID", voterGUID);
      voterGUIDCookie.setPath("/");
      voterGUIDCookie.setMaxAge(UserResource.COOKIE_TIMEOUT);
      response.addCookie(voterGUIDCookie);
    }
    return voterGUID;
  }
  
  public static UserRating getUserRating(Session session, PermissibleObject permissibleObject, User voter, String voterGUID) {
    if (permissibleObject == null) {
      return null;
    }
    if (voter != null) {
      UserRating rating = (UserRating) session
          .createQuery("from " + UserRating.class.getSimpleName() + " where permissibleObject.id = " + permissibleObject.id + " and voter.id = " + voter.id)
          .setCacheable(true).uniqueResult();
      return rating;
    }
    UserRating rating = (UserRating) session
        .createQuery(
            "from " + UserRating.class.getSimpleName() + " where permissibleObject.id = " + permissibleObject.id + " and voterGUID = '" + voterGUID + "'")
        .setCacheable(true).uniqueResult();
    return rating;
  }

  public static List<UserRating> getUserRatings(Session session, PermissibleObject permissibleObject) {
    List<UserRating> ratings = session.createQuery("from " + UserRating.class.getSimpleName() + " where permissibleObject.id = " + permissibleObject.id)
        .setCacheable(true).list();
    return ratings;
  }

  public static PermissibleObject getNextUnratedPermissibleObject(Session session, String objectType, User voter, String voterGUID) {
    PermissibleObject object = null;
    Class<?> clazz = PermissibleObject.class;
    try {
      clazz = Class.forName(objectType);
    } catch (Throwable t) {
    }
    if (voter != null) {
      String query = "from " + clazz.getSimpleName() + " where id not in (select rating.permissibleObject.id from " + UserRating.class.getSimpleName()
          + " rating where rating.voter.id = " + voter.getId() + ")";
      System.out.println(query);
      object = (PermissibleObject) session.createQuery(query).setCacheable(true).uniqueResult();
    } else {
      // go based on voterIP
      object = (PermissibleObject) session
          .createQuery(
              "from " + clazz.getSimpleName() + " where id not in (select permissibleObject.id from " + UserRating.class.getSimpleName()
                  + " where voterGUID = '" + voterGUID + "')").setCacheable(true).uniqueResult();
    }

    return object;
  }
}

package org.damour.base.server.hibernate.helpers;

import java.util.List;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserAdvisory;
import org.hibernate.Session;

public class AdvisoryHelper {

  public static UserAdvisory getUserAdvisory(Session session, PermissibleObject permissibleObject, User voter, String voterGUID) {
    if (permissibleObject == null) {
      return null;
    }
    if (voter != null) {
      UserAdvisory rating = (UserAdvisory) session
          .createQuery("from " + UserAdvisory.class.getSimpleName() + " where permissibleObject.id = " + permissibleObject.id + " and voter.id = " + voter.id)
          .setCacheable(true).uniqueResult();
      return rating;
    }
    UserAdvisory rating = (UserAdvisory) session
        .createQuery(
            "from " + UserAdvisory.class.getSimpleName() + " where permissibleObject.id = " + permissibleObject.id + " and voterGUID = '" + voterGUID + "'")
        .setCacheable(true).uniqueResult();
    return rating;
  }

  public static List<UserAdvisory> getUserAdvisories(Session session, PermissibleObject permissibleObject) {
    List<UserAdvisory> ratings = session.createQuery("from " + UserAdvisory.class.getSimpleName() + " where permissibleObject.id = " + permissibleObject.id)
        .setCacheable(true).list();
    return ratings;
  }

}

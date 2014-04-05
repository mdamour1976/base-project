package org.damour.base.server.hibernate.helpers;

import org.damour.base.client.objects.User;
import org.hibernate.Session;

public class UserHelper {

  public static User getUser(Session session, String username) {
    User user = (User) session.createQuery("from User where username = '" + username + "'").setCacheable(true).uniqueResult();
    if (user == null) {
      // try email
      user = (User) session.createQuery("from User where email = '" + username + "'").setCacheable(true).uniqueResult();
    }
    return user;
  }

}

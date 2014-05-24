package org.damour.base.server.resource;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.damour.base.client.objects.GroupMembership;
import org.damour.base.client.objects.StringWrapper;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.client.utils.StringUtils;
import org.damour.base.server.BaseSystem;
import org.damour.base.server.Logger;
import org.damour.base.server.MD5;
import org.damour.base.server.hibernate.HibernateUtil;
import org.damour.base.server.hibernate.helpers.SecurityHelper;
import org.damour.base.server.hibernate.helpers.UserHelper;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.andrewtimberlake.captcha.Captcha;

@Path("/user")
public class UserResource {

  public static final int COOKIE_TIMEOUT = 31556926; // 1 year in seconds

  public static void destroyAuthCookies(HttpServletRequest request, HttpServletResponse response) {
    Cookie userCookie = new Cookie("user", "");
    userCookie.setMaxAge(0);
    userCookie.setPath("/");
    Cookie userAuthCookie = new Cookie("auth", "");
    userAuthCookie.setMaxAge(0);
    userAuthCookie.setPath("/");
    response.addCookie(userCookie);
    response.addCookie(userAuthCookie);
  }

  public static void destroyAllCookies(HttpServletRequest request, HttpServletResponse response) {
    for (Cookie cookie : request.getCookies()) {
      cookie.setMaxAge(0);
      cookie.setPath("/");
      response.addCookie(cookie);
    }
  }

  @GET
  @Path("/authenticated-user")
  @Produces(MediaType.APPLICATION_JSON)
  public User getAuthenticatedUser(@Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      return getAuthenticatedUser(session, httpRequest, httpResponse);
    } finally {
      session.close();
    }
  }

  public User getAuthenticatedUser(Session session, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
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
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    String username = userCookie.getValue().toLowerCase();
    User user = UserHelper.getUser(session, username);
    if (user != null && userAuthCookie.getValue().equals(user.getPasswordHash())) {
      return user;
    }
    if (user == null) {
      UserResource.destroyAuthCookies(httpRequest, httpResponse);
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    return user;
  }

  @GET
  @Path("/logout")
  @Produces(MediaType.APPLICATION_JSON)
  public Boolean logout(@Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    destroyAllCookies(httpRequest, httpResponse);
    return true;
  }

  @GET
  @Path("/list")
  @Produces(MediaType.APPLICATION_JSON)
  public List<User> getUsers() {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      return SecurityHelper.getUsers(session);
    } finally {
      session.close();
    }
  }

  @GET
  @Path("{username}")
  @Produces(MediaType.APPLICATION_JSON)
  public User getUser(@PathParam("username") String username) {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      return UserHelper.getUser(session, username);
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/list-names")
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> getUsernames() {
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
  @Path("/login")
  @Produces(MediaType.APPLICATION_JSON)
  public User login(@QueryParam("username") String username, @QueryParam("password") String password, @QueryParam("facebook") Boolean facebook,
      @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      if (facebook) {
        // this is a facebook api call
        // let's check if the accessToken (password) is valid
        // https://graph.facebook.com/username?access_token=password
        URL url = new URL("https://graph.facebook.com/" + username + "?access_token=" + password);

        JSONObject json = (JSONObject) JSONValue.parseStrict(IOUtils.toString(url.openStream()));

        String id = (String) json.get("id");
        String fbusername = (String) json.get("username");
        String email = StringEscapeUtils.unescapeJava((String) json.get("email"));
        String first = (String) json.get("first_name");
        String last = (String) json.get("last_name");

        if (!StringUtils.isEmpty(id)) {
          try {
            User user = UserHelper.getUser(session, email);
            if (user != null) {
              return login(session, httpRequest, httpResponse, user.getUsername(), user.getPasswordHash(), true);
            }
          } catch (Throwable t) {
          }
          // we do not have this user, add them
          User user = new User();
          user.setAdministrator(false);
          user.setBirthday(System.currentTimeMillis());
          user.setEmail(email);
          user.setFacebook(true);
          user.setFirstname(first);
          user.setLastname(last);
          user.setPasswordHash(password);
          user.setPasswordHint("You logged in via Facebook");
          user.setSignupDate(System.currentTimeMillis());
          user.setUsername("fb_" + fbusername);
          user.setValidated(false);
          return createOrEditAccount(user, password, null, httpRequest, httpResponse);
        }
      }
      return login(session, httpRequest, httpResponse, username, password, false);
    } catch (ParseException | IOException e) {
      throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      session.close();
    }
  }

  private boolean isAccountValidated(User user) {
    if (!BaseSystem.requireAccountValidation()) {
      return true;
    }
    if (user == null) {
      return false;
    }
    return user.isValidated();
  }

  private User login(org.hibernate.Session session, HttpServletRequest request, HttpServletResponse response, String username, String password, boolean internal) {
    username = username.toLowerCase();
    User user = UserHelper.getUser(session, username);

    String passwordHash = MD5.md5(password);
    if (user != null && isAccountValidated(user) && ((internal && password.equals(user.getPasswordHash())) || user.getPasswordHash().equals(passwordHash))) {
      Cookie userCookie = new Cookie("user", user.getUsername());
      userCookie.setPath("/");
      userCookie.setMaxAge(COOKIE_TIMEOUT);
      Cookie userAuthCookie = new Cookie("auth", internal ? password : passwordHash);
      userAuthCookie.setPath("/");
      userAuthCookie.setMaxAge(COOKIE_TIMEOUT);
      Cookie voterCookie = new Cookie("voterGUID", UUID.randomUUID().toString());
      voterCookie.setPath("/");
      voterCookie.setMaxAge(COOKIE_TIMEOUT);
      response.addCookie(userCookie);
      response.addCookie(userAuthCookie);
      response.addCookie(voterCookie);
    } else {
      UserResource.destroyAuthCookies(request, response);
      if (user != null && !isAccountValidated(user)) {
        throw new WebApplicationException(Response.Status.FORBIDDEN);
      }
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    return user;
  }

  @POST
  @Path("/account")
  @Produces(MediaType.APPLICATION_JSON)
  public User createOrEditAccount(User inUser, @QueryParam("password") String password, @QueryParam("captchText") String captchaText,
      @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = null;
    Transaction tx = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      tx = session.beginTransaction();
      User possibleAuthUser = getAuthenticatedUser(session, httpRequest, httpResponse);
      User authUser = null;
      if (possibleAuthUser instanceof User) {
        authUser = (User) possibleAuthUser;
      }

      User dbUser = null;
      try {
        dbUser = (User) session.load(User.class, inUser.getId());
      } catch (Exception e) {
      }

      if (dbUser == null) {
        // new account, it did NOT exist
        // validate captcha first
        if (StringUtils.isEmpty(captchaText)) {
          captchaText = "INVALID!";
        }
        Captcha captcha = (Captcha) httpRequest.getSession().getAttribute("captcha");
        if (captcha != null && !captcha.isValid(captchaText)) {
          throw new WebApplicationException(Response.Status.PRECONDITION_FAILED);
        }

        User newUser = new User();
        newUser.setUsername(inUser.getUsername().toLowerCase());
        if (password != null && !"".equals(password)) {
          newUser.setPasswordHash(MD5.md5(password));
        }
        if (authUser != null && authUser.isAdministrator()) {
          newUser.setAdministrator(inUser.isAdministrator());
        }
        newUser.setFirstname(inUser.getFirstname());
        newUser.setLastname(inUser.getLastname());
        newUser.setEmail(inUser.getEmail());
        newUser.setBirthday(inUser.getBirthday());
        newUser.setPasswordHint(inUser.getPasswordHint());
        newUser.setFacebook(inUser.isFacebook());
        newUser.setSignupDate(System.currentTimeMillis());

        newUser.setValidated(!BaseSystem.requireAccountValidation());
        if (authUser != null && authUser.isAdministrator()) {
          // admin can automatically create/validate accounts
          newUser.setValidated(true);
        }

        session.save(newUser);

        UserGroup userGroup = new UserGroup();
        userGroup.setName(newUser.getUsername());
        userGroup.setVisible(true);
        userGroup.setAutoJoin(false);
        userGroup.setLocked(false);
        userGroup.setOwner(newUser);

        session.save(userGroup);

        GroupMembership groupMembership = new GroupMembership();
        groupMembership.setUser(newUser);
        groupMembership.setUserGroup(userGroup);
        session.save(groupMembership);

        tx.commit();

        // if a new user is creating a new account, login if new user account is validated
        if (authUser == null && isAccountValidated(newUser)) {
          UserResource.destroyAuthCookies(httpRequest, httpResponse);
          if (login(session, httpRequest, httpResponse, newUser.getUsername(), newUser.getPasswordHash(), true) != null) {
            return newUser;
          }
        } else if (authUser == null && !isAccountValidated(newUser)) {
          // send user a validation email, where, upon clicking the link, their account will be validated
          // the validation code in the URL will simply be a hash of their email address
          MD5 md5 = new MD5();
          md5.update(newUser.getEmail());
          md5.update(newUser.getPasswordHash());

          String portStr = "";
          if (httpRequest.getLocalPort() != 80) {
            portStr = ":" + httpRequest.getLocalPort();
          }
          String url = httpRequest.getScheme() + "://" + httpRequest.getServerName() + portStr + "/?u=" + newUser.getUsername() + "&v=" + md5.digest();

          String text = "Thank you for signing up with " + BaseSystem.getDomainName()
              + ".<BR><BR>Please confirm your account by clicking the following link:<BR><BR>";
          text += "<A HREF=\"";
          text += url;
          text += "\">" + url + "</A>";
          BaseSystem.getEmailService().sendMessage(BaseSystem.getSmtpHost(), BaseSystem.getAdminEmailAddress(), BaseSystem.getDomainName() + " validator",
              newUser.getEmail(), BaseSystem.getDomainName() + " account validation", text);
        }
        return newUser;
      } else if (authUser != null && (authUser.isAdministrator() || authUser.getId().equals(dbUser.getId()))) {
        // edit an existing account
        // the following conditions must be met to be here:
        // -authentication
        // -we are the administrator
        // -we are editing our own account
        if (password != null && !"".equals(password)) {
          MD5 md5 = new MD5();
          md5.update(password);
          dbUser.setPasswordHash(md5.digest());
        }
        if (authUser.isAdministrator()) {
          dbUser.setAdministrator(inUser.isAdministrator());
        }
        dbUser.setUsername(inUser.getUsername());
        dbUser.setFirstname(inUser.getFirstname());
        dbUser.setLastname(inUser.getLastname());
        dbUser.setEmail(inUser.getEmail());
        dbUser.setBirthday(inUser.getBirthday());
        dbUser.setPasswordHint(inUser.getPasswordHint());

        // only admin can validate directly
        if (authUser.isAdministrator()) {
          dbUser.setValidated(inUser.isValidated());
        }

        session.save(dbUser);
        tx.commit();

        // if we are editing our own account, then re-authenticate
        if (authUser.getId().equals(dbUser.getId())) {
          UserResource.destroyAuthCookies(httpRequest, httpResponse);
          if (login(session, httpRequest, httpResponse, dbUser.getUsername(), dbUser.getPasswordHash(), true) != null) {
            return dbUser;
          }
        }
        return dbUser;
      }
      throw new WebApplicationException(Response.Status.CONFLICT);
    } catch (Exception ex) {
      Logger.log(ex);
      try {
        tx.rollback();
      } catch (Exception exx) {
      }
      throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/hint")
  @Produces(MediaType.APPLICATION_JSON)
  public StringWrapper getLoginHint(@QueryParam("username") String username) {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User user = UserHelper.getUser(session, username.toLowerCase());
      if (user == null) {
        throw new WebApplicationException(Response.Status.NO_CONTENT);
      }
      return new StringWrapper(user.getPasswordHint());
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/validate")
  @Produces(MediaType.APPLICATION_JSON)
  public User submitAccountValidation(@QueryParam("username") String username, @QueryParam("validationCode") String validationCode,
      @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = null;
    Transaction tx = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      tx = session.beginTransaction();
      User user = UserHelper.getUser(session, username);
      if (user != null && !user.isValidated()) {
        MD5 md5 = new MD5();
        md5.update(user.getEmail());
        md5.update(user.getPasswordHash());
        if (validationCode.equals(md5.digest())) {
          // validation successful
          user.setValidated(true);
          login(session, httpRequest, httpResponse, username, user.getPasswordHash(), true);
          tx.commit();
        } else {
          throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
        }
      } else {
        throw new WebApplicationException(Response.Status.NO_CONTENT);
      }
      return user;
    } catch (Throwable t) {
      Logger.log(t);
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      try {
        tx.rollback();
      } catch (Throwable t) {
      }
      session.close();
    }
  }
}

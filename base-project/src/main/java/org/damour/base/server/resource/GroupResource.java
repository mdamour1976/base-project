package org.damour.base.server.resource;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.damour.base.client.objects.GroupMembership;
import org.damour.base.client.objects.PendingGroupMembership;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.server.BaseSystem;
import org.damour.base.server.Logger;
import org.damour.base.server.hibernate.HibernateUtil;
import org.damour.base.server.hibernate.helpers.SecurityHelper;
import org.damour.base.server.hibernate.helpers.UserHelper;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

@Path("/group")
public class GroupResource {
  @PUT
  @Path("/{userId}/{groupId}/add")
  @Produces(MediaType.APPLICATION_JSON)
  public GroupMembership addUserToGroup(@PathParam("userId") Long userId, @PathParam("groupId") Long groupId, @Context HttpServletRequest httpRequest,
      @Context HttpServletResponse httpResponse) {
    Transaction tx = null;
    Session session = null;

    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser == null) {
        throw new WebApplicationException(new Exception("Could not join group, attempt to join with unauthorized client."), Response.Status.UNAUTHORIZED);
      }
      UserGroup group = (UserGroup) session.load(UserGroup.class, groupId);
      User user = (User) session.load(User.class, userId);

      if (group == null || user == null) {
        throw new WebApplicationException(new Exception("Could not join group, user and group not found."), Response.Status.NOT_FOUND);
      }

      // the group owner and an administrator may add users to groups without obeying the 'lock'
      if (group.isLocked() && !authUser.isAdministrator() && !group.getOwner().getId().equals(authUser.getId())) {
        throw new WebApplicationException(new Exception("This group is currently not accepting new members."), Response.Status.FORBIDDEN);
      }

      if (authUser.isAdministrator() || group.isAutoJoin() || group.getOwner().getId().equals(authUser.getId())) {
        tx = session.beginTransaction();
        GroupMembership groupMembership = new GroupMembership();
        groupMembership.setUser(user);
        groupMembership.setUserGroup(group);
        session.save(groupMembership);
        tx.commit();
        return groupMembership;
      } else if (!group.isAutoJoin()) {
        tx = session.beginTransaction();
        PendingGroupMembership groupMembership = new PendingGroupMembership();
        groupMembership.setUser(user);
        groupMembership.setUserGroup(group);
        session.save(groupMembership);
        tx.commit();
        // send email to group owner
        BaseSystem.getEmailService().sendMessage(BaseSystem.getSmtpHost(), BaseSystem.getAdminEmailAddress(), BaseSystem.getAdminEmailAddress(),
            group.getOwner().getEmail(), "Group join request from " + user.getUsername(),
            "[" + BaseSystem.getDomainName() + "] " + user.getUsername() + " has requested permission to join your group " + group.getName());
        throw new WebApplicationException(new Exception("Could not join group, request submitted to group owner."), Response.Status.ACCEPTED);
      }
      throw new WebApplicationException(new Exception("Could not join group."), Response.Status.FORBIDDEN);
    } catch (org.hibernate.exception.ConstraintViolationException e) {
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new WebApplicationException(new Exception("Could not join group, user already a member or add request pending."), Response.Status.FORBIDDEN);
    } finally {
      try {
        session.close();
      } catch (Throwable tt) {
      }
    }
  }

  @PUT
  @Path("/create")
  @Produces(MediaType.APPLICATION_JSON)
  public UserGroup createOrEditGroup(UserGroup group, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Transaction tx = null;
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      tx = session.beginTransaction();

      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser != null && (authUser.isAdministrator() || authUser.getId().equals(group.getOwner().getId()))) {
        try {
          User owner = (User) session.load(User.class, group.getOwner().getId());
          group.setOwner(owner);
        } catch (HibernateException e) {
        }

        if (group.getId() == null) {
          // new group
          // before we save, let's make sure the user doesn't already have a group by this name
          List<UserGroup> existingGroups = SecurityHelper.getOwnedUserGroups(session, group.getOwner());
          for (UserGroup existingGroup : existingGroups) {
            if (existingGroup.getName().equalsIgnoreCase(group.getName())) {
              throw new WebApplicationException(new Exception("A group already exists with this name."), Response.Status.CONFLICT);
            }
          }
          session.save(group);
          // default is to create membership for the owner
          GroupMembership groupMembership = new GroupMembership();
          groupMembership.setUser(group.getOwner());
          groupMembership.setUserGroup(group);
          session.save(groupMembership);
        } else {
          // let's make sure that if we are changing the group name that
          // the only group with this name (for the group owner) is this group
          session.saveOrUpdate(group);
        }

        tx.commit();
        return group;
      }
      return null;
    } catch (Throwable t) {
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      try {
        session.close();
      } catch (Throwable tt) {
      }
    }
  }

  @DELETE
  @Path("/{userId}/{groupId}/delete")
  public void deleteUser(@PathParam("userId") Long userId, @PathParam("groupId") Long groupId, @Context HttpServletRequest httpRequest,
      @Context HttpServletResponse httpResponse) {
    Session session = null;
    Transaction tx = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser == null) {
        throw new WebApplicationException(new Exception("Could not remove user from group, attempt made with unauthorized client."),
            Response.Status.UNAUTHORIZED);
      }
      UserGroup group = (UserGroup) session.load(UserGroup.class, groupId);
      User user = (User) session.load(User.class, userId);

      if (group == null || user == null) {
        throw new WebApplicationException(new Exception("Could not remove user from group, user or group not found."), Response.Status.NOT_FOUND);
      }

      if (authUser.isAdministrator() || group.isAutoJoin() || group.getOwner().getId().equals(authUser.getId())) {
        tx = session.beginTransaction();
        GroupMembership groupMembership = SecurityHelper.getGroupMembership(session, user, group);
        if (groupMembership != null) {
          session.delete(groupMembership);
        }
        tx.commit();
      }

    } catch (Throwable t) {
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      try {
        session.close();
      } catch (Throwable tt) {
      }
    }
  }

  @DELETE
  @Path("{groupId}/delete")
  public void deleteGroup(@PathParam("groupId") Long groupId, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = null;
    Transaction tx = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      UserGroup group = (UserGroup) session.load(UserGroup.class, groupId);
      if (authUser != null && (authUser.isAdministrator() || group.getOwner().getId().equals(authUser.getId()))) {
        tx = session.beginTransaction();
        SecurityHelper.deleteUserGroup(session, group);
        tx.commit();
      } else {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
    } catch (Throwable t) {
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      try {
        session.close();
      } catch (Throwable tt) {
      }
    }
  }

  @GET
  @Path("/{userId}/pending")
  public List<PendingGroupMembership> getPendingGroupMemberships(@PathParam("userId") Long userId, @Context HttpServletRequest httpRequest,
      @Context HttpServletResponse httpResponse) {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      User user = (User) session.load(User.class, userId);

      if (user == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }

      if (authUser.isAdministrator() || user.getId().equals(authUser.getId())) {
        // remember, administrator owns all
        return SecurityHelper.getPendingGroupMemberships(session, user);
      } else {
        throw new WebApplicationException(Response.Status.FORBIDDEN);
      }
    } catch (Throwable t) {
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      try {
        session.close();
      } catch (Throwable tt) {
      }
    }
  }

  @PUT
  @Path("/{userId}/{approve}/approve")
  @Produces(MediaType.APPLICATION_JSON)
  public List<PendingGroupMembership> submitPendingGroupMembershipApproval(@PathParam("userId") Long userId, @PathParam("approve") boolean approve,
      Set<PendingGroupMembership> members, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    if (members == null || members.size() == 0) {
      throw new WebApplicationException(new Exception("List of members provided was empty."), Response.Status.NOT_ACCEPTABLE);
    }

    if (userId == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    Session session = null;
    Transaction tx = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      tx = session.beginTransaction();
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser == null) {
        throw new WebApplicationException(new Exception("Cannot approve or deny requests without authentication."), Response.Status.UNAUTHORIZED);
      }

      // only the authenticated: admin or user themselves
      if (authUser.isAdministrator() || userId.equals(authUser.getId())) {
        for (PendingGroupMembership pendingGroupMembership : members) {
          // if we are the admin or to be sure that the user actually owns the group for this pending request
          if (authUser.isAdministrator() || userId.equals(pendingGroupMembership.getUserGroup().getOwner().getId())) {
            // approve/deny request
            if (approve) {
              GroupMembership realGroupMembership = new GroupMembership();
              realGroupMembership.setUser(pendingGroupMembership.getUser());
              realGroupMembership.setUserGroup(pendingGroupMembership.getUserGroup());
              session.save(realGroupMembership);
            }
            session.delete(pendingGroupMembership);
          }
        }
        tx.commit();
        // send back the new list
        User user = (User) session.load(User.class, userId);

        return SecurityHelper.getPendingGroupMemberships(session, user);
      } else {
        throw new WebApplicationException(new Exception("Cannot approve or deny requests without authentication."), Response.Status.UNAUTHORIZED);
      }
    } catch (Throwable t) {
      Logger.log(t);
      try {
        tx.rollback();
      } catch (Throwable tt) {
      }
      throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @GET
  @Path("/{groupId}/users")
  @Produces(MediaType.APPLICATION_JSON)
  public List<User> getUsers(@PathParam("groupId") Long groupId, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
      if (authUser == null) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
      UserGroup group = (UserGroup) session.load(UserGroup.class, groupId);
      // only the group owner, group members and administrator can see the users in a group
      if (authUser.isAdministrator() || authUser.equals(group.getOwner())) {
        return SecurityHelper.getUsersInUserGroup(session, group);
      }
      // now check the groups for the user against the group
      List<GroupMembership> memberships = SecurityHelper.getGroupMemberships(session, authUser);
      if (memberships.contains(group)) {
        return SecurityHelper.getUsersInUserGroup(session, group);
      }
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/list")
  @Produces(MediaType.APPLICATION_JSON)
  public List<UserGroup> getGroups(@Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
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
  @Path("/{username}/groups")
  @Produces(MediaType.APPLICATION_JSON)
  public List<UserGroup> getGroups(@PathParam("username") String username, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User inUser = UserHelper.getUser(session, username);
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
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
  @Path("/{username}/owned-groups")
  @Produces(MediaType.APPLICATION_JSON)
  public List<UserGroup> getOwnedGroups(@PathParam("username") String username, @Context HttpServletRequest httpRequest,
      @Context HttpServletResponse httpResponse) {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      User authUser = (new UserResource()).getAuthenticatedUser(session, httpRequest, httpResponse);
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
}
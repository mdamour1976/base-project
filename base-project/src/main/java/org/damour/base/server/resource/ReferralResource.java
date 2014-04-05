package org.damour.base.server.resource;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.damour.base.client.objects.Referral;
import org.damour.base.client.utils.StringUtils;
import org.damour.base.server.Logger;
import org.damour.base.server.hibernate.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

@Path("/referral")
public class ReferralResource {

  @PUT
  @Path("/add")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Referral submitReferral(Referral referral, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      if (referral == null) {
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
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
        throw new WebApplicationException(Response.Status.BAD_REQUEST);
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
        throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
      }
    } finally {
      session.close();
    }
  }

  @GET
  @Path("/{id}/list")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Referral> getReferrals(@PathParam("id") Long id, @Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
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
  @Path("/list")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Referral> getReferrals(@Context HttpServletRequest httpRequest, @Context HttpServletResponse httpResponse) {
    Session session = null;
    try {
      session = HibernateUtil.getInstance().getSession();
      return session.createQuery("from " + Referral.class.getSimpleName()).setCacheable(true).list();
    } finally {
      session.close();
    }
  }
}
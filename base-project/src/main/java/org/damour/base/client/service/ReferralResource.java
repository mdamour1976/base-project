package org.damour.base.client.service;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.damour.base.client.objects.Referral;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

@Path("rest/referral")
public interface ReferralResource extends RestService {
  @PUT
  @Path("add")
  void submitReferral(Referral referral, MethodCallback<Referral> callback);

  @GET
  @Path("{id}/list")
  void getReferrals(@PathParam("id") String id, MethodCallback<List<Referral>> callback);

  @GET
  @Path("list")
  void getReferrals(MethodCallback<List<Referral>> callback);
}

package org.damour.base.client.service;

import java.util.Date;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.damour.base.client.objects.AdvertisingInfo;
import org.damour.base.client.objects.CpuStats;
import org.damour.base.client.objects.Email;
import org.damour.base.client.objects.Feedback;
import org.damour.base.client.objects.FileUploadStatus;
import org.damour.base.client.objects.HibernateStat;
import org.damour.base.client.objects.SystemStats;
import org.damour.base.client.objects.StringWrapper;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

@Path("/rest/base")
public interface BaseResource extends RestService {

  @GET
  @Path("hibernate/stats")
  void getHibernateStats(MethodCallback<List<HibernateStat>> callback);

  @POST
  @Path("hibernate/reset")
  void resetHibernate(MethodCallback<List<HibernateStat>> callback);

  @DELETE
  @Path("hibernate/evict/{classname}")
  void evictClassFromCache(@PathParam("classname") String className, MethodCallback<List<HibernateStat>> callback);

  @GET
  @Path("startupDate")
  void getServerStartupDate(MethodCallback<Date> callback);

  @GET
  @Path("/uptime")
  @Produces(MediaType.TEXT_PLAIN)
  void getUptime(MethodCallback<Long> callback);

  @POST
  @Path("hibernate/executeHQL")
  @Produces(MediaType.TEXT_PLAIN)
  void executeHQL(String query, MethodCallback<String> callback);

  @POST
  @Path("hibernate/executeUpdateHQL")
  @Produces(MediaType.TEXT_PLAIN)
  void executeUpdateHQL(String query, MethodCallback<String> callback);

  @GET
  @Path("system/stats")
  void getSystemStats(@QueryParam("from") Long from, @QueryParam("to") Long to, @QueryParam("ago") Long ago, MethodCallback<List<SystemStats>> callback);

  @POST
  @Path("memory/gc")
  void requestGarbageCollection(MethodCallback<SystemStats> callback);

  @GET
  @Path("ping")
  void ping(MethodCallback<Long> callback);

  @POST
  @Path("advertise")
  void submitAdvertisingInfo(AdvertisingInfo info, MethodCallback<Boolean> callback);

  @POST
  @Path("feedback")
  void submitFeedback(Feedback feedback, MethodCallback<Boolean> callback);

  @POST
  @Path("email/{id}")
  void sendEmail(@PathParam("id") Long id, Email email, MethodCallback<Void> callback);

  @GET
  @Path("fileUploadStatus")
  public void getFileUploadStatus(MethodCallback<FileUploadStatus> callback);

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("log/{lines}")
  public void getLog(@PathParam("lines") Long lines, MethodCallback<StringWrapper> callback);

}
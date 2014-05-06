package org.damour.base.client.service;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.damour.base.client.objects.Page;
import org.damour.base.client.objects.PageInfo;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.PermissibleObjectTreeNode;
import org.damour.base.client.objects.PermissibleObjectTreeRequest;
import org.damour.base.client.objects.RepositoryTreeNode;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

@Path("rest/objects")
public interface PermissibleResource extends RestService {

  @GET
  @Path("/{id}")
  void getPermissibleObject(@PathParam("id") Long id, MethodCallback<PermissibleObject> callback);

  @POST
  @Path("/tree")
  void getPermissibleObjectTree(PermissibleObjectTreeRequest request, MethodCallback<PermissibleObjectTreeNode> callback);

  @GET
  @Path("/{id}/children/{objectType}")
  void getPermissibleObjects(@PathParam("id") Long id, @PathParam("objectType") String type, MethodCallback<List<PermissibleObject>> callback);

  @GET
  @Path("/page/{id}/{pageClassType}/{sortField}/{sortDescending}/{pageNumber}/{pageSize}")
  void getPage(@PathParam("id") Long id, @PathParam("pageClassType") String pageClassType, @PathParam("sortField") String sortField,
      @PathParam("sortDescending") boolean sortDescending, @PathParam("pageNumber") int pageNumber, @PathParam("pageSize") int pageSize,
      MethodCallback<Page<PermissibleObject>> callback);

  @GET
  @Path("/page/{id}/{pageClassType}/{pageSize}")
  void getPageInfo(@PathParam("id") Long id, @PathParam("pageClassType") String pageClassType, @PathParam("pageSize") int pageSize,
      MethodCallback<PageInfo> callback);

  // file/content/permissions methods
  @GET
  @Path("/filetree")
  void getRepositoryTree(MethodCallback<RepositoryTreeNode> callback);

  @POST
  @Path("/echo")
  void echoPermissibleObject(PermissibleObject permissibleObject, MethodCallback<PermissibleObject> callback);
  
  @GET
  @Path("/counter/{id}")
  void getCustomCounter1(@PathParam("id") Long id, MethodCallback<Long> callback);

  @POST
  @Path("/counter/{id}")
  void incrementCustomCounter1(@PathParam("id") Long id, MethodCallback<Long> callback);
  
}
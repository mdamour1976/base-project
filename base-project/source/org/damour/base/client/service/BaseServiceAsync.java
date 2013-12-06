package org.damour.base.client.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.damour.base.client.exceptions.SimpleMessageException;
import org.damour.base.client.objects.Comment;
import org.damour.base.client.objects.File;
import org.damour.base.client.objects.FileUploadStatus;
import org.damour.base.client.objects.Folder;
import org.damour.base.client.objects.GroupMembership;
import org.damour.base.client.objects.HibernateStat;
import org.damour.base.client.objects.MemoryStats;
import org.damour.base.client.objects.Page;
import org.damour.base.client.objects.PageInfo;
import org.damour.base.client.objects.PendingGroupMembership;
import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.objects.PermissibleObjectTreeNode;
import org.damour.base.client.objects.Permission;
import org.damour.base.client.objects.Referral;
import org.damour.base.client.objects.RepositoryTreeNode;
import org.damour.base.client.objects.Tag;
import org.damour.base.client.objects.TagMembership;
import org.damour.base.client.objects.User;
import org.damour.base.client.objects.UserAdvisory;
import org.damour.base.client.objects.UserGroup;
import org.damour.base.client.objects.UserRating;
import org.damour.base.client.objects.UserThumb;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface BaseServiceAsync {
  
  public void getAuthenticatedUser(AsyncCallback<User> callback);
  public void createOrEditAccount(User user, String password, String captchaText, AsyncCallback<User> callback);
  public void login(String username, String password, boolean facebook, AsyncCallback<User> callback);
  public void logout(AsyncCallback<Void> callback);
  public void getLoginHint(String username, AsyncCallback<String> callback);
  public void submitAccountValidation(String username, String validationCode, AsyncCallback<User> callback);
  
  // hibernate admin methods
  public void getHibernateStats(AsyncCallback<List<HibernateStat>> callback);
  public void resetHibernate(AsyncCallback<List<HibernateStat>> callback);  
  public void evictClassFromCache(String className, AsyncCallback<List<HibernateStat>> callback);
  public void getMemoryStats(AsyncCallback<MemoryStats> callback);
  public void requestGarbageCollection(AsyncCallback<MemoryStats> callback);
  public void getServerStartupDate(AsyncCallback<Date> callback);
  public void ping(AsyncCallback<Void> callback);
  public void executeHQL(String query, boolean executeUpdate, AsyncCallback<String> callback);
  
  public void getUsernames(AsyncCallback<List<String>> callback);
  // users/group admin methods
  public void getUsers(AsyncCallback<List<User>> callback);
  public void getUsers(UserGroup group, AsyncCallback<List<User>> callback);
  public void getGroups(AsyncCallback<List<UserGroup>> callback);
  public void getGroups(User user, AsyncCallback<List<UserGroup>> callback);
  public void getOwnedGroups(User user, AsyncCallback<List<UserGroup>> callback);
  public void addUserToGroup(User user, UserGroup group, AsyncCallback<GroupMembership> callback);
  public void createOrEditGroup(UserGroup group, AsyncCallback<UserGroup> callback);
  public void deleteUser(User user, UserGroup group, AsyncCallback<Void> callback);
  public void deleteGroup(UserGroup group, AsyncCallback<Void> callback);  
  public void getPendingGroupMemberships(User user, AsyncCallback<List<PendingGroupMembership>> callback);
  public void submitPendingGroupMembershipApproval(User user, Set<PendingGroupMembership> memberships, boolean approve, AsyncCallback<List<PendingGroupMembership>> callback);

  // file/content/permissions methods
  public void getPermissibleObject(Long id, AsyncCallback<PermissibleObject> callback);
  public void getRepositoryTree(AsyncCallback<RepositoryTreeNode> callback);
  public void getPermissibleObjectTree(PermissibleObject parent, User owner, List<String> acceptedClasses, int fetchDepth, int metaDataFetchDepth, AsyncCallback<PermissibleObjectTreeNode> callback);
  public void savePermissibleObject(PermissibleObject permissibleObject, AsyncCallback<PermissibleObject> callback);
  public void savePermissibleObjects(List<PermissibleObject> permissibleObjects, AsyncCallback<List<PermissibleObject>> callback);
  public void deletePermissibleObject(PermissibleObject permissibleObject, AsyncCallback<Void> callback);
  public void deletePermissibleObjects(Set<PermissibleObject> permissibleObjects, AsyncCallback<Void> callback);
  public void deleteAndSavePermissibleObjects(Set<PermissibleObject> toBeDeleted, Set<PermissibleObject> toBeSaved, AsyncCallback<Void> callback);
  public void getPermissibleObjects(PermissibleObject parent, String objectType, AsyncCallback<List<PermissibleObject>> callback);
  public void getMyPermissibleObjects(PermissibleObject parent, String objectType, AsyncCallback<List<PermissibleObject>> callback);
  public void createNewFolder(Folder newFolder, AsyncCallback<Folder> callback);
  public void renameFile(File file, AsyncCallback<Void> callback);
  public void renameFolder(Folder folder, AsyncCallback<Void> callback);
  public void getPermissions(PermissibleObject permissibleObject, AsyncCallback<List<Permission>> callback);
  public void setPermissions(PermissibleObject permissibleObject, List<Permission> permissions, AsyncCallback<Void> callback);
  public void updatePermissibleObject(PermissibleObject permissibleObject, AsyncCallback<PermissibleObject> callback);
  public void updatePermissibleObjects(List<PermissibleObject> permissibleObjects, AsyncCallback<List<PermissibleObject>> callback);
  public void getFileUploadStatus(AsyncCallback<FileUploadStatus> callback);
  public void searchPermissibleObjects(PermissibleObject parent, String query, String sortField, boolean sortDescending, String searchObjectType, boolean searchNames, boolean searchDescriptions, boolean searchKeywords, boolean useExactPhrase, AsyncCallback<List<PermissibleObjectTreeNode>> callback);
  public void getCustomCounter1(PermissibleObject permissibleObject, AsyncCallback<Long> callback);
  public void incrementCustomCounter1(PermissibleObject permissibleObject, AsyncCallback<Long> callback);
  // for debug purposes: simply return what was given, proving the serialization of the desired object
  public void echoPermissibleObject(PermissibleObject permissibleObject, AsyncCallback<PermissibleObject> callback);

  // referral/tracking api
  public void submitReferral(Referral referral, AsyncCallback<Referral> callback);
  public void getReferrals(PermissibleObject subject, AsyncCallback<List<Referral>> callback);
  
  // page api
  public void getPage(PermissibleObject parent, String pageClassType, String sortField, boolean sortDescending, int pageNumber, int pageSize, AsyncCallback<Page<PermissibleObject>> callback);
  public void getPageInfo(PermissibleObject parent, String pageClassType, int pageSize, AsyncCallback<PageInfo> callback);
  
  // tag methods
  public void getTags(AsyncCallback<List<Tag>> callback);
  public void getTags(PermissibleObject permissibleObject, AsyncCallback<List<Tag>> callback);
  public void getTaggedPermissibleObjects(Tag tag, AsyncCallback<List<PermissibleObject>> callback);
  public void createTag(String tagName, String tagDescription, Tag parentTag, AsyncCallback<Void> callback);
  public void deleteTag(Tag tag, AsyncCallback<Void> callback);
  public void removeFromTag(Tag tag, PermissibleObject permissibleObject, AsyncCallback<Void> callback);
  public void removeTagMembership(TagMembership tagMembership, AsyncCallback<Void> callback);
  public void addToTag(Tag tag, PermissibleObject permissibleObject, AsyncCallback<Void> callback);
  public void addToTag(TagMembership tagMembership, AsyncCallback<Void> callback);
  
  // content rating, advisory and thumbs
  public void setUserRating(PermissibleObject permissibleObject, int rating, AsyncCallback<UserRating> callback);
  public void getUserRating(PermissibleObject permissibleObject, AsyncCallback<UserRating> callback);
  public void getNextUnratedPermissibleObject(String objectType, AsyncCallback<PermissibleObject> callback);
  public void setUserAdvisory(PermissibleObject permissibleObject, int advisory, AsyncCallback<UserAdvisory> callback);
  public void getUserAdvisory(PermissibleObject permissibleObject, AsyncCallback<UserAdvisory> callback);
  public void setUserThumb(PermissibleObject permissibleObject, boolean like, AsyncCallback<UserThumb> callback);
  public void getUserThumb(PermissibleObject permissibleObject, AsyncCallback<UserThumb> callback);
  
  // top rated/most liked api
  public void getMostRated(int maxResults, String classType, AsyncCallback<List<PermissibleObject>> callback);
  public void getTopRated(int maxResults, int minNumVotes, String classType, AsyncCallback<List<PermissibleObject>> callback);
  public void getBottomRated(int maxResults, int minNumVotes, String classType, AsyncCallback<List<PermissibleObject>> callback);
  public void getMostLiked(int maxResults, int minNumVotes, String classType, AsyncCallback<List<PermissibleObject>> callback);
  public void getMostDisliked(int maxResults, int minNumVotes, String classType, AsyncCallback<List<PermissibleObject>> callback);
  public void getCreatedSince(int maxResults, long createdSinceMillis, String classType, AsyncCallback<List<PermissibleObject>> callback);
  
  // file comments
  public void submitComment(Comment comment, AsyncCallback<Boolean> callback);
  public void approveComment(Comment comment, AsyncCallback<Boolean> callback);
  public void deleteComment(Comment comment, AsyncCallback<Boolean> callback);

  // advertising/feedback rpc
  public void submitAdvertisingInfo(String contactName, String email, String company, String phone, String comments, AsyncCallback<Boolean> callback);
  public void submitFeedback(String contactName, String email, String phone, String comments, AsyncCallback<Boolean> callback);
  public void sendEmail(PermissibleObject permissibleObject, String subject, String message, String fromAddress, String fromName, String toAddresses, AsyncCallback<Void> callback) throws SimpleMessageException;
  
  
}

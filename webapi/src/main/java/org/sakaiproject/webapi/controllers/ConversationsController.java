/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.conversations.api.ConversationsPermissionsException;
import org.sakaiproject.conversations.api.Permissions;
import org.sakaiproject.conversations.api.PostSort;
import org.sakaiproject.conversations.api.Reaction;
import org.sakaiproject.conversations.api.beans.CommentTransferBean;
import org.sakaiproject.conversations.api.beans.PostTransferBean;
import org.sakaiproject.conversations.api.beans.TopicTransferBean;
import org.sakaiproject.conversations.api.model.ConvStatus;
import org.sakaiproject.conversations.api.model.Settings;
import org.sakaiproject.conversations.api.model.Tag;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.grading.api.GradingAuthz;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.webapi.beans.ConversationsRestBean;
import org.sakaiproject.webapi.beans.SimpleGroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ConversationsController extends AbstractSakaiApiController {

	@Autowired
	private ConversationsService conversationsService;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private SecurityService securityService;

	@Autowired
	@Qualifier("org.sakaiproject.component.api.ServerConfigurationService")
	private ServerConfigurationService serverConfigurationService;

	@Autowired
	private UserDirectoryService userDirectoryService;

	@Autowired
	private SearchService searchService;

	@GetMapping(value = "/sites/{siteId}/conversations", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityModel<ConversationsRestBean> getSiteConversations(@PathVariable String siteId) throws ConversationsPermissionsException, IdUnusedException {

		String currentUserId = checkSakaiSession().getUserId();

        Site site = siteService.getSite(siteId);
        String siteRef = siteService.siteReference(siteId);
        ConversationsRestBean bean = new ConversationsRestBean();
        bean.userId = currentUserId;
        bean.siteId = siteId;
        bean.canUpdatePermissions = securityService.unlock(SiteService.SECURE_UPDATE_SITE, siteRef);
        bean.isInstructor = securityService.unlock(Permissions.ROLETYPE_INSTRUCTOR.label, siteRef);

        if (bean.canUpdatePermissions || bean.isInstructor) {
            bean.groups = site.getGroups().stream().map(SimpleGroup::new).collect(Collectors.toList());
        } else {
            bean.groups = site.getGroupsWithMember(currentUserId).stream().map(SimpleGroup::new).collect(Collectors.toList());
        }

        bean.topics = conversationsService.getTopicsForSite(siteId).stream()
            .map(tb -> entityModelForTopicBean(tb)).collect(Collectors.toList());
        Settings settings = conversationsService.getSettingsForSite(siteId);

        if (!settings.getSiteLocked()
            || securityService.unlock(Permissions.MODERATE.label, siteRef)) {
            bean.canEditTags = securityService.unlock(Permissions.TAG_CREATE.label, siteRef);
            bean.canCreateDiscussion = securityService.unlock(Permissions.DISCUSSION_CREATE.label, siteRef);
            bean.canCreateQuestion = securityService.unlock(Permissions.QUESTION_CREATE.label, siteRef);
            bean.canCreateTopic = bean.canCreateDiscussion || bean.canCreateQuestion;
        }
        bean.canViewStatistics = securityService.unlock(Permissions.VIEW_STATISTICS.label, siteRef);
        bean.canPin = settings.getAllowPinning() && securityService.unlock(Permissions.TOPIC_PIN.label, siteRef);
        bean.canViewAnonymous = securityService.unlock(Permissions.VIEW_ANONYMOUS.label, siteRef);
        bean.canGrade = securityService.unlock(GradingAuthz.PERMISSION_GRADE_ALL, siteRef);
        bean.maxThreadDepth = serverConfigurationService.getInt(ConversationsService.PROP_MAX_THREAD_DEPTH, 5);
        bean.settings = settings;

        ConvStatus convStatus = conversationsService.getConvStatusForSiteAndUser(siteId, currentUserId);
        bean.showGuidelines = settings.getRequireGuidelinesAgreement() && !convStatus.getGuidelinesAgreed();
        bean.tags = conversationsService.getTagsForSite(siteId);

        bean.disableDiscussions = serverConfigurationService.getBoolean(ConversationsService.PROP_DISABLE_DISCUSSIONS, false);

        bean.blankTopic = conversationsService.getBlankTopic(siteId);

        bean.searchEnabled = searchService.isEnabledForSite(siteId);

        List<Link> links = new ArrayList<>();
        if (bean.canViewStatistics) links.add(Link.of("/api/sites/" + siteId + "/conversations/stats", "stats"));
        return EntityModel.of(bean, links);
    }

	@PostMapping(value = "/sites/{siteId}/conversations/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getSiteStats(@PathVariable String siteId, @RequestBody Map<String, Object> options) throws ConversationsPermissionsException {

		checkSakaiSession();

        String interval = (String) options.get("interval");
        Instant from = interval.equals("WEEK") ? Instant.now().minus(7, ChronoUnit.DAYS) : null;
        Instant to = interval.equals("THIS_WEEK") ? Instant.now(): null;

        return conversationsService.getSiteStats(siteId, from, to, (Integer) options.get("page"), (String) options.get("sort"));
    }

	@PostMapping(value = "/sites/{siteId}/topics", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityModel createTopic(@PathVariable String siteId, @RequestBody TopicTransferBean topicBean) throws ConversationsPermissionsException {

		checkSakaiSession();

        topicBean.siteId = siteId;

        return entityModelForTopicBean(conversationsService.saveTopic(topicBean, true));
    }

	@PutMapping(value = "/sites/{siteId}/topics/{topicId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityModel updateTopic(@PathVariable String siteId, @PathVariable String topicId, @RequestBody TopicTransferBean topicBean) throws ConversationsPermissionsException {

		checkSakaiSession();

        topicBean.id = topicId;
        topicBean.siteId = siteId;
        return entityModelForTopicBean(conversationsService.saveTopic(topicBean, true));
    }

	@DeleteMapping(value = "/sites/{siteId}/topics/{topicId}")
    public ResponseEntity deleteTopic(@PathVariable String topicId) throws ConversationsPermissionsException, UserNotDefinedException {

		checkSakaiSession();
        conversationsService.deleteTopic(topicId);
        return new ResponseEntity(HttpStatus.OK);
	}

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/pinned")
    public ResponseEntity pinTopic(@PathVariable String siteId, @PathVariable String topicId, @RequestBody Boolean pinned) throws ConversationsPermissionsException {

		checkSakaiSession();

        conversationsService.pinTopic(topicId, pinned);
        return new ResponseEntity(HttpStatus.OK);
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/bookmarked")
    public ResponseEntity bookmarkTopic(@PathVariable String siteId, @PathVariable String topicId, @RequestBody Boolean bookmarked) throws ConversationsPermissionsException {

		checkSakaiSession();

        conversationsService.bookmarkTopic(topicId, bookmarked);
        return new ResponseEntity(HttpStatus.OK);
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/hidden")
    public ResponseEntity hideTopic(@PathVariable String siteId, @PathVariable String topicId, @RequestBody Boolean hidden) throws ConversationsPermissionsException {

		checkSakaiSession();

        conversationsService.hideTopic(topicId, hidden, true);
        return new ResponseEntity(HttpStatus.OK);
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/locked", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityModel lockTopic(@PathVariable String siteId, @PathVariable String topicId, @RequestBody Boolean locked) throws ConversationsPermissionsException {

		checkSakaiSession();

        return entityModelForTopicBean(conversationsService.lockTopic(topicId, locked, true));
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/reactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<Reaction, Integer> postTopicReactions(@PathVariable String topicId, @RequestBody Map<Reaction, Boolean> reactions) throws ConversationsPermissionsException {

		checkSakaiSession();

        return conversationsService.saveTopicReactions(topicId, reactions);
    }

	@GetMapping(value = "/sites/{siteId}/topics/{topicId}/upvote")
    public ResponseEntity upvoteTopic(@PathVariable String siteId, @PathVariable String topicId) throws ConversationsPermissionsException {

		checkSakaiSession();
        conversationsService.upvoteTopic(siteId, topicId);
        return new ResponseEntity(HttpStatus.OK);
    }

	@GetMapping(value = "/sites/{siteId}/topics/{topicId}/unupvote")
    public ResponseEntity unUpvoteTopic(@PathVariable String siteId, @PathVariable String topicId) throws ConversationsPermissionsException {

		checkSakaiSession();
        conversationsService.unUpvoteTopic(siteId, topicId);
        return new ResponseEntity(HttpStatus.OK);
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/posts/markpostsviewed")
    public ResponseEntity markPostsViewed(@PathVariable String topicId, @RequestBody Set<String> postIds) throws ConversationsPermissionsException {

		checkSakaiSession();

        conversationsService.markPostsViewed(postIds, topicId);
        return new ResponseEntity(HttpStatus.OK);
    }

    private EntityModel entityModelForTopicBean(TopicTransferBean topicBean) {

        List<Link> links = new ArrayList<>();
        links.add(Link.of(topicBean.url, "self"));
        links.add(Link.of(topicBean.url + "/bookmarked", "bookmark"));
        links.add(Link.of(topicBean.url + "/posts/markpostsviewed", "markpostsviewed"));
        links.add(Link.of(topicBean.url + "/posts", "posts"));
        if (topicBean.canPin) links.add(Link.of(topicBean.url + "/pinned", "pin"));
        if (topicBean.canPost) links.add(Link.of(topicBean.url + "/posts", "post"));
        if (topicBean.canDelete) links.add(Link.of(topicBean.url, "delete"));
        if (topicBean.canReact) links.add(Link.of(topicBean.url + "/reactions", "react"));
        if (topicBean.canModerate) links.add(Link.of(topicBean.url + "/locked", "lock"));
        if (topicBean.canModerate) links.add(Link.of(topicBean.url + "/hidden", "hide"));
        if (topicBean.canUpvote) links.add(Link.of(topicBean.url + "/upvote", "upvote"));
        if (topicBean.canUpvote) links.add(Link.of(topicBean.url + "/unupvote", "unupvote"));
        return EntityModel.of(topicBean, links);
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityModel<PostTransferBean> createPost(@PathVariable String siteId, @PathVariable String topicId, @RequestBody PostTransferBean postBean) throws ConversationsPermissionsException {

        checkSakaiSession();
        postBean.siteId = siteId;
        postBean.topic = topicId;
        return entityModelForPostBean(conversationsService.savePost(postBean, true));
    }

	@GetMapping(value = "/sites/{siteId}/topics/{topicId}/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EntityModel<PostTransferBean>> getTopicPosts(
            @PathVariable String siteId,
            @PathVariable String topicId,
            @RequestParam Integer page,
            @RequestParam(required = false) PostSort sort,
            @RequestParam(required = false) String postId) throws ConversationsPermissionsException {

        checkSakaiSession();
        return conversationsService.getPostsByTopicId(siteId, topicId, page, sort, postId).stream()
            .map(pb -> entityModelForPostBean(pb)).collect(Collectors.toList());
    }

	@PutMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityModel<PostTransferBean> updatePost(@PathVariable String siteId, @PathVariable String topicId, @PathVariable String postId, @RequestBody PostTransferBean postBean) throws ConversationsPermissionsException {

		checkSakaiSession();

        postBean.siteId = siteId;
        postBean.id = postId;
        return entityModelForPostBean(conversationsService.savePost(postBean, true));
    }

	@DeleteMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deletePost(@PathVariable String siteId, @PathVariable String topicId, @PathVariable String postId) throws ConversationsPermissionsException {

		checkSakaiSession();

        conversationsService.deletePost(siteId, topicId, postId, true);
        return ResponseEntity.ok().build();
    }

	@GetMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}/upvote")
    public ResponseEntity upvotePost(@PathVariable String siteId, @PathVariable String topicId, @PathVariable String postId) throws ConversationsPermissionsException {

		checkSakaiSession();
        conversationsService.upvotePost(siteId, topicId, postId);
        return new ResponseEntity(HttpStatus.OK);
    }

	@GetMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}/unupvote")
    public ResponseEntity unUpvotePost(@PathVariable String siteId, @PathVariable String postId) throws ConversationsPermissionsException {

		checkSakaiSession();
        conversationsService.unUpvotePost(siteId, postId);
        return new ResponseEntity(HttpStatus.OK);
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}/reactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<Reaction, Integer> postPostReactions(@PathVariable String topicId, @PathVariable String postId, @RequestBody Map<Reaction, Boolean> reactions) throws ConversationsPermissionsException {

		checkSakaiSession();

        return conversationsService.savePostReactions(topicId, postId, reactions);
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}/locked", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityModel<PostTransferBean> lockPost(@PathVariable String siteId, @PathVariable String topicId, @PathVariable String postId, @RequestBody Boolean locked) throws ConversationsPermissionsException {

		checkSakaiSession();

        return entityModelForPostBean(conversationsService.lockPost(siteId, topicId, postId, locked));
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}/hidden")
    public ResponseEntity hidePost(@PathVariable String siteId, @PathVariable String topicId, @PathVariable String postId, @RequestBody Boolean hidden) throws ConversationsPermissionsException {

		checkSakaiSession();

        conversationsService.hidePost(siteId, topicId, postId, hidden);
        return ResponseEntity.ok().build();
    }

    private EntityModel<PostTransferBean> entityModelForPostBean(PostTransferBean postBean) {

        List<Link> links = new ArrayList<>();
        links.add(Link.of(postBean.url, "self"));
        links.add(Link.of("/api/sites/" + postBean.siteId + "/topics/" + postBean.topic + "/posts", "reply"));
        if (postBean.canDelete) links.add(Link.of(postBean.url, "delete"));
        if (postBean.canDelete) links.add(Link.of(postBean.url + "/restore", "restore"));
        if (postBean.canReact) links.add(Link.of(postBean.url + "/reactions", "react"));
        if (postBean.canReact) links.add(Link.of(postBean.url + "/upvote", "upvote"));
        if (postBean.canReact) links.add(Link.of(postBean.url + "/unupvote", "unupvote"));
        if (postBean.canModerate) links.add(Link.of(postBean.url + "/locked", "lock"));
        if (postBean.canModerate) links.add(Link.of(postBean.url + "/hidden", "hide"));
        recursivelyAddEntityModelForDescendants(postBean);
        return EntityModel.of(postBean, links);
    }

    private void recursivelyAddEntityModelForDescendants(PostTransferBean postBean) {

        ListIterator childIterator = postBean.posts.listIterator();

        while (childIterator.hasNext()) {
            try {
                PostTransferBean child = (PostTransferBean) childIterator.next();
                childIterator.set(entityModelForPostBean(child));
            } catch (ClassCastException cce) {
            }
        }
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommentTransferBean createComment(@PathVariable String siteId, @PathVariable String topicId, @PathVariable String postId, @RequestBody CommentTransferBean commentBean) throws ConversationsPermissionsException  {

		checkSakaiSession();
        commentBean.postId = postId;
        commentBean.siteId = siteId;
        return conversationsService.saveComment(commentBean);
    }

	@PutMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}/comments/{commentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommentTransferBean updateComment(@PathVariable String siteId, @PathVariable String topicId, @PathVariable String postId, @PathVariable String commentId, @RequestBody CommentTransferBean commentBean) throws ConversationsPermissionsException  {

		checkSakaiSession();

        commentBean.id = commentId;
        commentBean.postId = postId;
        commentBean.siteId = siteId;
        return conversationsService.saveComment(commentBean);
    }

	@DeleteMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}/comments/{commentId}")
    public ResponseEntity deleteComment(@PathVariable String siteId, @PathVariable String commentId) throws ConversationsPermissionsException  {

		checkSakaiSession();

        conversationsService.deleteComment(siteId, commentId);
        return new ResponseEntity(HttpStatus.OK);
    }

	@PostMapping(value = "/sites/{siteId}/conversations/tags", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Tag> createTags(@PathVariable String siteId, @RequestBody List<Tag> tags) throws ConversationsPermissionsException {

		checkSakaiSession();
        return conversationsService.createTags(tags);
    }

	@GetMapping(value = "/sites/{siteId}/conversations/tags", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Tag> getTagsForSite(@PathVariable String siteId) throws ConversationsPermissionsException {

		checkSakaiSession();
        return conversationsService.getTagsForSite(siteId);
    }

	@PutMapping(value = "/sites/{siteId}/conversations/tags/{tagId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Tag> updateTag(@PathVariable String siteId, @PathVariable Long tagId, @RequestBody Tag tag) throws ConversationsPermissionsException {

		checkSakaiSession();

        tag.setId(tagId);
        return ResponseEntity.ok(conversationsService.saveTag(tag));
    }

	@DeleteMapping(value = "/sites/{siteId}/conversations/tags/{tagId}")
    public ResponseEntity deleteTag(@PathVariable Long tagId) throws ConversationsPermissionsException  {

		checkSakaiSession();

        conversationsService.deleteTag(tagId);
        return new ResponseEntity(HttpStatus.OK);
    }

	@PostMapping(value = "/sites/{siteId}/conversations/settings/guidelines")
    public ResponseEntity saveSetting(@PathVariable String siteId, @RequestBody String guidelines) throws ConversationsPermissionsException {

		checkSakaiSession();

        Settings settings = conversationsService.getSettingsForSite(siteId);
        settings.setGuidelines(guidelines);
        conversationsService.saveSettings(settings);

        return new ResponseEntity(HttpStatus.OK);
    }

	@PostMapping(value = "/sites/{siteId}/conversations/settings/{setting}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity saveSetting(@PathVariable String siteId, @PathVariable String setting, @RequestBody Boolean on) throws ConversationsPermissionsException {

		checkSakaiSession();

        Settings settings = conversationsService.getSettingsForSite(siteId);

        switch (setting) {
            case "allowPinning":
                settings.setAllowPinning(on);
                break;
            case "allowUpvoting":
                settings.setAllowUpvoting(on);
                break;
            case "allowAnonPosting":
                settings.setAllowAnonPosting(on);
                break;
            case "allowReactions":
                settings.setAllowReactions(on);
                break;
            case "allowBookmarking":
                settings.setAllowBookmarking(on);
                break;
            case "requireGuidelinesAgreement":
                settings.setRequireGuidelinesAgreement(on);
                break;
            case "siteLocked":
                settings.setSiteLocked(on);
                break;
            default:
        }

        conversationsService.saveSettings(settings);

        return new ResponseEntity(HttpStatus.OK);
    }

	@GetMapping(value = "/sites/{siteId}/conversations/agree")
    public ResponseEntity agreeToGuidelines(@PathVariable String siteId) throws ConversationsPermissionsException {

		String currentUserId = checkSakaiSession().getUserId();
        ConvStatus convStatus = conversationsService.getConvStatusForSiteAndUser(siteId, currentUserId);
        convStatus.setGuidelinesAgreed(true);
        conversationsService.saveConvStatus(convStatus);
        return new ResponseEntity(HttpStatus.OK);
    }

	@PostMapping(value = "/sites/{siteId}/conversations/cache/clear")
    public ResponseEntity clearCacheForTopicsGradedByItem(@PathVariable String siteId, @RequestBody Map<String, String> body) {

		String currentUserId = checkSakaiSession().getUserId();

        String siteRef = siteService.siteReference(siteId);
        if (!securityService.unlock(Permissions.GRADE.label, siteRef)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        String id = body.get("gradingItemId");
        if (StringUtils.isBlank(id)) {
            return ResponseEntity.badRequest().build();
        }

        Long gradingItemId = Long.parseLong(id);

        conversationsService.clearCacheForGradedTopic(gradingItemId);

        return ResponseEntity.ok().build();
    }
}

package org.sakaiproject.contentreview.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.dao.ContentReviewItem;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.sakaiproject.contentreview.exception.TransientSubmissionException;
import org.sakaiproject.site.api.Site;

@Slf4j
public class NoOpContentReviewService implements ContentReviewService {
	private static final String SERVICE_NAME = "NOOP";

	@Override
	public void queueContent(String userId, String siteId, String taskId, List<ContentResource> content, String submissionId, boolean resubmission)
			throws QueueException {
		log.debug("Service {} method queueContent({}, {}, {}, {}, {} {})",
				SERVICE_NAME, userId, siteId, taskId, content, submissionId, resubmission);
	}

	@Override
	public int getReviewScore(String contentId, String taskId, String userId)
			throws QueueException, ReportException, Exception {
		log.debug("Service {} method getReviewScore({}, {}, {})", SERVICE_NAME, contentId, taskId, userId);
		return 0;
	}

	@Override
	public String getReviewReport(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		log.debug("Service {} method getReviewReport({}, {}, {})", SERVICE_NAME, contentId, assignmentRef, userId);
		return null;
	}

	@Override
	public String getReviewReportStudent(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		log.debug("Service {} method getReviewReportStudent({}, {}, {})", SERVICE_NAME, contentId, assignmentRef, userId);
		return null;
	}

	@Override
	public String getReviewReportInstructor(String contentId, String assignmentRef, String userId)
			throws QueueException, ReportException {
		log.debug("Service {} method getReviewReportInstructor({}, {}, {})", SERVICE_NAME, contentId, assignmentRef, userId);
		return null;
	}

	@Override
	public Long getReviewStatus(String contentId) throws QueueException {
		log.debug("Service {} method getReviewStatus({})", SERVICE_NAME, contentId);
		return null;
	}

	@Override
	public Date getDateQueued(String contextId) throws QueueException {
		log.debug("Service {} method getDateQueued({})", SERVICE_NAME, contextId);
		return null;
	}

	@Override
	public Date getDateSubmitted(String contextId) throws QueueException, SubmissionException {
		log.debug("Service {} method getDateSubmitted({})", SERVICE_NAME, contextId);
		return null;
	}

	@Override
	public void processQueue() {
		log.debug("Service {} method processQueue()", SERVICE_NAME);
	}

	@Override
	public void checkForReports() {
		log.debug("Service {} method checkForReports()", SERVICE_NAME);
	}

	@Override
	public List<ContentReviewItem> getReportList(String siteId, String taskId)
			throws QueueException, SubmissionException, ReportException {
		log.debug("Service {} method getReportList({}, {})", SERVICE_NAME, siteId, taskId);
		return null;
	}

	@Override
	public List<ContentReviewItem> getReportList(String siteId)
			throws QueueException, SubmissionException, ReportException {
		log.debug("Service {} method getReportList({})", SERVICE_NAME, siteId);
		return null;
	}

	@Override
	public List<ContentReviewItem> getAllContentReviewItems(String siteId, String taskId)
			throws QueueException, SubmissionException, ReportException {
		log.debug("Service {} method getAllContentReviewItems({}, {})", SERVICE_NAME, siteId, taskId);
		return null;
	}

	@Override
	public String getServiceName() {
		log.debug("Service {} method getServiceName()", SERVICE_NAME);
		return SERVICE_NAME;
	}

	@Override
	public void resetUserDetailsLockedItems(String userId) {
		log.debug("Service {} method resetUserDetailsLockedItems({})", SERVICE_NAME, userId);
	}

	@Override
	public boolean allowAllContent() {
		log.debug("Service {} method allowAllContent()", SERVICE_NAME);
		return false;
	}

	@Override
	public boolean isAcceptableContent(ContentResource resource) {
		log.debug("Service {} method isAcceptableContent({})", SERVICE_NAME, resource);
		return false;
	}

	@Override
	public boolean isAcceptableSize(ContentResource resource) {
		log.debug("Service {} method isAcceptableSize({})", SERVICE_NAME, resource);
		return false;
	}

	@Override
	public Map<String, SortedSet<String>> getAcceptableExtensionsToMimeTypes() {
		log.debug("Service {} method getAcceptableExtensionsToMimeTypes()", SERVICE_NAME);
		return new HashMap<String, SortedSet<String>>();
	}

	@Override
	public Map<String, SortedSet<String>> getAcceptableFileTypesToExtensions() {
		log.debug("Service {} method getAcceptableFileTypesToExtensions()", SERVICE_NAME);
		return new HashMap<String, SortedSet<String>>();
	}

	@Override
	public boolean isSiteAcceptable(Site site) {
		log.debug("Service {} method isSiteAcceptable({})", SERVICE_NAME, site);
		return false;
	}

	@Override
	public boolean isDirectAccess(Site site) {
		log.debug("Service {} method isDirectAccess({})", SERVICE_NAME, site);
		return false;
	}

	@Override
	public String getIconUrlForScore(Long score) {
		log.debug("Service {} method getIconUrlForScore({})", SERVICE_NAME, score);
		return "/library/content-review/noservice.png";
	}

	@Override
	public String getIconColorForScore(Long score) {
		log.debug("Service {} method getIconColorForScore({})", SERVICE_NAME, score);
		return null;
	}

	@Override
	public boolean allowResubmission() {
		log.debug("Service {} method allowResubmission()", SERVICE_NAME);
		return false;
	}

	@Override
	public void removeFromQueue(String ContentId) {
		log.debug("Service {} method removeFromQueue()", SERVICE_NAME);
	}

	@Override
	public String getLocalizedStatusMessage(String messageCode, String userRef) {
		log.debug("Service {} method getLocalizedStatusMessage({}, {})", SERVICE_NAME, messageCode, userRef);
		return "There is no content review service configured, please see your administrator";
	}

	@Override
	public String getLocalizedStatusMessage(String messageCode) {
		log.debug("Service {} method getLocalizedStatusMessage({})", SERVICE_NAME, messageCode);
		return "There is no content review service configured, please see your administrator";
	}

	@Override
	public String getReviewError(String contentId) {
		log.debug("Service {} method getReviewError({})", SERVICE_NAME, contentId);
		return "There is no content review service configured, please see your administrator";
	}

	@Override
	public String getLocalizedStatusMessage(String messageCode, Locale locale) {
		log.debug("Service {} method getLocalizedStatusMessage({}, {})", SERVICE_NAME, messageCode, locale);
		return "There is no content review service configured, please see your administrator";
	}

	@Override
	public Map getAssignment(String siteId, String taskId) throws SubmissionException, TransientSubmissionException {
		log.debug("Service {} method getAssignment({}, {})", SERVICE_NAME, siteId, taskId);
		return null;
	}

	@Override
	public void createAssignment(String siteId, String taskId, Map extraAsnnOpts)
			throws SubmissionException, TransientSubmissionException {
		log.debug("Service {} method createAssignment({}, {}, {})", SERVICE_NAME, siteId, taskId, extraAsnnOpts);
	}

	@Override
	public String getLTIAccess(String taskId, String siteId) {
		log.debug("Service {} method getLTIAccess({}, {})", SERVICE_NAME, taskId, siteId);
		return null;
	}

	@Override
	public boolean deleteLTITool(String taskId, String siteId) {
		log.debug("Service {} method deleteLTITool({}, {})", SERVICE_NAME, taskId, siteId);
		return false;
	}
}
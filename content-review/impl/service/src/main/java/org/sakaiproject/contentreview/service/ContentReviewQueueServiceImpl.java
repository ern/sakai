/**
 * Copyright (c) 2003 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.contentreview.service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.contentreview.dao.ContentReviewConstants;
import org.sakaiproject.contentreview.dao.ContentReviewItem;
import org.sakaiproject.contentreview.dao.ContentReviewItemDao;
import org.sakaiproject.contentreview.exception.QueueException;
import org.sakaiproject.contentreview.exception.ReportException;
import org.sakaiproject.contentreview.exception.SubmissionException;
import org.springframework.transaction.annotation.Transactional;

import lombok.Setter;

@Slf4j
public class ContentReviewQueueServiceImpl implements ContentReviewQueueService {

	@Setter
	private ContentReviewItemDao itemDao;
	
	@Override
	@Transactional
	public void queueContent(Integer providerId, String userId, String siteId, String taskId, List<ContentResource> content, String submissionId, boolean resubmission)
			throws QueueException {
		
		Objects.requireNonNull(providerId, "providerId cannot be null");
		Objects.requireNonNull(userId, "userId cannot be null");
		Objects.requireNonNull(siteId, "siteId cannot be null");
		Objects.requireNonNull(taskId, "taskId cannot be null");
		Objects.requireNonNull(content, "content cannot be null");
				
		for (ContentResource resource : content) {
			String contentId = resource.getId();
			
			/*
			 * first check that this content has not been submitted before this may
			 * not be the best way to do this - perhaps use contentId as the primary
			 * key for now id is the primary key and so the database won't complain
			 * if we put in repeats necessitating the check
			 */
			
			Optional<ContentReviewItem> existingItem = itemDao.findByProviderAndContentId(providerId, contentId);
			
			if (existingItem.isPresent()) {
				throw new QueueException("Content " + contentId + " is already queued");
			}
			
			ContentReviewItem item = new ContentReviewItem(contentId, userId, siteId, taskId, new Date(), ContentReviewConstants.CONTENT_REVIEW_NOT_SUBMITTED_CODE, providerId);
			item.setSubmissionId(submissionId);
			item.setResubmission(resubmission);
			
			log.debug("Adding content: " + contentId + " from site " + siteId + " and user: " + userId + " for task: " + taskId + " to submission queue");
			
			itemDao.create(item);
		}
	}
	
	@Override
	@Transactional(readOnly=true)
	public int getReviewScore(Integer providerId, String contentId) throws QueueException, ReportException, Exception {
		Objects.requireNonNull(providerId, "providerId cannot be null");
		Objects.requireNonNull(contentId, "contentId cannot be null");
		
		log.debug("Getting review score for providerId: " + providerId + " contentId: " + contentId);
		
		Optional<ContentReviewItem> matchingItem = itemDao.findByProviderAndContentId(providerId, contentId);
		
		if (!matchingItem.isPresent()) {
			log.debug("Content " + contentId + " has not been queued previously");
			throw new QueueException("Content " + contentId + " has not been queued previously");
		}
		
		ContentReviewItem item = matchingItem.get();
		if (item.getStatus().compareTo(ContentReviewConstants.CONTENT_REVIEW_SUBMITTED_REPORT_AVAILABLE_CODE) != 0) {
			log.debug("Report not available: " + item.getStatus());
			throw new ReportException("Report not available: " + item.getStatus());
		}
		
		return item.getReviewScore().intValue();
	}

	@Override
	@Transactional(readOnly=true)
	public Long getReviewStatus(Integer providerId, String contentId) throws QueueException {
		Objects.requireNonNull(providerId, "providerId cannot be null");
		Objects.requireNonNull(contentId, "contentId cannot be null");

		log.debug("Returning review status for content: " + contentId);

		Optional<ContentReviewItem> matchingItem = itemDao.findByProviderAndContentId(providerId, contentId);

		if (!matchingItem.isPresent()) {
			log.debug("Content " + contentId + " has not been queued previously");
			throw new QueueException("Content " + contentId + " has not been queued previously");
		}

		return matchingItem.get().getStatus();
	}

	@Override
	@Transactional(readOnly=true)
	public Date getDateQueued(Integer providerId, String contentId) throws QueueException {
		Objects.requireNonNull(providerId, "providerId cannot be null");
		Objects.requireNonNull(contentId, "contentId cannot be null");

		log.debug("Returning date queued for content: " + contentId);

		Optional<ContentReviewItem> matchingItem = itemDao.findByProviderAndContentId(providerId, contentId);
		if (!matchingItem.isPresent()) {
			log.debug("Content " + contentId + " has not been queued previously");
			throw new QueueException("Content " + contentId + " has not been queued previously");
		}

		return matchingItem.get().getDateQueued();
	}

	@Override
	@Transactional(readOnly=true)
	public Date getDateSubmitted(Integer providerId, String contentId) throws QueueException, SubmissionException {
		Objects.requireNonNull(providerId, "providerId cannot be null");
		Objects.requireNonNull(contentId, "contentId cannot be null");

		log.debug("Returning date queued for content: " + contentId);

		Optional<ContentReviewItem> matchingItem = itemDao.findByProviderAndContentId(providerId, contentId);

		if (!matchingItem.isPresent()) {
			log.debug("Content " + contentId + " has not been queued previously");
			throw new QueueException("Content " + contentId + " has not been queued previously");
		}

		ContentReviewItem item = matchingItem.get();
		if (item.getDateSubmitted() == null) {
			log.debug("Content not yet submitted: " + item.getStatus());
			throw new SubmissionException("Content not yet submitted: " + item.getStatus());
		}

		return item.getDateSubmitted();
	}

	@Override
	@Transactional(readOnly=true)
	public List<ContentReviewItem> getContentReviewItems(Integer providerId, String siteId, String taskId) {
		Objects.requireNonNull(providerId, "providerId cannot be null");

		return itemDao.findByProviderAnyMatching(providerId, null, null, siteId, taskId, null, null, null);
	}

	@Override
	@Transactional(readOnly=true)
	public List<ContentReviewItem> getAllContentReviewItemsGroupedBySiteAndTask(Integer providerId) {
		Objects.requireNonNull(providerId, "providerId cannot be null");

		log.debug("Returning list of items grouped by site and task");

		return itemDao.findByProviderGroupedBySiteAndTask(providerId);
	}

	@Override
	@Transactional(readOnly=true)
	public List<ContentReviewItem> getContentReviewItemsByExternalId(Integer providerId, String externalId) {
		Objects.requireNonNull(providerId, "providerId cannot be null");

		log.debug("Returning list of items where externalId = {}", externalId);

		return itemDao.findByProviderAnyMatching(providerId, null, null, null, null, externalId, null, null);
	}


	@Override
	@Transactional
	public void resetUserDetailsLockedItems(Integer providerId, String userId) {
		Objects.requireNonNull(providerId, "providerId cannot be null");
		Objects.requireNonNull(userId, "userId cannot be null");

		List<ContentReviewItem> lockedItems = itemDao.findByProviderAnyMatching(providerId, null, userId, null, null, null, ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_USER_DETAILS_CODE, null);
		for (ContentReviewItem item : lockedItems) {
			item.setStatus(ContentReviewConstants.CONTENT_REVIEW_SUBMISSION_ERROR_RETRY_CODE);
			itemDao.save(item);
		}
	}

	@Override
	@Transactional
	public void removeFromQueue(Integer providerId, String contentId) {
		Objects.requireNonNull(providerId, "providerId cannot be null");
		Objects.requireNonNull(contentId, "contentId cannot be null");

		Optional<ContentReviewItem> item = itemDao.findByProviderAndContentId(providerId, contentId);
		if (item.isPresent()) {
			itemDao.delete(item.get());
		}
	}

	@Override
	@Transactional(readOnly=true)
	public Optional<ContentReviewItem> getQueuedItem(Integer providerId, String contentId) {
		Objects.requireNonNull(providerId, "providerId cannot be null");
		Objects.requireNonNull(contentId, "contentId cannot be null");
		
		return itemDao.findByProviderAndContentId(providerId, contentId);
	}

	@Override
	@Transactional(readOnly=true)
	public List<ContentReviewItem> getQueuedNotSubmittedItems(Integer providerId) {
		return itemDao.findByProviderAnyMatching(providerId, null, null, null, null, null, ContentReviewConstants.CONTENT_REVIEW_NOT_SUBMITTED_CODE, null);
	}

	@Override
	@Transactional(readOnly=true)
	public Optional<ContentReviewItem> getNextItemInQueueToSubmit(Integer providerId) {
		Objects.requireNonNull(providerId, "providerId cannot be null");
		return itemDao.findByProviderSingleItemToSubmit(providerId);
	}

	@Override
	@Transactional(readOnly=true)
	public List<ContentReviewItem> getAwaitingReports(Integer providerId) {
		Objects.requireNonNull(providerId, "providerId cannot be null");
		return itemDao.findByProviderAwaitingReports(providerId);
	}
	
	@Override
	@Transactional
	public void update(ContentReviewItem item) {
		Objects.requireNonNull(item, "item cannot be null");
		Objects.requireNonNull(item.getId(), "Id cannot be null");
		Objects.requireNonNull(item.getProviderId(), "providerId cannot be null");
		
		itemDao.save(item);
	}

	@Override
	@Transactional
	public void delete(ContentReviewItem item) {
		Objects.requireNonNull(item, "item cannot be null");
		Objects.requireNonNull(item.getId(), "Id cannot be null");
		Objects.requireNonNull(item.getProviderId(), "providerId cannot be null");
		
		itemDao.delete(item);
	}	
}

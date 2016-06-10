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
package org.sakaiproject.contentreview.advisors;

import org.sakaiproject.site.api.Site;

public interface ContentReviewSiteAdvisor {

	/**
	 * Indicates whether a site is allowed to use the Content Review Service
	 *
	 * @param site the {@link Site} to check
	 * @return true if allowed otherwise false
     */
	boolean siteCanUseReviewService(Site site);

	/**
	 * Indicates whether a site is allowed to use LTI Content Review Service
	 *
	 * @param site the {@link Site} to check
	 * @return true if allowed otherwise false
	 */
	boolean siteCanUseLTIReviewService(Site site);

	/**
	 * Indicates whether a site is allowed to use LTI Direct Submission
	 *
	 * @param site the {@link Site} to check
	 * @return true if allowed otherwise false
	 */
	boolean siteCanUseDirectReviewService(Site site);
}

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
package org.sakaiproject.webapi.beans;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.AssignmentReferenceReckoner;
import org.sakaiproject.calendar.api.CalendarConstants;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.time.api.TimeRange;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j
public class CalendarEventRestBean {

    private String id;
    private String siteId;
    private String siteTitle;
    private String creator;
    private String viewText;
    private String title;
    private String tool;
    private String type;
    private String assignmentId;
    private long start;
    private long duration;
    private List<AttachmentRestBean> attachments;
    private RecurrenceRuleRestBean recurrence;
    private String url;

    public CalendarEventRestBean(CalendarEvent ce, ContentHostingService chs, EntityManager em) {

        id = ce.getId();
        siteId = ce.getSiteId();
        siteTitle = ce.getSiteName();
        creator = ce.getCreator();
        viewText = ce.getDescription();
        title = ce.getDisplayName();
        type = ce.getType();
        TimeRange timeRange = ce.getRange();
        start = timeRange.firstTime().getTime();
        duration = timeRange.duration();
        recurrence = new RecurrenceRuleRestBean(ce.getRecurrenceRule());
        assignmentId = ce.getField(CalendarConstants.NEW_ASSIGNMENT_DUEDATE_CALENDAR_ASSIGNMENT_ID);
        if (StringUtils.isNotBlank(assignmentId)) {
            this.tool = "assignments";
            String reference = AssignmentReferenceReckoner.reckoner().context(siteId).subtype("a").id(assignmentId).reckon().getReference();
            url = em.getUrl(reference, Entity.UrlType.PORTAL).orElse("");
        } else {
            url = ce.getUrl();
        }

        attachments = ce.getAttachments().stream().map(ref -> {
            String resourceId = ref.getId();
            try {
                return new AttachmentRestBean(chs.getResource(resourceId));
            } catch (Exception e) {
                log.warn("Could not load resource [{}], {}", resourceId, e.toString());
                return null;
            }
        }).collect(Collectors.toList());
    }
}

/**
 * Copyright (c) 2007-2016 The Apereo Foundation
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
/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.impl.messages;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.signup.api.SakaiFacade;
import org.sakaiproject.signup.api.messages.SignupEmailNotification;
import org.sakaiproject.signup.api.model.MeetingTypes;
import org.sakaiproject.signup.api.model.SignupMeeting;
import org.sakaiproject.signup.api.model.SignupTimeslot;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.ResourceLoader;

import lombok.Getter;
import lombok.Setter;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * Abstract base class for Signup Email notifications that provides common functionality
 * for email notifications such as getting footer information, site details, and time
 * formatting. Implements SignupEmailNotification interface for sending email notifications
 * related to signup events.
 * <p>
 * Subclasses must implement abstract methods to define email headers, message content,
 * from address and subject. Provides utility methods for:
 * <ul>
 * <li>Generating email footers with site access links</li>
 * <li>Formatting site titles and meeting information</li>
 * <li>Converting Java dates to Sakai Time objects</li>
 * <li>Checking user attendance in timeslots</li>
 * <li>Retrieving events for attendees</li>
 * </ul>
 */
abstract public class SignupEmailBase implements SignupEmailNotification, MeetingTypes {

	public static final String NEWLINE = "<BR>\r\n";

	@Getter @Setter private SakaiFacade sakaiFacade;
	@Setter protected ResourceLoader rb;
	@Getter protected SignupMeeting meeting;
	@Setter @Getter protected boolean modifyComment = false;
    // Indicates whether the email represents a cancellation - to be overwritten by subclasses
	@Getter protected boolean cancellation = false;

    private String myServiceName = null;

	protected String getFooter(String newline) {
        // tag the message - HTML version
        if (this.meeting.getCurrentSiteId() == null) {
            return getFooterWithNoAccessUrl(newline);
        } else {
            return getFooterWithAccessUrl(newline);
        }
	}

	protected String getFooter(String newline, String targetSiteId) {
		// tag the message - HTML version
		Object[] params = new Object[] {
                getServiceName(),
				"<a href=\"" + getSiteAccessUrl(targetSiteId) + "\">" + getSiteAccessUrl(targetSiteId) + "</a>",
				getSiteTitle(targetSiteId),
                newline };
        return newline
                + rb.getString("separator")
                + newline
                + MessageFormat.format(rb.getString("body.footer.text"), params)
                + newline;
	}
	
	private String getFooterWithAccessUrl(String newline) {
		// tag the message - HTML version
		Object[] params = new Object[] {
                getServiceName(),
				"<a href=\"" + getSiteAccessUrl() + "\">" + getSiteAccessUrl() + "</a>",
                getSiteTitle(),
                newline
        };
        return newline
                + rb.getString("separator")
                + newline
                + MessageFormat.format(rb.getString("body.footer.text"), params)
                + newline;
	}
	
	private String getFooterWithNoAccessUrl(String newline) {
		// tag the message - HTML version
		Object[] params = new Object[] {
                getServiceName(),
				getSiteTitle(),
                newline
        };
        return newline
                + rb.getString("separator")
                + newline
                + MessageFormat.format(rb.getString("body.footer.text.no.access.link"), params)
                + newline;
	}

	/**
	 * get the email Header, which contains destination email address, subject
	 * etc.
	 */
	abstract public List<String> getHeader();

	/**
	 * get the main message for this email
	 */
	abstract public String getMessage();
	
	/**
	 * get the from address for this email
	 */
	abstract public String getFromAddress();
	
	/**
	 * get the subject for this email
	 */
	abstract public String getSubject();
	
	/**
	 * get current site Id
	 * 
	 * @return the current site Id
	 */
	protected String getSiteId() {
		String siteId = getSakaiFacade().getCurrentLocationId();
        if (SakaiFacade.NO_LOCATION.equals(siteId)) {
            siteId = meeting.getCurrentSiteId() != null ? this.meeting.getCurrentSiteId() : SakaiFacade.NO_LOCATION;
        }
        return siteId;
	}

	protected String getSiteTitle() {
		return getSakaiFacade().getLocationTitle(getSiteId());
	}

	protected String getSiteTitle(String targetSiteId) {
		return getSakaiFacade().getLocationTitle(targetSiteId);
	}
	
	protected String getShortSiteTitle(String targetSiteId) {
		return getSakaiFacade().getLocationTitle(targetSiteId);
	}

	protected String getSiteTitleWithQuote() {
		return "\"" + getSiteTitle() + "\"";
	}

	protected String getSiteTitleWithQuote(String targetSiteId) {
		return "\"" + getSiteTitle(targetSiteId) + "\"";
	}
	
	protected String getShortSiteTitleWithQuote(String targetSiteId) {
		return "\"" + getShortSiteTitle(targetSiteId) + "\"";
	}

    protected String getSiteAccessUrl() {
        return getSakaiFacade().getServerConfigurationService().getPortalUrl()
                + "/site/" + getSiteId()
                + "/page/" + getSakaiFacade().getCurrentPageId();
    }

    protected String getSiteAccessUrl(String targetSiteId) {
        return getSakaiFacade().getServerConfigurationService().getPortalUrl()
                + "/site/" + targetSiteId
                + "/page/" + getSakaiFacade().getSiteSignupPageId(targetSiteId);
    }

	protected String getAbbreviatedMeetingTitle(){
		return StringUtils.abbreviate(meeting.getTitle(), 30);
	}

	/**
	 * This will convert the Java date object to a Sakai's Time object, which
	 * provides all the useful methods for output.
	 * 
	 * @param date
	 *            a Java Date object.
	 * @return a Sakai's Time object.
	 */
	protected Time getTime(Date date) {
        return getSakaiFacade().getTimeService().newTime(date.getTime());
	}

	/**
	 * Make first letter of the string to Capital letter
	 * 
	 * @param st
	 *            a string value
	 * @return a string with a first capital letter
	 */
	protected String makeFirstCapLetter(String st) {
		return StringUtils.capitalize(st);
	}

	protected String getServiceName() {
		/* first look at email bundle and then sakai.properties.
		 * it will allow user to define different 'ui.service' value */
        if (myServiceName == null) {
            try {
                if (rb.keySet().contains("ui.service")) {
                    myServiceName = rb.getString("ui.service");
                } else {
                    myServiceName = getSakaiFacade().getServerConfigurationService().getString("ui.service", "Sakai Service");
                }
            } catch (Exception e) {
                myServiceName = getSakaiFacade().getServerConfigurationService().getString("ui.service", "Sakai Service");
            }
        }
        return myServiceName;
	}
	
	
	protected String getRepeatTypeMessage(SignupMeeting meeting){
		String recurFrqs ="";
		if (DAILY.equals(meeting.getRepeatType()))
			recurFrqs = rb.getString("body.meeting.repeatDaily");
		else if (WEEKDAYS.equals(meeting.getRepeatType()))
			recurFrqs = rb.getString("body.meeting.repeatWeekdays");
		else if (WEEKLY.equals(meeting.getRepeatType()))
			recurFrqs = rb.getString("body.meeting.repeatWeekly");
		else if (BIWEEKLY.equals(meeting.getRepeatType()))
			recurFrqs = rb.getString("body.meeting.repeatBiWeekly");
		else
			recurFrqs = rb.getString("body.meeting.unknown.repeatType");

		return recurFrqs;
	}
	
	protected String getServerFromAddress() {
		return  getServiceName() +" <" + getSakaiFacade().getServerConfigurationService().getSmtpFrom() + ">";
	}

	protected boolean userIsAttendingTimeslot(User user, SignupTimeslot timeslot) {
		return timeslot.getAttendee(user.getId()) != null;
	}

	protected List<VEvent> eventsWhichUserIsAttending(User user) {
		final List<SignupTimeslot> timeslots = meeting.getSignupTimeSlots();
		List<VEvent> events = new ArrayList<>();
		for (SignupTimeslot timeslot : timeslots) {
			if (userIsAttendingTimeslot(user, timeslot)) {
				final VEvent event = timeslot.getVevent();
				if (event != null) {
					events.add(event);
				}
			}
		}
		return events;
	}

}

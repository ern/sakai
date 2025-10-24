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
package org.sakaiproject.signup.impl.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.fortuna.ical4j.model.component.VEvent;
import org.sakaiproject.signup.api.SignupCalendarHelper;
import org.sakaiproject.signup.api.model.SignupAttendee;
import org.sakaiproject.user.api.User;


/**
 * Base class for emails sent to organizers of signup meetings.
 * Contains shared functionality for generating calendar events with attendees.
 * @author Ben Holmes
 */
abstract public class OrganizerEmailBase extends SignupEmailBase {

    @Override
    public List<VEvent> generateEvents(User user, SignupCalendarHelper calendarHelper) {
        List<VEvent> events = new ArrayList<>();

        VEvent meetingEvent = meeting.getVevent();
        if (meetingEvent == null) {
            return events;
        }

        Set<SignupAttendee> attendees = meeting.getSignupTimeSlots().stream()
                .flatMap(timeslot -> timeslot.getAttendees().stream())
                .collect(Collectors.toSet());

        calendarHelper.addAttendeesToVEvent(meetingEvent, attendees);
        events.add(meetingEvent);
        return events;
    }
}

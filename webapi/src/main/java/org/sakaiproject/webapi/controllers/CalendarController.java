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

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.webapi.beans.CalendarEventRestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class CalendarController extends AbstractSakaiApiController {

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private ContentHostingService contentHostingService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
	private PortalService portalService;

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @Autowired
    private UserDirectoryService userDirectoryService;

    private final Function<CalendarEvent, CalendarEventRestBean> convert = ce -> new CalendarEventRestBean(ce, contentHostingService, entityManager);

    @GetMapping(value = "/users/current/calendar", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getCurrentUserCalendar() throws UserNotDefinedException {

        checkSakaiSession();

        List<String> refs = portalService.getPinnedSites().stream()
            .map(siteId -> calendarService.calendarReference(siteId, "main"))
            .collect(Collectors.toList());

        List<CalendarEventRestBean> beans = calendarService.getEvents(refs, null, false).stream()
                .map(convert)
                .collect(Collectors.toList());

        return Map.of("events", beans);
    }

    @GetMapping(value = "/sites/{siteId}/calendar", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getSiteCalendar(@PathVariable String siteId) throws UserNotDefinedException {

        checkSakaiSession();

        List<String> refs = List.of(calendarService.calendarReference(siteId, "main"));

        return Map.of(
            "events", calendarService.getEvents(refs, null, false).stream().map(convert).collect(Collectors.toList())
        );
    }
}

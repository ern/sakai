/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon based on code created by pascal alma
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.rubrics.security;

import static org.sakaiproject.rubrics.logic.RubricsConstants.RBCS_TOOL;

import javax.annotation.Resource;

import org.sakaiproject.rubrics.logic.AuthenticatedRequestContext;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;

/**
 * Used for checking the token from the request and supply the UserDetails if the token is valid
 */
@Component
public class SakaiAuthenticationManager implements AuthenticationManager {

    @Resource(name = "org.sakaiproject.tool.api.SessionManager")
    private SessionManager sessionManager;

    @Resource(name = "org.sakaiproject.tool.api.ToolManager")
    private ToolManager toolManager;

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        System.out.println("HERERERERERER");
        Session session = sessionManager.getCurrentSession();
        if (session == null) {
            throw new SessionAuthenticationException(String.format("Sakai session does not exists for request, user = %s", authentication.getPrincipal()));
        }
        //AuthenticatedRequestContext authenticatedRequestContext = new AuthenticatedRequestContext(
        //        session.getUserId(), RBCS_TOOL, toolManager.getCurrentPlacement().getContext(), "site");
        authentication.setAuthenticated(true);
        return authentication;
    }
}

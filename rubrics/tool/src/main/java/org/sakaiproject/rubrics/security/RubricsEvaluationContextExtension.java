/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
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

import org.sakaiproject.rubrics.repository.CriterionRestRepository;
import org.sakaiproject.rubrics.repository.EvaluationRestRepository;
import org.sakaiproject.rubrics.repository.RatingRestRepository;
import org.sakaiproject.rubrics.repository.RubricRestRepository;
import org.sakaiproject.rubrics.repository.ToolItemRubricAssociationRestRepository;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import javax.annotation.Resource;

import org.springframework.stereotype.Component;

@Component
public class RubricsEvaluationContextExtension extends SecurityEvaluationContextExtension {

	//private Authentication authentication;

    @Autowired
    private RubricRestRepository rubricRestRepository;

    @Autowired
    private CriterionRestRepository criterionRestRepository;

    @Autowired
    private RatingRestRepository ratingRestRepository;

    @Autowired
    private EvaluationRestRepository evaluationRestRepository;

    @Autowired
    private ToolItemRubricAssociationRestRepository toolItemRubricAssociationRestRepository;

    @Resource(name = "org.sakaiproject.tool.api.SessionManager")
    private SessionManager sessionManager;

	/**
	 * Creates a new instance that uses the current {@link Authentication} found on the
	 * {@link org.springframework.security.core.context.SecurityContextHolder}.
	 */
    /*
	public RubricsEvaluationContextExtension() {
	}
    */

	/**
	 * Creates a new instance that always uses the same {@link Authentication} object.
	 *
	 * @param authentication the {@link Authentication} to use
	 */
    /*
	public RubricsEvaluationContextExtension(Authentication authentication) {
		this.authentication = authentication;
	}
    */

	public String getExtensionId() {
		return "rubrics";
	}

	@Override
	public Object getRootObject() {
        System.out.println("GETROOTOBJECT");
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Session session = sessionManager.getCurrentSession();
        String userId = session.getUserId();
        System.out.println("USERID: " + userId);
		return new CustomMethodSecurityExpressionRoot(this.rubricRestRepository
                                                            , this.criterionRestRepository
                                                            , this.ratingRestRepository
                                                            , this.evaluationRestRepository
                                                            , this.toolItemRubricAssociationRestRepository
                                                            , authentication) {
		};
	}
}

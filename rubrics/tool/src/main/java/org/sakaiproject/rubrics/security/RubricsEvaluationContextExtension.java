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

import org.springframework.data.repository.query.spi.EvaluationContextExtensionSupport;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * <p>
 * By defining this object as a Bean, Spring Security is exposed as SpEL expressions for
 * creating Spring Data queries.
 *
 * <p>
 * With Java based configuration, we can define the bean using the following:
 *
 * <p>
 * For example, if you return a UserDetails that extends the following User object:
 *
 * <pre>
 * &#064;Entity
 * public class User {
 *     &#064;GeneratedValue(strategy = GenerationType.AUTO)
 *     &#064;Id
 *     private Long id;
 *
 *     ...
 * }
 * </pre>
 *
 * <p>
 * And you have a Message object that looks like the following:
 *
 * <pre>
 * &#064;Entity
 * public class Message {
 *     &#064;Id
 *     &#064;GeneratedValue(strategy = GenerationType.AUTO)
 *     private Long id;
 *
 *     &#064;OneToOne
 *     private User to;
 *
 *     ...
 * }
 * </pre>
 *
 * You can use the following {@code Query} annotation to search for only messages that are
 * to the current user:
 *
 * <pre>
 * &#064;Repository
 * public interface SecurityMessageRepository extends MessageRepository {
 *
 * 	&#064;Query(&quot;select m from Message m where m.to.id = ?#{ principal?.id }&quot;)
 * 	List&lt;Message&gt; findAll();
 * }
 * </pre>
 *
 * This works because the principal in this instance is a User which has an id field on
 * it.
 *
 * @since 4.0
 * @author Rob Winch
 */
public class RubricsEvaluationContextExtension extends EvaluationContextExtensionSupport {

	private Authentication authentication;

	/**
	 * Creates a new instance that uses the current {@link Authentication} found on the
	 * {@link org.springframework.security.core.context.SecurityContextHolder}.
	 */
	public RubricsEvaluationContextExtension() {
	}

	/**
	 * Creates a new instance that always uses the same {@link Authentication} object.
	 *
	 * @param authentication the {@link Authentication} to use
	 */
	public RubricsEvaluationContextExtension(Authentication authentication) {
		this.authentication = authentication;
	}

	public String getExtensionId() {
		return "security";
	}

	@Override
	public Object getRootObject() {
		Authentication authentication = getAuthentication();
		return new CustomMethodSecurityExpressionRoot(authentication) {
		};
	}

	private Authentication getAuthentication() {
		if (this.authentication != null) {
			return this.authentication;
		}

		SecurityContext context = SecurityContextHolder.getContext();
		return context.getAuthentication();
	}
}

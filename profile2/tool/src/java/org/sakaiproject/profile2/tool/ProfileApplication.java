/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.tool;

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.sakaiproject.profile2.tool.pages.MyProfile;
import org.sakaiproject.profile2.tool.pages.ViewProfile;
import org.sakaiproject.util.ResourceLoader;

public class ProfileApplication extends WebApplication {

	@Override
	protected void init() {
		super.init();

		// Configure for Spring injection
		getComponentInstantiationListeners().add(new SpringComponentInjector(this));

		getCspSettings().blocking().disabled();
		getResourceSettings().setThrowExceptionOnMissingResource(false);
		getMarkupSettings().setStripWicketTags(true);

		// On Wicket session timeout, redirect to main page
		getApplicationSettings().setPageExpiredErrorPage(MyProfile.class);
		getApplicationSettings().setAccessDeniedPage(MyProfile.class);

		// Custom resource loader since our properties are not in the default location
		getResourceSettings().getStringResourceLoaders().add(new ProfileStringResourceLoader());

		// encrypt URLs
		// this immediately sets up a session (note that things like css now becomes bound to the session)
		//getSecuritySettings().setCryptFactory(new KeyInSessionSunJceCryptFactory()); // diff key per user
		//final IRequestMapper cryptoMapper = new CryptoMapper(getRootRequestMapper(), this);
		//setRootRequestMapper(cryptoMapper);

		// page mounting
		mountPage("/profile", MyProfile.class);
		mountPage("/viewprofile/${id}", ViewProfile.class);

	}

	// Custom resource loader
	private static class ProfileStringResourceLoader implements IStringResourceLoader {

		private final ResourceLoader messages = new ResourceLoader("ProfileApplication");

		@Override
		public String loadStringResource(final Class<?> clazz, final String key,
				final Locale locale, final String style, final String variation) {
			this.messages.setContextLocale(locale);
			return this.messages.getString(key, key);
		}

		@Override
		public String loadStringResource(final Component component, final String key,
				final Locale locale, final String style, final String variation) {
			this.messages.setContextLocale(locale);
			return this.messages.getString(key, key);
		}

	}

	public ProfileApplication() {
	}

	// setup homepage
	@Override
	public Class<MyProfile> getHomePage() {
		return MyProfile.class;
	}

}

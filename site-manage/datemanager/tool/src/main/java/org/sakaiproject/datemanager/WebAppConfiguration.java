/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.datemanager;

import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.SakaiContextLoaderListener;
import org.sakaiproject.util.ToolListener;
import javax.servlet.FilterRegistration;

public class WebAppConfiguration implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
		rootContext.setServletContext(servletContext);
		rootContext.register(ThymeleafConfig.class);
		rootContext.register(MvcConfig.class);

		servletContext.addListener(new ToolListener());
		servletContext.addListener(new SakaiContextLoaderListener(rootContext));

		FilterRegistration requestFilterRegistration = servletContext.addFilter("sakai.request", RequestFilter.class);
		requestFilterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE), true, new String[]{"/*"});
		requestFilterRegistration.setInitParameter("upload.enabled", "true");

		Dynamic servlet = servletContext.addServlet("sakai.datemanager", new DispatcherServlet(rootContext));
		servlet.addMapping(new String[]{"/"});
		servlet.setLoadOnStartup(1);
	}
}

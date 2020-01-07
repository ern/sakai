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

package org.sakaiproject.rubrics;

import static org.sakaiproject.rubrics.logic.RubricsConstants.RBCS_TOOL;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.sakaiproject.rubrics.config.RubricsMvcConfiguration;
import org.sakaiproject.rubrics.config.RubricsSecurityConfiguration;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.SakaiContextLoaderListener;
import org.sakaiproject.util.ToolListener;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

//public class RubricsApplication implements WebApplicationInitializer {
public class RubricsApplication implements WebApplicationInitializer {
//public class RubricsApplication extends AbstractSecurityWebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) {
    //public void afterSpringSecurityFilterChain(ServletContext servletContext) {

        // Spring webapp configuration
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(RubricsMvcConfiguration.class);
        //context.register(RubricsSecurityConfiguration.class);
        servletContext.addListener(ToolListener.class);
        servletContext.addListener(new SakaiContextLoaderListener(context));

        // Rubrics servlet configuration
        ServletRegistration.Dynamic dispatcher = servletContext.addServlet(RBCS_TOOL, new DispatcherServlet(context));
        dispatcher.setLoadOnStartup(0);
        dispatcher.addMapping("/", "/index");

        // Sakai RequestFilter
        FilterRegistration.Dynamic reqFilter = servletContext.addFilter("sakai.request", new RequestFilter());
        reqFilter.addMappingForServletNames(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE), false, RBCS_TOOL);

        //FilterRegistration.Dynamic secFilter = servletContext.addFilter("springSecurityFilterChain", "org.springframework.web.filter.DelegatingFilterProxy");
        //secFilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE), true, "/*");
        //secFilter.addMappingForServletNames(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE), false, RBCS_TOOL);
    }
}

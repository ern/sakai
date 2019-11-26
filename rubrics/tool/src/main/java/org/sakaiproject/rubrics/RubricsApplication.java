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

import java.util.Arrays;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.impl.SpringCompMgr;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.ToolListener;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.WebApplicationInitializer;

@Configuration
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ComponentScan
@Slf4j
public class RubricsApplication implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext container) {

        container.addListener(ToolListener.class);
        ServletRegistration.Dynamic dispatcher
            = container.addServlet(RBCS_TOOL, new DispatcherServlet(new AnnotationConfigWebApplicationContext()));
        dispatcher.setLoadOnStartup(0);
        dispatcher.addMapping("/", "/index");

        FilterRegistration.Dynamic reqFilter = container.addFilter("sakai.request", new RequestFilter());
        reqFilter.addMappingForServletNames(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE), false, RBCS_TOOL);
    }

    /**
     * Required per http://docs.spring.io/spring-boot/docs/current/reference/html/howto-traditional-deployment.html
     * in order produce a traditional deployable war file.
     *
     * @param application
     * @return
     */

    /*
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        ConfigurableApplicationContext sharedAc = ((SpringCompMgr) ComponentManager.getInstance()).getApplicationContext();
        application.parent(sharedAc);
        return application.bannerMode(Banner.Mode.OFF).sources(RubricsApplication.class);
    }

    @Bean
    public ServletRegistrationBean rubricsServlet() {
        ServletRegistrationBean srb = new ServletRegistrationBean(new DispatcherServlet(new AnnotationConfigWebApplicationContext())) {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
                super.onStartup(servletContext);
                servletContext.addListener(ToolListener.class);
                // servletContext.addListener(SakaiContextLoaderListener.class);
            }
        };
        srb.setName("sakai.rubrics");
        srb.setLoadOnStartup(0);
        srb.addUrlMappings("/", "/index");
        return srb;
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean frb = new FilterRegistrationBean();
        frb.setName("sakai.request");
        frb.setServletNames(Arrays.asList(RBCS_TOOL, "dispatcherServlet"));
        frb.setFilter(new RequestFilter());
        frb.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE);
        return frb;
    }
    */
}

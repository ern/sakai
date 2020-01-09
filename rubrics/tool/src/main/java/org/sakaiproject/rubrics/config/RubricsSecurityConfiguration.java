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

package org.sakaiproject.rubrics.config;

import java.util.Arrays;

import org.sakaiproject.util.RequestFilter;

import org.sakaiproject.rubrics.security.CustomMethodSecurityExpressionHandler;
import org.sakaiproject.rubrics.security.CustomMethodSecurityExpressionRoot;
import org.sakaiproject.rubrics.security.RubricsEvaluationContextExtension;
import org.sakaiproject.rubrics.security.SakaiAuthenticationProvider;
import org.sakaiproject.rubrics.security.UnauthorizedAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.data.repository.query.spi.EvaluationContextExtension;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;


@SuppressWarnings("SpringJavaAutowiringInspection")
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
//public class RubricsSecurityConfiguration extends GlobalMethodSecurityConfiguration {
public class RubricsSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomMethodSecurityExpressionHandler expressionHandler;

    @Autowired
    private RubricsEvaluationContextExtension securityExtension;

    @Autowired
    private SakaiAuthenticationProvider authenticationProvider;

    @Bean
    public EvaluationContextExtension securityExtension() {
        System.out.println("HERE1");
        System.out.println("SEC EXTENSION NULL: " + (this.securityExtension == null));
        return this.securityExtension;
    }

    /*
    @Bean
    public SecurityEvaluationContextExtension securityEvaluationContextExtension(){
        System.out.println("HERE2");
        return new RubricsEvaluationContextExtension();
    }
    */

    /*
    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {

        System.out.println("HERE3");
        return this.expressionHandler;
    }
    */

    //@Configuration
    //public static class WebSecurityConfig extends WebSecurityConfigurerAdapter {

        //@Autowired
        //private SakaiAuthenticationProvider authenticationProvider;

        /*
        @Override
        public void configure(WebSecurity web) throws Exception {
            web.expressionHandler(this.expressionHandler);
        }
        */

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .authorizeRequests().regexMatchers("/rest.*").authenticated();
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            System.out.println("CONFIGUREAUTH");
            auth.authenticationProvider(this.authenticationProvider);
        }
    //}
}

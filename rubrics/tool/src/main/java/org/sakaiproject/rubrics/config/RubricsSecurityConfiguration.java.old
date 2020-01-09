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

import org.sakaiproject.rubrics.security.CustomMethodSecurityExpressionHandler;
import org.sakaiproject.rubrics.security.CustomMethodSecurityExpressionRoot;
import org.sakaiproject.rubrics.security.RubricsEvaluationContextExtension;
import org.sakaiproject.rubrics.security.SakaiAuthenticationProvider;
import org.sakaiproject.rubrics.security.UnauthorizedAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
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

@SuppressWarnings("SpringJavaAutowiringInspection")
@Configuration
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
//public class RubricsSecurityConfiguration extends GlobalMethodSecurityConfiguration {
public class RubricsSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private SakaiAuthenticationProvider authenticationProvider;

    @Autowired
    private CustomMethodSecurityExpressionHandler expressionHandler;

    @Autowired
    private RubricsEvaluationContextExtension securityExtension;

    @Bean
    public EvaluationContextExtension securityExtension() {
        System.out.println("HERE1");
        System.out.println("SEC EXTENSION NULL: " + (this.securityExtension == null));
        return this.securityExtension;
    }

    @Bean
    protected AuthenticationManager authenticationManager() {

        System.out.println("HERE2");
        return new ProviderManager(Arrays.asList(authenticationProvider));
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

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            System.out.println("CONFIGUREHTTP");
            http
                //.csrf().disable() // we don't need CSRF because our token is invulnerable
                .exceptionHandling().authenticationEntryPoint(new UnauthorizedAuthenticationEntryPoint())
                //.and()
                //.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests().antMatchers(
                        "/",
                        "/index",
                        "/favicon.ico",
                        "/*.html",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js"
                ).permitAll()
                .anyRequest().authenticated();

            // disable page caching
            http.headers().cacheControl();
        }

        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            System.out.println("CONFIGUREAUTH");
            auth.authenticationProvider(this.authenticationProvider);
        }


    //}
}

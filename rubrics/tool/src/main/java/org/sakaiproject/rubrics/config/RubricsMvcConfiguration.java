package org.sakaiproject.rubrics.config;

import org.sakaiproject.rubrics.logic.model.Criterion;
import org.sakaiproject.rubrics.logic.model.CriterionOutcome;
import org.sakaiproject.rubrics.logic.model.Evaluation;
import org.sakaiproject.rubrics.logic.model.Rating;
import org.sakaiproject.rubrics.logic.model.Rubric;
import org.sakaiproject.rubrics.logic.model.ToolItemRubricAssociation;
import org.sakaiproject.util.ResourceLoaderMessageSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.data.web.config.HateoasAwareSpringDataWebConfiguration;
import org.springframework.hateoas.Resource;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;

import org.sakaiproject.util.RequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;

@ComponentScan({"org.sakaiproject.rubrics"})
@Configuration
@EnableJpaRepositories({"org.sakaiproject.rubrics.repository"})
@EnableSpringConfigured
@EnableWebMvc
@Import({RubricsRepositoryRestMvcConfiguration.class})
public class RubricsMvcConfiguration extends WebMvcConfigurerAdapter implements ApplicationContextAware {

    @Setter private ApplicationContext applicationContext;

    @Bean
    public ViewResolver viewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCharacterEncoding("UTF-8");
        return resolver;
    }

    @Bean
    public TemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setEnableSpringELCompiler(true);
        engine.setTemplateResolver(templateResolver());
        engine.setTemplateEngineMessageSource(messageSource());
        return engine;
    }

    @Bean(name = "org.sakaiproject.rubrics.Messages")
    public MessageSource messageSource() {
        ResourceLoaderMessageSource messages = new ResourceLoaderMessageSource();
        messages.setBasename("Messages");
        return messages;
    }

    private ITemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(applicationContext);
        resolver.setPrefix("/WEB-INF/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        return resolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // registry.addResourceHandler("/css/**").addResourceLocations("classpath:/css/");
        // registry.addResourceHandler("/icons/**").addResourceLocations("classpath:/icons/");
        registry.addResourceHandler("/webcomponents/**").addResourceLocations("classpath:/static/webcomponents/");
        super.addResourceHandlers(registry);
    }
}

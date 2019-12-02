package org.sakaiproject.rubrics.config;

import org.sakaiproject.rubrics.logic.model.Criterion;
import org.sakaiproject.rubrics.logic.model.CriterionOutcome;
import org.sakaiproject.rubrics.logic.model.Evaluation;
import org.sakaiproject.rubrics.logic.model.Rating;
import org.sakaiproject.rubrics.logic.model.Rubric;
import org.sakaiproject.rubrics.logic.model.ToolItemRubricAssociation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.SpringDataJacksonConfiguration;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.hal.HalConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@ComponentScan(basePackageClasses = RepositoryRestController.class,
        includeFilters = @ComponentScan.Filter(BasePathAwareController.class), useDefaultFilters = false)
@ImportResource("classpath*:META-INF/spring-data-rest/**/*.xml")
@Import({ SpringDataJacksonConfiguration.class, EnableSpringDataWebSupport.QuerydslActivator.class })
public class RubricsRepositoryRestMvcConfiguration extends RepositoryRestMvcConfiguration {

    private ObjectMapper objectMapper;
    private HalConfiguration halConfiguration;

    @Bean
    public HalConfiguration halConfiguration() {
        if (halConfiguration == null) {
            halConfiguration = new HalConfiguration();
        }
        return halConfiguration;
    }

    @Bean
    @Primary
    public ObjectMapper halObjectMapper() {
        if (objectMapper == null) {
            objectMapper = super.halObjectMapper();
        }
        return objectMapper;
    }

    @Override
    public RepositoryRestConfiguration config() {
        RepositoryRestConfiguration config = super.config();

        config.exposeIdsFor(Rubric.class, Criterion.class, Rating.class, ToolItemRubricAssociation.class, Evaluation.class, Resource.class, CriterionOutcome.class);
        config.setBasePath("/rest");
        config.setReturnBodyOnCreate(true);
        config.setReturnBodyOnUpdate(true);

        return config;
    }
}

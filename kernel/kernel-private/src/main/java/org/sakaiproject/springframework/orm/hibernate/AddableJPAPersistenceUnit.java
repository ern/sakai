package org.sakaiproject.springframework.orm.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by enietzel on 6/21/17.
 */
@Slf4j
public class AddableJPAPersistenceUnit implements PersistenceUnitPostProcessor, ApplicationContextAware {

    @Setter private ApplicationContext applicationContext;

    @Override
    public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
        List<AdditionalJPAPersistentUnit> units = new ArrayList<>();
        String[] unitNames = applicationContext.getBeanNamesForType(AdditionalJPAPersistentUnit.class, false, false);

        for (String name : unitNames) {
            try {
                units.add((AdditionalJPAPersistentUnit) applicationContext.getBean(name));

                Collections.sort(units);

                units.forEach(u -> u.processAdditionalUnit(pui));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }
}

package org.sakaiproject.springframework.orm.hibernate.impl;

import org.sakaiproject.springframework.orm.hibernate.AdditionalPersistentUnit;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by enietzel on 6/21/17.
 */
@Slf4j
public class AdditionalPersistentUnitImpl implements AdditionalPersistentUnit {

    @Setter private Class<?>[] annotatedClasses;
    @Getter @Setter private Integer sortOrder = Integer.MAX_VALUE;

    @Override
    public void processAdditionalUnit(MutablePersistenceUnitInfo pui) {
        if (annotatedClasses != null) {
            for (Class<?> clazz : annotatedClasses) {
                log.info("EntityManagerFactory add annotated class [{}]", clazz.getCanonicalName());
                pui.addManagedClassName(clazz.getName());
            }
        }
    }

    @Override
    public int compareTo(AdditionalPersistentUnit o) {
        return getSortOrder().compareTo(o.getSortOrder());
    }
}

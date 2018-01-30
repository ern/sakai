package org.sakaiproject.springframework.orm.hibernate;

import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

/**
 * Created by enietzel on 6/21/17.
 */
public interface AdditionalPersistentUnit extends Comparable<AdditionalPersistentUnit> {
    Integer getSortOrder();
    void processAdditionalUnit(MutablePersistenceUnitInfo pui);
    void setAnnotatedClasses(Class<?>... annotatedClasses);
}

package org.sakaiproject.springframework.orm.hibernate;

import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

/**
 * Created by enietzel on 6/21/17.
 */
public interface AdditionalJPAPersistentUnit extends Comparable<AdditionalJPAPersistentUnit> {
    Integer getSortOrder();
    void processAdditionalUnit(MutablePersistenceUnitInfo pui);
    void setAnnotatedClasses(Class<?>... annotatedClasses);
}

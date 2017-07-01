package org.sakaiproject.springframework.orm.hibernate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.spi.PersistenceUnitInfo;

import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

/**
 * Created by enietzel on 6/22/17.
 */
public class SakaiPersistenceUnitManager extends DefaultPersistenceUnitManager {

    private String defaultPersistenceUnitName = "sakai";
    private PersistenceUnitInfo defaultPersistenceUnitInfo;

    @Override
    public void preparePersistenceUnitInfos() {
        MutablePersistenceUnitInfo pui = new MutablePersistenceUnitInfo();
        pui.setPersistenceUnitName(defaultPersistenceUnitName);
        pui.setExcludeUnlistedClasses(true);

        if (pui.getJtaDataSource() == null) {
            pui.setJtaDataSource(getDefaultJtaDataSource());
        }
        if (pui.getNonJtaDataSource() == null) {
            pui.setNonJtaDataSource(getDefaultDataSource());
        }

        postProcessPersistenceUnitInfo(pui);

        defaultPersistenceUnitInfo = pui;
    }

    @Override
    public PersistenceUnitInfo obtainDefaultPersistenceUnitInfo() {
        return defaultPersistenceUnitInfo;
    }
}

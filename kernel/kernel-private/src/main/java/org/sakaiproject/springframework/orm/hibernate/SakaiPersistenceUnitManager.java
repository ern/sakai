package org.sakaiproject.springframework.orm.hibernate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import lombok.Setter;
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

/**
 * Created by enietzel on 6/22/17.
 */
public class SakaiPersistenceUnitManager extends DefaultPersistenceUnitManager {

    private String defaultPersistenceUnitName = "sakai";
    private PersistenceUnitInfo defaultPersistenceUnitInfo;
    @Setter private DataSource dataSource;
    @Setter private DataSource jtaDataSource;

    @Override
    public void preparePersistenceUnitInfos() {
        MutablePersistenceUnitInfo pui = new MutablePersistenceUnitInfo();
        pui.setPersistenceUnitName(defaultPersistenceUnitName);
        pui.setExcludeUnlistedClasses(true);

        if (pui.getJtaDataSource() == null) {
            pui.setJtaDataSource(jtaDataSource);
        }
        if (pui.getNonJtaDataSource() == null) {
            pui.setNonJtaDataSource(dataSource);
        }

        postProcessPersistenceUnitInfo(pui);

        defaultPersistenceUnitInfo = pui;
    }

    @Override
    public PersistenceUnitInfo obtainDefaultPersistenceUnitInfo() {
        return defaultPersistenceUnitInfo;
    }
}

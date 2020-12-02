/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/PermissionLevelManagerImpl.java $
 * $Id: PermissionLevelManagerImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.messageforums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.FetchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PermissionLevel;
import org.sakaiproject.api.app.messageforums.PermissionLevelManager;
import org.sakaiproject.api.app.messageforums.PermissionsMask;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DBMembershipItemImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PermissionLevelImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.TopicImpl;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.hibernate.HibernateCriterionUtils;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PermissionLevelManagerImpl extends HibernateDaoSupport implements PermissionLevelManager {

	@Setter private AreaManager areaManager;
	@Setter private EventTrackingService eventTrackingService;
	@Setter private IdManager idManager;
	@Setter private SessionManager sessionManager;
	@Setter private TransactionTemplate transactionTemplate;
	@Setter private MessageForumsTypeManager typeManager;

	private final Map<String, PermissionLevel> defaultPermissionsMap = new HashMap<>();

	public void init(){
		log.info("init()");
		transactionTemplate.executeWithoutResult(transactionStatus -> loadDefaultTypeAndPermissionLevelData());
	}
	
	public PermissionLevel getPermissionLevelByName(String name){
		if (log.isDebugEnabled()){
			log.debug("getPermissionLevelByName executing(" + name + ")");
		}
		
		if (PERMISSION_LEVEL_NAME_OWNER.equals(name)){
			return getDefaultOwnerPermissionLevel();
		}
		else if (PERMISSION_LEVEL_NAME_AUTHOR.equals(name)){
			return getDefaultAuthorPermissionLevel();
		}
		else if (PERMISSION_LEVEL_NAME_NONEDITING_AUTHOR.equals(name)){
			return getDefaultNoneditingAuthorPermissionLevel();
		}
		else if (PERMISSION_LEVEL_NAME_CONTRIBUTOR.equals(name)){
			return getDefaultContributorPermissionLevel();
		}
		else if (PERMISSION_LEVEL_NAME_REVIEWER.equals(name)){
			return getDefaultReviewerPermissionLevel();
		}
		else if (PERMISSION_LEVEL_NAME_NONE.equals(name)){
			return getDefaultNonePermissionLevel();
		}		
		else{
			return null;
		}
	}

	public List<String> getOrderedPermissionLevelNames() {
		log.debug("Get ordered permission levels");

		List<String> levelNames = new ArrayList<String>();

		List<PermissionLevel> levels = getDefaultPermissionLevels();
		if (levels != null && !levels.isEmpty()) {
			for (PermissionLevel level : levels) {
				levelNames.add(level.getName());
			}
			
			Collections.sort(levelNames);
		}

		return levelNames;
	}	
	
	public String getPermissionLevelType(PermissionLevel level){
		
		if (log.isDebugEnabled()){
			log.debug("getPermissionLevelType executing(" + level + ")");
		}
		
		if (level == null) {      
      throw new IllegalArgumentException("Null Argument");
		}
		
		PermissionLevel ownerLevel = getDefaultOwnerPermissionLevel();		
		if (level.equals(ownerLevel)){
			return ownerLevel.getTypeUuid();
		}
				
		PermissionLevel authorLevel = getDefaultAuthorPermissionLevel();		
		if (level.equals(authorLevel)){
			return authorLevel.getTypeUuid();
		}
		
		PermissionLevel noneditingAuthorLevel = getDefaultNoneditingAuthorPermissionLevel();		
		if (level.equals(noneditingAuthorLevel)){
			return noneditingAuthorLevel.getTypeUuid();
		}
				
	  PermissionLevel reviewerLevel = getDefaultReviewerPermissionLevel();
	  if (level.equals(reviewerLevel)){
			return reviewerLevel.getTypeUuid();
		}
	  	  
		PermissionLevel contributorLevel = getDefaultContributorPermissionLevel();
		if (level.equals(contributorLevel)){
			return contributorLevel.getTypeUuid();
		}
				
		PermissionLevel noneLevel = getDefaultNonePermissionLevel();
		if (level.equals(noneLevel)){
			return noneLevel.getTypeUuid();
		}
		
		return null;
	}

	public PermissionLevel createPermissionLevel(String name, String typeUuid, PermissionsMask mask){
		
		if (log.isDebugEnabled()){
			log.debug("createPermissionLevel executing(" + name + "," + typeUuid + "," + mask + ")");
		}
		
		if (name == null || typeUuid == null || mask == null) {      
      throw new IllegalArgumentException("Null Argument");
		}

		String currentUser = getCurrentUser();
		if (currentUser == null) {
			currentUser = typeManager.getCreatedBy(typeUuid);
		}

		PermissionLevel newPermissionLevel = new PermissionLevelImpl();
		Date now = new Date();
		newPermissionLevel.setName(name);
		newPermissionLevel.setUuid(idManager.createUuid());
		newPermissionLevel.setCreated(now);
		newPermissionLevel.setCreatedBy(currentUser);
		newPermissionLevel.setModified(now);
		newPermissionLevel.setModifiedBy(currentUser);
		newPermissionLevel.setTypeUuid(typeUuid);
			
		// set permission properties using reflection
		for (Iterator<Entry<String, Boolean>> i = mask.entrySet().iterator(); i.hasNext();){
			Entry<String, Boolean> entry = i.next();
 			String key = entry.getKey();
			Boolean value = entry.getValue();
			try{
			  PropertyUtils.setSimpleProperty(newPermissionLevel, key, value);
			}
			catch (Exception e){
				throw new RuntimeException(e);
			}
		}										
				
		return newPermissionLevel;		
	}
	
  public DBMembershipItem createDBMembershipItem(String name, String permissionLevelName, Integer type){
		
		if (log.isDebugEnabled()){
			log.debug("createDBMembershipItem executing(" + name + "," + type + ")");
		}
		
		if (name == null || type == null) {      
      throw new IllegalArgumentException("Null Argument");
		}
								
		DBMembershipItem newDBMembershipItem = new DBMembershipItemImpl();
		Date now = new Date();
		String currentUser = getCurrentUser();
		newDBMembershipItem.setName(name);
		newDBMembershipItem.setPermissionLevelName(permissionLevelName);
		newDBMembershipItem.setUuid(idManager.createUuid());
		newDBMembershipItem.setCreated(now);
		newDBMembershipItem.setCreatedBy(currentUser);
		newDBMembershipItem.setModified(now);
		newDBMembershipItem.setModifiedBy(currentUser);
		newDBMembershipItem.setType(type);
															
		return newDBMembershipItem;		
	}
  
  public DBMembershipItem saveDBMembershipItem(DBMembershipItem item, PermissionsMask mask){
		if (item != null) {

			// we never change the default but instead create a new one with the changes
			PermissionLevel level = item.getPermissionLevel();
			PermissionLevel defaultPermissionLevel = getDefaultPermissionLevel(level.getTypeUuid());

			// TODO we need to sort out when something needs to be saved
			if (defaultPermissionLevel != null) {
				// TODO thinking of using the PermissionMask to compare permissions
				PermissionsMask defaultPermissionMask = createMaskFromPermissionLevel(defaultPermissionLevel);

				// TODO if the permission is no longer the same as the default we need to create a new one and remove the default from the list
				// TODO if the permission is a custom and were switching back to the default then delete the custom and add default back to list
				// TODO need a way to prevent changes to default permissions
				// if (defaultPermissionLevel.getName().equals(level.getName()) &&


			}
			return (DBMembershipItem) getSessionFactory().getCurrentSession().merge(item);
		}
		return null;
  }
  
  public PermissionLevel savePermissionLevel(PermissionLevel level) {
		if (level != null) {
			// TODO need to prevent changes to default permissions levels
			return (PermissionLevel) getSessionFactory().getCurrentSession().merge(level);
		}
		return null;
  }
	
  public PermissionLevel getDefaultOwnerPermissionLevel(){

	  if (log.isDebugEnabled()){
		  log.debug("getDefaultOwnerPermissionLevel executing");
	  }

	  String typeUuid = typeManager.getOwnerLevelType();

	  if (typeUuid == null) {      
		  throw new IllegalStateException("type cannot be null");
	  }		
	  PermissionLevel level = getDefaultPermissionLevel(typeUuid);

	  if(level == null)
	  {    

		  log.info("No permission level data exists for the Owner level in the MFR_PERMISSION_LEVEL_T table. " +
				  "Default Owner permissions will be used. If you want to customize this permission level, use " +
				  "mfr.sql as a reference to add this level to the table.");

		  // return the default owner permission
		  PermissionsMask mask = getDefaultOwnerPermissionsMask();
		  level = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_OWNER, typeUuid, mask);
	  }
		  
	  return level;
  }

  public PermissionLevel getDefaultAuthorPermissionLevel(){

	  if (log.isDebugEnabled()){
		  log.debug("getDefaultAuthorPermissionLevel executing");
	  }

	  String typeUuid = typeManager.getAuthorLevelType();

	  if (typeUuid == null) {      
		  throw new IllegalStateException("type cannot be null");
	  }		
	  PermissionLevel level = getDefaultPermissionLevel(typeUuid);

	  if(level == null)
	  {
		  log.info("No permission level data exists for the Author level in the MFR_PERMISSION_LEVEL_T table. " +
                  "Default Author permissions will be used. If you want to customize this permission level, use " +
                  "mfr.sql as a reference to add this level to the table.");

		  // return the default author permission
		  PermissionsMask mask = getDefaultAuthorPermissionsMask();
		  level = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_AUTHOR, typeUuid, mask);
	  }

	  return level;
  }

  public PermissionLevel getDefaultNoneditingAuthorPermissionLevel(){

	  if (log.isDebugEnabled()){
		  log.debug("getDefaultNoneditingAuthorPermissionLevel executing");
	  }

	  String typeUuid = typeManager.getNoneditingAuthorLevelType();

	  if (typeUuid == null) {      
		  throw new IllegalStateException("type cannot be null");
	  }		
	  PermissionLevel level = getDefaultPermissionLevel(typeUuid);

	  if(level == null)
	  {
		  log.info("No permission level data exists for the NoneditingAuthor level in the MFR_PERMISSION_LEVEL_T table. " +
                  "Default NoneditingAuthor permissions will be used. If you want to customize this permission level, use " +
                  "mfr.sql as a reference to add this level to the table.");
		  
		  // return the default nonediting author permission
		  PermissionsMask mask = getDefaultNoneditingAuthorPermissionsMask();
		  level = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_NONEDITING_AUTHOR, typeUuid, mask);

	  }
	  
	  return level;
  }

  public PermissionLevel getDefaultReviewerPermissionLevel(){

	  if (log.isDebugEnabled()){
		  log.debug("getDefaultReviewerPermissionLevel executing");
	  }

	  String typeUuid = typeManager.getReviewerLevelType();

	  if (typeUuid == null) {      
		  throw new IllegalStateException("type cannot be null");
	  }		
	  PermissionLevel level = getDefaultPermissionLevel(typeUuid);

	  if(level == null)
	  {
		  log.info("No permission level data exists for the Reviewer level in the MFR_PERMISSION_LEVEL_T table. " +
                  "Default Reviewer permissions will be used. If you want to customize this permission level, use " +
                  "mfr.sql as a reference to add this level to the table.");
		  
		  // return the default reviewer permission
		  PermissionsMask mask = getDefaultReviewerPermissionsMask();
		  level = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_REVIEWER, typeUuid, mask);

	  }
	  
	  return level;
  }

  public PermissionLevel getDefaultContributorPermissionLevel(){

	  if (log.isDebugEnabled()){
		  log.debug("getDefaultContributorPermissionLevel executing");
	  }

	  String typeUuid = typeManager.getContributorLevelType();

	  if (typeUuid == null) {      
		  throw new IllegalStateException("type cannot be null");
	  }		
	  PermissionLevel level = getDefaultPermissionLevel(typeUuid);

	  if(level == null)
	  {
		  log.info("No permission level data exists for the Contributor level in the MFR_PERMISSION_LEVEL_T table. " +
                  "Default Contributor permissions will be used. If you want to customize this permission level, use " +
                  "mfr.sql as a reference to add this level to the table.");
		  
		  // return the default contributor permission
		  PermissionsMask mask = getDefaultContributorPermissionsMask();
		  level = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_CONTRIBUTOR, typeUuid, mask);

	  }

	  return level;	
  }

  public PermissionLevel getDefaultNonePermissionLevel(){

	  if (log.isDebugEnabled()){
		  log.debug("getDefaultNonePermissionLevel executing");
	  }

	  String typeUuid = typeManager.getNoneLevelType();

	  if (typeUuid == null) {      
		  throw new IllegalStateException("type cannot be null");
	  }		
	  PermissionLevel level = getDefaultPermissionLevel(typeUuid);

	  if(level == null)
	  {    
		  log.info("No permission level data exists for the None level in the MFR_PERMISSION_LEVEL_T table. " +
                  "Default None permissions will be used. If you want to customize this permission level, use " +
                  "mfr.sql as a reference to add this level to the table.");
		  
		// return the default None permission
		  PermissionsMask mask = getDefaultNonePermissionsMask();
		  level = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_NONE, typeUuid, mask);

	  }
	
	  return level;
  }
  	
  /**
   * 
   * @param typeUuid
   * @return the PermissionLevel for the given typeUuid. Returns null if no
   * PermissionLevel found.
   */
  private PermissionLevel getDefaultPermissionLevel(final String typeUuid){

	  if (typeUuid == null) {      
		  throw new IllegalArgumentException("Null Argument");
	  }

	  log.debug("Fetch Permission Level with typeUuid: {}", typeUuid);

	  PermissionLevel level = defaultPermissionsMap.get(typeUuid);

	  if (level == null) {
		  // retrieve it from the table
		  // <![CDATA[from org.sakaiproject.component.app.messageforums.dao.hibernate.PermissionLevelImpl as pli where pli.typeUuid = :typeUuid]]>
		  level = getHibernateTemplate().execute(session -> (PermissionLevelImpl) session.createCriteria(PermissionLevelImpl.class)
				  .add(Restrictions.eq("typeUuid", typeUuid))
				  .uniqueResult());

		  defaultPermissionsMap.put(typeUuid, level);
		  log.debug("Returned Permission Level from query was {}", level);
	  }

	  return level;

  }	
	
	public Boolean getCustomPermissionByName(String customPermName, PermissionLevel permissionLevel) {
    	if (customPermName == null) 
    		throw new IllegalArgumentException("Null permissionLevelName passed");
    	if (permissionLevel == null)
    		throw new IllegalArgumentException("Null permissionLevel passed");
    		  
    	if (customPermName.equals(PermissionLevel.NEW_FORUM))
    		return permissionLevel.getNewForum();
    	else if (customPermName.equals(PermissionLevel.NEW_RESPONSE))
    		return permissionLevel.getNewResponse();
    	else if (customPermName.equals(PermissionLevel.NEW_RESPONSE_TO_RESPONSE))
    		return permissionLevel.getNewResponseToResponse();
    	else if (customPermName.equals(PermissionLevel.NEW_TOPIC))
    		return permissionLevel.getNewTopic();
    	else if (customPermName.equals(PermissionLevel.POST_TO_GRADEBOOK))
    		return permissionLevel.getPostToGradebook();
    	else if (customPermName.equals(PermissionLevel.DELETE_ANY))
    		return permissionLevel.getDeleteAny();
    	else if (customPermName.equals(PermissionLevel.DELETE_OWN))
    		return permissionLevel.getDeleteOwn();
    	else if (customPermName.equals(PermissionLevel.MARK_AS_READ))
    		return permissionLevel.getMarkAsRead();
    	else if (customPermName.equals(PermissionLevel.MODERATE_POSTINGS))
    		return permissionLevel.getModeratePostings();
    	else if (customPermName.equals(PermissionLevel.IDENTIFY_ANON_AUTHORS))
    	{
    		return permissionLevel.getIdentifyAnonAuthors();
    	}
    	else if (customPermName.equals(PermissionLevel.MOVE_POSTING))
    		return permissionLevel.getMovePosting();
    	else if (customPermName.equals(PermissionLevel.READ))
    		return permissionLevel.getRead();
    	else if (customPermName.equals(PermissionLevel.REVISE_ANY))
    		return permissionLevel.getReviseAny();
    	else if (customPermName.equals(PermissionLevel.REVISE_OWN))
    		return permissionLevel.getReviseOwn();
    	else if (customPermName.equals(PermissionLevel.CHANGE_SETTINGS))
    		return permissionLevel.getChangeSettings();
    	else 
    		return null;
    }
	
	public List<String> getCustomPermissions() {
		List<String> customPerms = new ArrayList<>();
		customPerms.add(PermissionLevel.NEW_FORUM);
		customPerms.add(PermissionLevel.NEW_RESPONSE);
		customPerms.add(PermissionLevel.NEW_RESPONSE_TO_RESPONSE);
		customPerms.add(PermissionLevel.NEW_TOPIC);
		customPerms.add(PermissionLevel.DELETE_ANY);
		customPerms.add(PermissionLevel.DELETE_OWN);
		customPerms.add(PermissionLevel.MARK_AS_READ);
		customPerms.add(PermissionLevel.MODERATE_POSTINGS);
		customPerms.add(PermissionLevel.IDENTIFY_ANON_AUTHORS);
		customPerms.add(PermissionLevel.MOVE_POSTING);
		customPerms.add(PermissionLevel.POST_TO_GRADEBOOK);
		customPerms.add(PermissionLevel.READ);
		customPerms.add(PermissionLevel.REVISE_ANY);
		customPerms.add(PermissionLevel.REVISE_OWN);
		customPerms.add(PermissionLevel.CHANGE_SETTINGS);
		
		return customPerms;
	}
    
    
	
	private String getCurrentUser() {    
		return sessionManager.getCurrentSessionUserId();
  }

	public Set<DBMembershipItem> getAllMembershipItemsForForumsForSite(final Long areaId) {
		List<DBMembershipItem> list = getHibernateTemplate().execute(session ->
				session.createCriteria(DBMembershipItemImpl.class, "m")
						.createAlias("m.forum", "f")
						.createAlias("f.area", "a")
						.setFetchMode("m.permissionLevel", FetchMode.JOIN)
						.add(Restrictions.eq("a.id", areaId))
						.list());
		return new HashSet<>(list != null ? list : Collections.emptyList());
	}

	private List<Long> getAllTopicsForSite(final Long areaId) {
        List<Long> ids = getHibernateTemplate().execute(session ->
                session.createCriteria(TopicImpl.class, "t")
						.setProjection(Projections.property("id"))
                        .createAlias("t.openForum", "f")
                        .createAlias("f.area", "a")
                        .add(Restrictions.eq("a.id", areaId))
                        .list());

        return ids != null ? ids : Collections.emptyList();
    }

    public Set<DBMembershipItem> getAllMembershipItemsForTopicsForSite(final Long areaId) {
        final List<Long> topicIds = getAllTopicsForSite(areaId);

        if (!topicIds.isEmpty()) {
				List<DBMembershipItem> list = getHibernateTemplate().execute(session ->
						session.createCriteria(DBMembershipItemImpl.class, "m")
								.createAlias("m.topic", "t")
								.setFetchMode("m.permissionLevel", FetchMode.JOIN)
								.add(HibernateCriterionUtils.CriterionInRestrictionSplitter("t.id", topicIds))
								.list());
				return new HashSet<>(list != null ? list : Collections.emptyList());
		}
        return Collections.emptySet();
    }

	private void loadDefaultTypeAndPermissionLevelData() {
		try {
			// first, call the methods that will load type data if it is missing
			String ownerType = typeManager.getOwnerLevelType();
			String authorType = typeManager.getAuthorLevelType();
			String contributorType = typeManager.getContributorLevelType();
			String reviewerType = typeManager.getReviewerLevelType();
			String noneditingAuthorType = typeManager.getNoneditingAuthorLevelType();
			String noneType = typeManager.getNoneLevelType();

			// now let's check to see if we need to add the default permission level
			// data
			if (getDefaultPermissionLevel(ownerType) == null) {
				PermissionsMask mask = getDefaultOwnerPermissionsMask();
				PermissionLevel permLevel = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_OWNER, ownerType, mask);
				savePermissionLevel(permLevel);
				defaultPermissionsMap.put(permLevel.getTypeUuid(), permLevel);
			}

			if (getDefaultPermissionLevel(authorType) == null) {
				PermissionsMask mask = getDefaultAuthorPermissionsMask();
				PermissionLevel permLevel = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_AUTHOR, authorType, mask);
				savePermissionLevel(permLevel);
				defaultPermissionsMap.put(permLevel.getTypeUuid(), permLevel);
			}

			if (getDefaultPermissionLevel(contributorType) == null) {
				PermissionsMask mask = getDefaultContributorPermissionsMask();
				PermissionLevel permLevel = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_CONTRIBUTOR, contributorType, mask);
				savePermissionLevel(permLevel);
				defaultPermissionsMap.put(permLevel.getTypeUuid(), permLevel);
			}

			if (getDefaultPermissionLevel(reviewerType) == null) {
				PermissionsMask mask = getDefaultReviewerPermissionsMask();
				PermissionLevel permLevel = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_REVIEWER, reviewerType, mask);
				savePermissionLevel(permLevel);
				defaultPermissionsMap.put(permLevel.getTypeUuid(), permLevel);
			}

			if (getDefaultPermissionLevel(noneditingAuthorType) == null) {
				PermissionsMask mask = getDefaultNoneditingAuthorPermissionsMask();
				PermissionLevel permLevel = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_NONEDITING_AUTHOR, noneditingAuthorType, mask);
				savePermissionLevel(permLevel);
				defaultPermissionsMap.put(permLevel.getTypeUuid(), permLevel);
			}

			if (getDefaultPermissionLevel(noneType) == null) {
				PermissionsMask mask = getDefaultNonePermissionsMask();
				PermissionLevel permLevel = createPermissionLevel(PermissionLevelManager.PERMISSION_LEVEL_NAME_NONE, noneType, mask);
				savePermissionLevel(permLevel);
				defaultPermissionsMap.put(permLevel.getTypeUuid(), permLevel);
			}
		} catch (Exception e) {
			log.warn("Error loading initial default types and/or permissions", e);
		}
	}

	private PermissionsMask getDefaultOwnerPermissionsMask() {
		PermissionsMask mask = new PermissionsMask();                
		  mask.put(PermissionLevel.NEW_FORUM, Boolean.TRUE);
		  mask.put(PermissionLevel.NEW_TOPIC, Boolean.TRUE);
		  mask.put(PermissionLevel.NEW_RESPONSE, Boolean.TRUE);
		  mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.TRUE);
		  mask.put(PermissionLevel.MOVE_POSTING, Boolean.TRUE);
		  mask.put(PermissionLevel.CHANGE_SETTINGS, Boolean.TRUE);
		  mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.TRUE);
		  mask.put(PermissionLevel.READ, Boolean.TRUE);
		  mask.put(PermissionLevel.MARK_AS_READ, Boolean.TRUE);
		  mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.TRUE);
		  mask.put(PermissionLevel.IDENTIFY_ANON_AUTHORS, Boolean.TRUE);
		  mask.put(PermissionLevel.DELETE_OWN, Boolean.FALSE);
		  mask.put(PermissionLevel.DELETE_ANY, Boolean.TRUE);
		  mask.put(PermissionLevel.REVISE_OWN, Boolean.FALSE);
		  mask.put(PermissionLevel.REVISE_ANY, Boolean.TRUE);
		  
		  return mask;
	}
	
	private PermissionsMask getDefaultAuthorPermissionsMask() {
		PermissionsMask mask = new PermissionsMask();                
		  mask.put(PermissionLevel.NEW_FORUM, Boolean.TRUE);
		  mask.put(PermissionLevel.NEW_TOPIC, Boolean.TRUE);
		  mask.put(PermissionLevel.NEW_RESPONSE, Boolean.TRUE);
		  mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.TRUE);
		  mask.put(PermissionLevel.MOVE_POSTING, Boolean.TRUE);
		  mask.put(PermissionLevel.CHANGE_SETTINGS, Boolean.TRUE);
		  mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.TRUE);
		  mask.put(PermissionLevel.READ, Boolean.TRUE);
		  mask.put(PermissionLevel.MARK_AS_READ, Boolean.TRUE);
		  mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.FALSE);
		  mask.put(PermissionLevel.IDENTIFY_ANON_AUTHORS, Boolean.FALSE);
		  mask.put(PermissionLevel.DELETE_OWN, Boolean.TRUE);
		  mask.put(PermissionLevel.DELETE_ANY, Boolean.FALSE);
		  mask.put(PermissionLevel.REVISE_OWN, Boolean.TRUE);
		  mask.put(PermissionLevel.REVISE_ANY, Boolean.FALSE);
		  
		  return mask;
	}
	
	private PermissionsMask getDefaultContributorPermissionsMask() {
		PermissionsMask mask = new PermissionsMask();                
		  mask.put(PermissionLevel.NEW_FORUM, Boolean.FALSE);
		  mask.put(PermissionLevel.NEW_TOPIC, Boolean.FALSE);
		  mask.put(PermissionLevel.NEW_RESPONSE, Boolean.TRUE);
		  mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.TRUE);
		  mask.put(PermissionLevel.MOVE_POSTING, Boolean.FALSE);
		  mask.put(PermissionLevel.CHANGE_SETTINGS, Boolean.FALSE);
		  mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.FALSE);
		  mask.put(PermissionLevel.READ, Boolean.TRUE);
		  mask.put(PermissionLevel.MARK_AS_READ, Boolean.TRUE);
		  mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.FALSE);
		  mask.put(PermissionLevel.IDENTIFY_ANON_AUTHORS, Boolean.FALSE);
		  mask.put(PermissionLevel.DELETE_OWN, Boolean.FALSE);
		  mask.put(PermissionLevel.DELETE_ANY, Boolean.FALSE);
		  mask.put(PermissionLevel.REVISE_OWN, Boolean.FALSE);
		  mask.put(PermissionLevel.REVISE_ANY, Boolean.FALSE);
		  
		  return mask;
	}
	
	private PermissionsMask getDefaultNoneditingAuthorPermissionsMask() {
		PermissionsMask mask = new PermissionsMask();                
		  mask.put(PermissionLevel.NEW_FORUM, Boolean.TRUE);
		  mask.put(PermissionLevel.NEW_TOPIC, Boolean.TRUE);
		  mask.put(PermissionLevel.NEW_RESPONSE, Boolean.TRUE);
		  mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.TRUE);
		  mask.put(PermissionLevel.MOVE_POSTING, Boolean.FALSE);
		  mask.put(PermissionLevel.CHANGE_SETTINGS, Boolean.TRUE);
		  mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.TRUE);
		  mask.put(PermissionLevel.READ, Boolean.TRUE);
		  mask.put(PermissionLevel.MARK_AS_READ, Boolean.TRUE);
		  mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.FALSE);
		  mask.put(PermissionLevel.IDENTIFY_ANON_AUTHORS, Boolean.FALSE);
		  mask.put(PermissionLevel.DELETE_OWN, Boolean.FALSE);
		  mask.put(PermissionLevel.DELETE_ANY, Boolean.FALSE);
		  mask.put(PermissionLevel.REVISE_OWN, Boolean.TRUE);
		  mask.put(PermissionLevel.REVISE_ANY, Boolean.FALSE);
		  
		  return mask;
	}
	
	private PermissionsMask getDefaultNonePermissionsMask() {
		  PermissionsMask mask = new PermissionsMask();                
		  mask.put(PermissionLevel.NEW_FORUM, Boolean.FALSE);
		  mask.put(PermissionLevel.NEW_TOPIC, Boolean.FALSE);
		  mask.put(PermissionLevel.NEW_RESPONSE, Boolean.FALSE);
		  mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.FALSE);
		  mask.put(PermissionLevel.MOVE_POSTING, Boolean.FALSE);
		  mask.put(PermissionLevel.CHANGE_SETTINGS, Boolean.FALSE);
		  mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.FALSE);
		  mask.put(PermissionLevel.READ, Boolean.FALSE);
		  mask.put(PermissionLevel.MARK_AS_READ, Boolean.FALSE);
		  mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.FALSE);
		  mask.put(PermissionLevel.IDENTIFY_ANON_AUTHORS, Boolean.FALSE);
		  mask.put(PermissionLevel.DELETE_OWN, Boolean.FALSE);
		  mask.put(PermissionLevel.DELETE_ANY, Boolean.FALSE);
		  mask.put(PermissionLevel.REVISE_OWN, Boolean.FALSE);
		  mask.put(PermissionLevel.REVISE_ANY, Boolean.FALSE);
		  
		  return mask;
	}
	
	private PermissionsMask getDefaultReviewerPermissionsMask() {
		PermissionsMask mask = new PermissionsMask();                
		  mask.put(PermissionLevel.NEW_FORUM, Boolean.FALSE);
		  mask.put(PermissionLevel.NEW_TOPIC, Boolean.FALSE);
		  mask.put(PermissionLevel.NEW_RESPONSE, Boolean.FALSE);
		  mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, Boolean.FALSE);
		  mask.put(PermissionLevel.MOVE_POSTING, Boolean.FALSE);
		  mask.put(PermissionLevel.CHANGE_SETTINGS, Boolean.FALSE);
		  mask.put(PermissionLevel.POST_TO_GRADEBOOK, Boolean.FALSE);
		  mask.put(PermissionLevel.READ, Boolean.TRUE);
		  mask.put(PermissionLevel.MARK_AS_READ, Boolean.TRUE);
		  mask.put(PermissionLevel.MODERATE_POSTINGS, Boolean.FALSE);
		  mask.put(PermissionLevel.IDENTIFY_ANON_AUTHORS, Boolean.FALSE);
		  mask.put(PermissionLevel.DELETE_OWN, Boolean.FALSE);
		  mask.put(PermissionLevel.DELETE_ANY, Boolean.FALSE);
		  mask.put(PermissionLevel.REVISE_OWN, Boolean.FALSE);
		  mask.put(PermissionLevel.REVISE_ANY, Boolean.FALSE);
		  
		  return mask;
	}
	
	public List<PermissionLevel> getDefaultPermissionLevels() {
		// first, check for the levels in the map. if map is null,
		// return the default permission level data
		List<PermissionLevel> defaultLevels = new ArrayList<PermissionLevel>();
		if (defaultPermissionsMap != null && !defaultPermissionsMap.isEmpty()) {
			defaultLevels.addAll(defaultPermissionsMap.values());
		} else {
			if (log.isDebugEnabled()) log.debug("Default permissions map was null!! Loading defaults to return from getDefaultPermissionLevels");
			defaultLevels.add(getDefaultOwnerPermissionLevel());
			defaultLevels.add(getDefaultAuthorPermissionLevel());
			defaultLevels.add(getDefaultContributorPermissionLevel());
			defaultLevels.add(getDefaultNoneditingAuthorPermissionLevel());
			defaultLevels.add(getDefaultNonePermissionLevel());
			defaultLevels.add(getDefaultReviewerPermissionLevel());
		}
		
		return defaultLevels;
	}

	private PermissionsMask createMaskFromPermissionLevel(PermissionLevel level) {
		PermissionsMask mask = new PermissionsMask();
		mask.put(PermissionLevel.NEW_FORUM, level.getNewForum());
		mask.put(PermissionLevel.NEW_TOPIC, level.getNewTopic());
		mask.put(PermissionLevel.NEW_RESPONSE, level.getNewResponse());
		mask.put(PermissionLevel.NEW_RESPONSE_TO_RESPONSE, level.getNewResponseToResponse());
		mask.put(PermissionLevel.MOVE_POSTING, level.getMovePosting());
		mask.put(PermissionLevel.CHANGE_SETTINGS, level.getChangeSettings());
		mask.put(PermissionLevel.POST_TO_GRADEBOOK, level.getPostToGradebook());
		mask.put(PermissionLevel.READ, level.getRead());
		mask.put(PermissionLevel.MARK_AS_READ, level.getMarkAsRead());
		mask.put(PermissionLevel.MODERATE_POSTINGS, level.getModeratePostings());
		mask.put(PermissionLevel.IDENTIFY_ANON_AUTHORS, level.getIdentifyAnonAuthors());
		mask.put(PermissionLevel.DELETE_OWN, level.getDeleteOwn());
		mask.put(PermissionLevel.DELETE_ANY, level.getDeleteAny());
		mask.put(PermissionLevel.REVISE_OWN, level.getReviseOwn());
		mask.put(PermissionLevel.REVISE_ANY, level.getReviseAny());
		return mask;
	}

}

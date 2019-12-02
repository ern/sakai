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

package org.sakaiproject.rubrics.logic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.rubrics.logic.model.*;
import org.sakaiproject.rubrics.logic.repository.EvaluationRepository;
import org.sakaiproject.rubrics.logic.repository.RubricRepository;
import org.sakaiproject.rubrics.logic.repository.ToolItemRubricAssociationRepository;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * Implementation of {@link RubricsService}
 */
@Slf4j
public class RubricsServiceImpl implements RubricsService, EntityProducer, EntityTransferrer {

    protected static ResourceLoader rb = new ResourceLoader("org.sakaiproject.rubrics.bundle.Messages");

    private static final String RBCS_PERMISSIONS_EVALUATOR = "rubrics.evaluator";
    private static final String RBCS_PERMISSIONS_EDITOR = "rubrics.editor";
    private static final String RBCS_PERMISSIONS_EVALUEE = "rubrics.evaluee";
    private static final String RBCS_PERMISSIONS_ASSOCIATOR = "rubrics.associator";
    private static final String RBCS_PERMISSIONS_SUPERUSER = "rubrics.superuser";

    private static final String RBCS_SERVICE_URL_PREFIX = "/rubrics-service/rest/";

    private static final String SITE_CONTEXT_TYPE = "site";

    @Setter private AuthzGroupService authzGroupService;
    @Setter private EntityManager entityManager;
    @Setter private EvaluationRepository evaluationRepository;
    @Setter private EventTrackingService eventTrackingService;
    @Setter private FunctionManager functionManager;
    @Setter private MemoryService memoryService;
    @Setter private RubricRepository rubricRepository;
    @Setter private SecurityService securityService;
    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SessionManager sessionManager;
    @Setter private SiteService siteService;
    @Setter private ToolItemRubricAssociationRepository associationRepository;
    @Setter private ToolManager toolManager;
    @Setter private UserDirectoryService userDirectoryService;

    private Cache<String, Boolean> hasAssociatedRubricCache;

    public void init() {

        // register as an entity producer
        entityManager.registerEntityProducer(this, REFERENCE_ROOT);

        functionManager.registerFunction(RBCS_PERMISSIONS_EVALUATOR);
        functionManager.registerFunction(RBCS_PERMISSIONS_EDITOR);
        functionManager.registerFunction(RBCS_PERMISSIONS_EVALUEE);
        functionManager.registerFunction(RBCS_PERMISSIONS_ASSOCIATOR);

        hasAssociatedRubricCache = memoryService.<String, Boolean>getCache("org.sakaiproject.rubrics.logic.hasAssociatedRubricCache");
    }

    private String getCurrentSiteId(String method) {

        if (toolManager.getCurrentPlacement() == null) {
            log.error("{}: current placement is null, Rubrics token won't be generated.", method);
            return null;
        }
        return toolManager.getCurrentPlacement().getContext();
    }

    public boolean hasAssociatedRubric(String tool, String id ) {

        String cacheKey = tool + "#" + id;
        Boolean isAssociated = hasAssociatedRubricCache.get(cacheKey);

        if (isAssociated != null) {
            return isAssociated;
        } else {
            boolean exists = false;
            try {
                Optional<ToolItemRubricAssociation> association = getRubricAssociation(tool, id);
                exists = association.isPresent();
                hasAssociatedRubricCache.put(cacheKey, exists);
            } catch (Exception e){
                log.debug("No previous association or rubrics not answering", e);
            }
            return exists;
        }
    }

    /**
     * call the rubrics-service to save the binding between assignment and rubric
     * @param params A hashmap with all the rbcs params comming from the component. The tool should generate it.
     * @param tool the tool id, something like "sakai.assignment"
     * @param id the id of the element to
     */
    public void saveRubricAssociation(String tool, String id, Map<String,String> params) {

        try {
            Optional<ToolItemRubricAssociation> optionalAssociation = getRubricAssociation(tool, id);

            //we will create a new one or update if the parameter rbcs-associate is true
            String nowTime = LocalDateTime.now().toString();
            if (params.get(RubricsConstants.RBCS_ASSOCIATE).equals("1")) {

                if (!optionalAssociation.isPresent()) {  // create a new one.
                    ToolItemRubricAssociation newAssociation = new ToolItemRubricAssociation();
                    newAssociation.setToolId(tool);
                    newAssociation.setItemId(id);
                    newAssociation.setRubricId(Long.valueOf(params.get(RubricsConstants.RBCS_LIST)));
                    Metadata metadata = new Metadata();
                    //metadata.setCreated(nowTime);
                    metadata.setOwnerId(userDirectoryService.getCurrentUser().getId());
                    newAssociation.setMetadata(metadata);
					newAssociation.setParameters(getConfigurationParameters(params, optionalAssociation.get().getParameters()));
                    associationRepository.save(newAssociation);
                } else {
                    ToolItemRubricAssociation association = optionalAssociation.get();
                    String created = association.getMetadata().getCreated().toString();
                    String owner = association.getMetadata().getOwnerId();
                    String ownerType = association.getMetadata().getOwnerType();
                    String creatorId = association.getMetadata().getCreatorId();
                    Map<String, Boolean> oldParams = association.getParameters();
                    Long oldRubricId = association.getRubricId();
                    association.setToolId(tool);
                    association.setItemId(id);
                    association.setRubricId(Long.valueOf(params.get(RubricsConstants.RBCS_LIST)));
                    Metadata metadata = new Metadata();
                    //metadata.setCreated(created);
                    metadata.setOwnerId(owner);
                    metadata.setOwnerType(ownerType);
                    metadata.setCreatorId(creatorId);
                    association.setMetadata(metadata);
					association.setParameters(getConfigurationParameters(params, oldParams));
                    associationRepository.save(association);
                    if (!Long.valueOf(params.get(RubricsConstants.RBCS_LIST)).equals(oldRubricId)) {
                        deleteRubricEvaluationsForAssociation(association.getId());
                    }
                }
                hasAssociatedRubricCache.put(tool + "#" + id, true);
            } else {
                // We delete the association
                if (optionalAssociation.isPresent()) {
                    ToolItemRubricAssociation association = optionalAssociation.get();
                    deleteRubricEvaluationsForAssociation(association.getId());
                    associationRepository.delete(association.getId());
                    hasAssociatedRubricCache.remove(association.getToolId() + "#" + association.getItemId());
                }
            }
        } catch (Exception e) {
            //TODO If we have an error here, maybe we should return say something to the user
        }
    }

    public void saveRubricEvaluation(String toolId, String associatedItemId, String evaluatedItemId,
            String evaluatedItemOwnerId, String evaluatorId, Map<String,String> params) {

        try {
            // Check for an existing evaluation
            Evaluation existingEvaluation = null;

            try {
                List<Evaluation> evaluations
                    = evaluationRepository.findByToolIdAndAssociationItemIdAndEvaluatedItemId(toolId, associatedItemId, evaluatedItemId);

                // Should only be one matching this search criterion
                if (evaluations.size() > 1) {
                    throw new IllegalStateException("Number of evaluations greater than one for request");
                }

                existingEvaluation = evaluations.get(0);
            } catch (Exception ex){
                log.info("Exception on saveRubricEvaluation: " + ex.getMessage());
                //no previous evaluation
            }

            // Get the actual association (necessary to get the rubrics association resource for persisting the evaluation)
            Optional<ToolItemRubricAssociation> optionalAssociation = associationRepository.findByToolIdAndItemId(toolId, associatedItemId);

            if (optionalAssociation.isPresent()) {
                ToolItemRubricAssociation association = optionalAssociation.get();

                List<CriterionOutcome> outcomes = getCriterionOutcomes(associatedItemId, evaluatedItemId, params, association);

                if (existingEvaluation == null) { // Create a new one
                    Evaluation evaluation = new Evaluation();
                    evaluation.setEvaluatorId(evaluatorId);
                    evaluation.setEvaluatedItemId(evaluatedItemId);
                    evaluation.setEvaluatedItemOwnerId(evaluatedItemOwnerId);
                    evaluation.setToolItemRubricAssociation(association);
                    evaluation.setCriterionOutcomes(outcomes);
                    evaluationRepository.save(evaluation);
                } else { // Update existing evaluation
                    existingEvaluation.setEvaluatorId(evaluatorId);
                    existingEvaluation.setEvaluatedItemId(evaluatedItemId);
                    existingEvaluation.setEvaluatedItemOwnerId(evaluatedItemOwnerId);
                    existingEvaluation.setToolItemRubricAssociation(association);
                    existingEvaluation.setCriterionOutcomes(outcomes);
                    evaluationRepository.save(existingEvaluation);
                }
            }
        } catch (Exception e) {
            //TODO If we have an error here, maybe we should return say something to the user
            log.error("Error in SaveRubricEvaluation " + e.getMessage());
        }
    }

    //public void saveRubricEvaluation(String toolId, String associatedItemId, String evaluatedItemId,
     //       String evaluatedItemOwnerId, String evaluatorId, Map<String,String> params) {
    private List<CriterionOutcome> getCriterionOutcomes(String associatedItemId, String evaluatedItemId,
                                              Map<String,String> formPostParameters,
                                              ToolItemRubricAssociation association) throws Exception {

        Map<String, Map<String, String>> criterionDataMap = extractCriterionDataFromParams(formPostParameters);

        boolean pointsAdjusted = false;
        String points = null;
        String selectedRatingId = null;

        String siteId = formPostParameters.get("siteId");

        /*
        Map<String, Criterion> criterions = new HashMap<>();
        for (Criterion criterion : association.getRubric().getCriterions()) {
            criterions.put(String.valueOf(criterion.getId()), criterion);
        }
        */

        final Map<String, Criterion> criterions
            = association.getRubric().getCriterions().stream()
                .collect(Collectors.toMap(c -> String.valueOf(c.getId()), c -> c));

        List<CriterionOutcome> outcomes = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> criterionData : criterionDataMap.entrySet()) {
            final String selectedRatingPoints = criterionData.getValue().get(RubricsConstants.RBCS_PREFIX + evaluatedItemId + "-"+ associatedItemId + "-criterion");

            if (StringUtils.isNotBlank(criterionData.getValue().get(RubricsConstants.RBCS_PREFIX + evaluatedItemId + "-" + associatedItemId + "-criterion-override"))) {
                pointsAdjusted = true;
                points = criterionData.getValue().get(RubricsConstants.RBCS_PREFIX + evaluatedItemId + "-" + associatedItemId + "-criterion-override");
            } else {
                pointsAdjusted = false;
                points = selectedRatingPoints;
            }

            Criterion criterion = criterions.get(criterionData.getKey());
            Optional<Rating> rating = criterion.getRatings().stream().filter(c -> String.valueOf(c.getPoints()).equals(selectedRatingPoints)).findFirst();

            if (rating.isPresent()) {
                selectedRatingId =  String.valueOf(rating.get().getId());
            }

            if (StringUtils.isEmpty(points)){
                points = "0";
            }

            CriterionOutcome co = new CriterionOutcome();
            co.setCriterionId(Long.valueOf(criterionData.getKey()));
            co.setPoints(Integer.valueOf(points));
            co.setComments(StringEscapeUtils.escapeJson(criterionData.getValue().get(RubricsConstants.RBCS_PREFIX + evaluatedItemId + "-"+ associatedItemId + "-criterion-comment")));
            co.setPointsAdjusted(pointsAdjusted);
            co.setSelectedRatingId(Long.valueOf(selectedRatingId));
            outcomes.add(co);
        }

        return outcomes;
    }

    private Map<String, Map<String, String>> extractCriterionDataFromParams(Map<String, String> params) {

        Map<String, Map<String, String>> criterionDataMap = new HashMap<>();

        for (Map.Entry<String, String> param : params.entrySet()) {
            String possibleCriterionId = StringUtils.substringAfterLast(param.getKey(), "-");
            String criterionDataLabel = StringUtils.substringBeforeLast(param.getKey(), "-");
            if (StringUtils.isNumeric(possibleCriterionId)) {
                if (!criterionDataMap.containsKey(possibleCriterionId)) {
                    criterionDataMap.put(possibleCriterionId, new HashMap<>());
                }
                criterionDataMap.get(possibleCriterionId).put(criterionDataLabel, param.getValue());
            }
        }

        return criterionDataMap;
    }

    /**
     * Prepare the association params in json format
     * @param params the full list of rubrics params coming from the component
     * @return
     */

    private String setConfigurationParameters(Map<String,String> params, Map<String,Boolean> oldParams ){
        String configuration = "";
        Boolean noFirst=false;
        //Get the parameters
        Iterator it2 = params.keySet().iterator();
        while (it2.hasNext()) {
            String name = it2.next().toString();
            if (name.startsWith(RubricsConstants.RBCS_CONFIG)) {
                if (noFirst) {
                    configuration = configuration + " , ";
                }
                String value = "false";
                if ((params.get(name) != null) && (params.get(name).equals("1"))) {
                    value = "true";
                }
                configuration = configuration + "\"" + name.substring(12) + "\" : " + value;
                noFirst = true;
            }
        }
        Iterator itOld = oldParams.keySet().iterator();
        while (itOld.hasNext()) {
            String name = itOld.next().toString();
            if (!(params.containsKey(RubricsConstants.RBCS_CONFIG + name))) {
                if (noFirst) {
                    configuration = configuration + " , ";
                }
                configuration = configuration + "\"" + name + "\" : false";
                noFirst = true;
            }
        }
        log.debug(configuration);
        return configuration;
    }

    private Map<String, Boolean> getConfigurationParameters(Map<String,String> params, Map<String,Boolean> oldParams ){

        Map<String, Boolean> newParams = new HashMap<>();

        String fineTunePoints = params.get(RubricsService.CONFIG_FINE_TUNE_POINTS);
        if (!StringUtils.isEmpty(fineTunePoints)) {
            newParams.put(RubricsService.CONFIG_FINE_TUNE_POINTS, fineTunePoints.equals("1"));
        } else {
            newParams.put(RubricsService.CONFIG_FINE_TUNE_POINTS, oldParams.get(CONFIG_FINE_TUNE_POINTS));
        }
        String hideStudentPreview = params.get(RubricsService.CONFIG_HIDE_STUDENT_PREVIEW);
        if (!StringUtils.isEmpty(hideStudentPreview)) {
            newParams.put(RubricsService.CONFIG_HIDE_STUDENT_PREVIEW, hideStudentPreview.equals("1"));
        } else {
            newParams.put(RubricsService.CONFIG_FINE_TUNE_POINTS, oldParams.get(CONFIG_FINE_TUNE_POINTS));
        }

        return newParams;
    }

	/**
     * Returns the ToolItemRubricAssociation for the given tool and associated item ID, wrapped as an Optional.
     * @param toolId the tool id, something like "sakai.assignment"
     * @param associatedToolItemId the id of the associated element within the tool
     * @return
     */
    public Optional<ToolItemRubricAssociation> getRubricAssociation(String toolId, String associatedToolItemId) throws Exception {
        return associationRepository.findByToolIdAndItemId(toolId, associatedToolItemId);
    }

    //TODO generate a public String postRubricAssociation(String tool, String id, HashMap<String,String> params)

    public String getRubricEvaluationObjectId(String associationId, String userId) {

        try {
            return evaluationRepository.findByAssociationIdAndUserId(associationId, userId);
        } catch (Exception e) {
            log.warn("Error {} while getting a rubric evaluation in assignment {} for user {}", e.getMessage(), associationId, userId);
        }
        return null;
    }

    //TODO generate a public String putRubricAssociation(String tool, String id, HashMap<String,String> params)

    /**
     * Delete all the rubric associations starting with itemId.
     * @param itemId The formatted item id.
     */
	public void deleteRubricAssociationsByItemIdPrefix(String itemId, String toolId) {

		try {
            List<ToolItemRubricAssociation> associations
                = associationRepository.findByItemIdPrefix(toolId, itemId);

			for (ToolItemRubricAssociation association : associations) {
				deleteRubricEvaluationsForAssociation(association.getId());
				associationRepository.delete(association.getId());
				hasAssociatedRubricCache.remove(toolId + "#" + itemId);
			}
        } catch (Exception e) {
            log.warn("Error deleting rubric association for id {} : {}", itemId, e.getMessage());
        }
    }

    public void deleteRubricEvaluationsForAssociation(Long associationId){

        try {
            evaluationRepository.deleteByToolItemRubricAssociation(associationId);
        } catch (Exception e) {
            log.warn("Error deleting rubric association for association {} : {}", associationId, e.getMessage());
        }
	}

    public void deleteRubricAssociation(String tool, String id){

        try {
            Optional<ToolItemRubricAssociation> optionalAssociation = getRubricAssociation(tool, id);
            if (optionalAssociation.isPresent()) {
                ToolItemRubricAssociation association = optionalAssociation.get();
                deleteRubricEvaluationsForAssociation(association.getId());
                associationRepository.delete(association.getId());
                hasAssociatedRubricCache.remove(association.getToolId() + "#" + association.getItemId());
            }
        } catch (Exception e) {
            log.warn("Error deleting rubric association for tool {} and id {} : {}", tool, id, e.getMessage());
        }
    }

    public String generateLang(){

        StringBuilder lines = new StringBuilder();
        lines.append("var rubricsLang = {");

        Locale locale = rb.getLocale();
        Set properties = rb.keySet();
        lines.append("'" + locale.toLanguageTag() + "': {");
        Iterator keys = properties.iterator();
        while (keys.hasNext()){
            String key = keys.next().toString();
            if (keys.hasNext()) {
                lines.append("'" + key + "': '" + rb.getString(key) + "',");
            }else{
                lines.append("'" + key + "': '" + rb.getString(key) + "'");
            }
        }

        lines.append("}");
        lines.append("}");

        log.debug(lines.toString());

        return lines.toString();
    }

    public String getCurrentSessionId() {
        return sessionManager.getCurrentSession().getId();
    }

    @Override
    public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> options) {

        Map<String, String> transversalMap = new HashMap<>();
        try {
            for (Rubric rubric : rubricRepository.findBySiteId(fromContext)) {
                String newId = cloneRubricToSite(rubric.getId(), toContext);
                transversalMap.put(RubricsConstants.RBCS_PREFIX+rubric.getId(), RubricsConstants.RBCS_PREFIX+newId);
            }
        } catch (Exception ex){
            log.info("Exception on duplicateRubricsFromSite: " + ex.getMessage());
        }
        return transversalMap;
    }

    @Override
    public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> options, boolean cleanup) {

        if (cleanup){
            try {
                for (Rubric rubric : rubricRepository.findBySiteId(toContext)) {
                    // TODO: these two lines should be in a transaction
                    associationRepository.deleteByRubricId(rubric.getId());
                    rubricRepository.delete(rubric.getId());
                }
            } catch (Exception e){
                log.error("Rubrics - transferCopyEntities: error trying to delete rubric -> {}" , e.getMessage());
            }
        }
        return transferCopyEntities(fromContext, toContext, ids, null);
    }

    private String cloneRubricToSite(Long rubricId, String toSite){

        try{
            Rubric from = rubricRepository.findOne(rubricId);
            if (from != null) {
                Rubric clone = from.clone(toSite);
                clone = rubricRepository.save(clone);
                return String.valueOf(clone.getId());
            } else {
                log.warn("{} not a valid rubric id", rubricId);
            }
        } catch (Exception e){
            log.error("Exception when cloning rubric {} to site {} : {}", rubricId, toSite, e.getMessage());
        }
        return null;
    }

    @Override
    public String[] myToolIds() {
        return new String[] { RubricsConstants.RBCS_TOOL };
    }

    @Override
    public void updateEntityReferences(String toContext, Map<String, String> transversalMap) {

        if (transversalMap != null && !transversalMap.isEmpty()) {
            for (Map.Entry<String, String> entry : transversalMap.entrySet()) {
                String key = entry.getKey();
                //1 get all the rubrics from map
                if (key.startsWith(RubricsConstants.RBCS_PREFIX)) {
                    try {
                        //2 for each, get its associations
                        List<ToolItemRubricAssociation> assocs = getRubricAssociationByRubric(Long.valueOf(key.substring(RubricsConstants.RBCS_PREFIX.length())));

                        //2b get association params
                        for(ToolItemRubricAssociation association : assocs){
                            Map<String,Boolean> originalParams = association.getParameters();

                            String newRubricId = entry.getValue().substring(RubricsConstants.RBCS_PREFIX.length());
                            String tool = association.getToolId();
                            String itemId = association.getItemId();
                            String newItemId = null;
                            //3 association type
                            switch (tool) {
                                case RubricsConstants.RBCS_TOOL_ASSIGNMENT:
                                    //3a if assignments
                                    log.debug("Handling Rubrics association transfer for Assignment entry {}", itemId);
                                    if (transversalMap.get("assignment/"+itemId) != null){
                                        newItemId = transversalMap.get("assignment/"+itemId).substring("assignment/".length());
                                    }
                                    break;
                                case RubricsConstants.RBCS_TOOL_SAMIGO:
                                    //3b if samigo
                                    if(itemId.startsWith(RubricsConstants.RBCS_PUBLISHED_ASSESSMENT_ENTITY_PREFIX)){
                                        log.debug("Skipping published item {}", itemId);
                                    }
                                    log.debug("Handling Rubrics association transfer for Samigo entry " + itemId);
                                    if(transversalMap.get("sam_item/"+itemId) != null){
                                        newItemId = transversalMap.get("sam_item/"+itemId).substring("sam_item/".length());
                                    }
                                    break;
                                case RubricsConstants.RBCS_TOOL_FORUMS:
                                    //3c if forums
                                    newItemId = itemId.substring(0, 4);
                                    String strippedId = itemId.substring(4);//every forum prefix have this size
                                    log.debug("Handling Rubrics association transfer for Forums entry " + strippedId);
                                    if(RubricsConstants.RBCS_FORUM_ENTITY_PREFIX.equals(newItemId) && transversalMap.get("forum/"+strippedId) != null){
                                        newItemId += transversalMap.get("forum/"+strippedId).substring("forum/".length());
                                    } else if(RubricsConstants.RBCS_TOPIC_ENTITY_PREFIX.equals(newItemId) && transversalMap.get("forum_topic/"+strippedId) != null){
                                        newItemId += transversalMap.get("forum_topic/"+strippedId).substring("forum_topic/".length());
                                    } else {
                                        log.debug("Not found updated id for item {}", itemId);
                                    }
                                    break;
                                case RubricsConstants.RBCS_TOOL_GRADEBOOKNG:
                                    //3d if gradebook
                                    log.debug("Handling Rubrics association transfer for Gradebook entry " + itemId);
                                    if(transversalMap.get("gb/"+itemId) != null){
                                        newItemId = transversalMap.get("gb/"+itemId).substring("gb/".length());
                                    }
                                    break;
                                default:
                                    log.warn("Unhandled tool for Rubrics transfer between sites");
                            }

                            //4 save new association
                            if (newItemId != null) {
                                try {
                                    ToolItemRubricAssociation newAssociation = new ToolItemRubricAssociation();
                                    newAssociation.setToolId(tool);
                                    newAssociation.setItemId(newItemId);
                                    newAssociation.setRubricId(Long.valueOf(newRubricId));
                                    newAssociation.setParameters(originalParams);
                                    associationRepository.save(newAssociation);
                                } catch (Exception exc){
                                    log.error("Error while trying to save new association with item it {} : {}", newItemId, exc.getMessage());
                                }
                            }
                        }
                    } catch (Exception ex){
                        log.error("Error while trying to update association for Rubric {} : {}", key, ex.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public HttpAccess getHttpAccess(){
        return null;
    }

    @Override
    public Collection<String> getEntityAuthzGroups(Reference reference, String userId) {
        return null;
    }

    @Override
    public String getEntityUrl(Reference reference) {
        return getEntity(reference).getUrl();
    }

    @Override
    public Entity getEntity(Reference reference) {
        return null;
    }

    @Override
    public ResourceProperties getEntityResourceProperties(Reference ref) {
       return null;
    }
   
    @Override
    public String getEntityDescription(Reference ref) {
       return null;
    }
   
    @Override
    public boolean parseEntityReference(String reference, Reference ref) {
        return true;
    }

    @Override
    public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport) {
       return null;
    }

    @Override
    public String archive(String siteId, Document doc, Stack<Element> stack, String archivePath, List<Reference> attachments) {
        return null;
    }

    @Override
    public boolean willArchiveMerge() {
        return true;
    }

    @Override
    public String getLabel() {
        return "rubric";
    }

    protected List<ToolItemRubricAssociation> getRubricAssociationByRubric(Long rubricId) throws Exception {
        return associationRepository.findByRubricId(rubricId);
    }
}

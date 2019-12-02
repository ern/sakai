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

package org.sakaiproject.rubrics.logic.repository;

import java.util.List;

import org.sakaiproject.rubrics.logic.model.Evaluation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

@Repository
public interface EvaluationRepository extends MetadataRepository<Evaluation, Long> {

    static final String EVALUATOR_CONSTRAINT = "(1 = ?#{principal.isEvaluator() ? 1 : 0} and " +
            QUERY_CONTEXT_CONSTRAINT + ")";

    static final String EVALUEE_CONSTRAINT = "(1 = ?#{principal.isEvalueeOnly() ? 1 : 0} and " +
            "resource.evaluatedItemOwnerId = ?#{principal.userId})";

    @Override
    @PreAuthorize("canRead(#id, 'Evaluation')")
    Evaluation findOne(Long id);

    @Override
    @PreAuthorize("hasRole('ROLE_EVALUATOR')")
    @Query("select resource from Evaluation resource where " + QUERY_CONTEXT_CONSTRAINT)
    Page<Evaluation> findAll(Pageable pageable);

    @Override
    @PreAuthorize("canWrite(#id, 'Evaluation')")
    void delete(Long id);

    void deleteByToolItemRubricAssociation(Long associationId);

    @PreAuthorize("hasAnyRole('ROLE_EVALUATOR', 'ROLE_EVALUEE')")
    @Query("select resource from Evaluation resource where resource.toolItemRubricAssociation.id = :toolItemRubricAssociationId " +
            "and (" + EVALUATOR_CONSTRAINT + " or " + EVALUEE_CONSTRAINT + ")")
    List<Evaluation> findByToolItemRubricAssociationId(Long toolItemRubricAssociationId);

    @PreAuthorize("hasRole('ROLE_EVALUATOR')")
    @Query("select resource from Evaluation resource where resource.toolItemRubricAssociation.toolId = :toolId " +
            "and resource.toolItemRubricAssociation.itemId = :itemId and " + QUERY_CONTEXT_CONSTRAINT)
    List<Evaluation> findByToolIdAndAssociationItemId(String toolId, String itemId);

    @PreAuthorize("hasAnyRole('ROLE_EVALUATOR', 'ROLE_EVALUEE')")
    @Query("select resource from Evaluation resource where " +
            "resource.evaluatedItemId = :evaluatedItemId " +
            "and resource.toolItemRubricAssociation.toolId = :toolId " +
            "and resource.toolItemRubricAssociation.itemId = :itemId " +
            "and (" + EVALUATOR_CONSTRAINT + " or " + EVALUEE_CONSTRAINT + ")")
    List<Evaluation> findByToolIdAndAssociationItemIdAndEvaluatedItemId(String toolId, String itemId, String evaluatedItemId);

    @PreAuthorize("hasAnyRole('ROLE_EVALUATOR', 'ROLE_EVALUEE')")
    @Query("select resource.evaluatedItemId from Evaluation resource where " +
            " resource.toolItemRubricAssociation.itemId = :associationId " +
            "and resource.evaluatedItemOwnerId = :userId " +
            "and (" + EVALUATOR_CONSTRAINT + " or " + EVALUEE_CONSTRAINT + ")")
    String findByAssociationIdAndUserId(String associationId, String userId);
}

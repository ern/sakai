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
import java.util.Optional;

import org.sakaiproject.rubrics.logic.model.ToolItemRubricAssociation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.security.access.prepost.PreAuthorize;

@Repository
public interface ToolItemRubricAssociationRepository extends MetadataRepository<ToolItemRubricAssociation, Long> {

    @Override
    @PreAuthorize("canRead(#id, 'ToolItemRubricAssociation')")
    ToolItemRubricAssociation findOne(Long id);

    @Override
    @Query("select resource from ToolItemRubricAssociation resource where " + QUERY_CONTEXT_CONSTRAINT)
    Page<ToolItemRubricAssociation> findAll(Pageable pageable);

    @Override
    @PreAuthorize("canWrite(#id, 'ToolItemRubricAssociation')")
    void delete(Long id);

    void deleteByRubricId(Long rubricId);

    @Query("select resource from ToolItemRubricAssociation resource where resource.toolId = :toolId and resource.itemId = :itemId and " + QUERY_CONTEXT_CONSTRAINT)
    Optional<ToolItemRubricAssociation> findByToolIdAndItemId(String toolId, String itemId);

    @Query("select resource from ToolItemRubricAssociation resource where resource.rubricId = :rubricId ") //and " + QUERY_CONTEXT_CONSTRAINT)
    List<ToolItemRubricAssociation> findByRubricId(Long rubricId);
	
    @Query("select resource from ToolItemRubricAssociation resource where resource.toolId = :toolId and resource.itemId like CONCAT(:itemId, '%') and " + QUERY_CONTEXT_CONSTRAINT)
    List<ToolItemRubricAssociation> findByItemIdPrefix(String toolId, String itemId);
}

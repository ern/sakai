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

import java.io.Serializable;

import org.sakaiproject.rubrics.logic.model.Modifiable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;

@NoRepositoryBean
public interface MetadataRepository<T extends Modifiable, ID extends Serializable> extends PagingAndSortingRepository<T, ID> {

    static final String QUERY_CONTEXT_CONSTRAINT = "(resource.metadata.ownerId = ?#{principal.contextId} or 1 = ?#{principal.isSuperUser() ? 1 : 0})";

    @Override
    @PreAuthorize("canWrite(#resource)")
    <S extends T> S save(S resource);

    @Override
    <S extends T> Iterable<S> save(Iterable<S> iterable);

    @Override
    @PreAuthorize("canWrite(#resource)")
    void delete(T resource);

    @Override
    void deleteAll();

    @Override
    void delete(Iterable<? extends T> iterable);
}

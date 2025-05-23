/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.springframework.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * This is modeled after Spring's CrudRepository. The idea here is using this api will
 * make for an easier migration to spring-data in the future.
 */
public interface SpringCrudRepository<T extends PersistableEntity<ID>, ID extends Serializable> extends Repository<T, ID> {

    /**
     * Returns the number of entities available.
     *
     * @return the number of entities
     */
    long count();

    /**
     * Deletes a given entity.
     *
     * @param entity
     * @throws IllegalArgumentException in case the given entity is {@literal null}.
     */
    void delete(T entity);

    /**
     * Deletes all entities managed by the repository.
     */
    void deleteAll();

    /**
     * Deletes the given entities.
     */
    void deleteAll(Iterable<? extends T> entities);

    /**
     * Deletes the entity with the given id.
     *
     * @param id must not be {@literal null}.
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}
     */
    void deleteById(ID id);

    /**
     * Returns whether an entity with the given id exists.
     *
     * @param id must not be {@literal null}.
     * @return true if an entity with the given id exists, {@literal false} otherwise
     * @throws IllegalArgumentException if {@code id} is {@literal null}
     */
    boolean existsById(ID id);

    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    List<T> findAll();

    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    Page<T> findAll(Pageable pageable);

    /**
     * Returns all instances of the type T with the given IDs.
     *
     * @param ids
     * @return
     */
    Iterable<T> findAllById(Iterable<ID> ids);

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id or {@literal null} if none found
     * @throws IllegalArgumentException if {@code id} is {@literal null}
     */
    Optional<T> findById(ID id);

    /**
     * Retrieves a reference to an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @return the entity reference with the given id or {@literal null} if none found
     * @throws IllegalArgumentException if {@code id} is {@literal null}
     */
    T getById(ID id);

    /**
     * Retrieves a reference to an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @return the entity reference with the given id or {@literal null} if none found
     * @throws IllegalArgumentException if {@code id} is {@literal null}
     */
    T getReferenceById(ID id);

    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param entity
     * @return the saved entity
     */
    <S extends T> S save(S entity);

    /**
     * Saves all given entities.
     *
     * @param entities
     * @return the saved entities
     * @throws IllegalArgumentException in case the given entity is {@literal null}.
     */
    <S extends T> Iterable<S> saveAll(Iterable<S> entities);

    /**
     * Serialize object to JSON
     *
     * @param t
     * @return String
     */
    String toJSON(T t);

    /**
     * Deserialize object from JSON
     *
     * @param json
     * @return T
     */
    T fromJSON(String json);

    /**
     * Serialize object to XML. Wraps long strings as cdata inside a wrapper text element
     *
     * @param t
     *
     * @return String The xml
     */
    String toXML(T t);

    /**
     * Serialize object to XML
     *
     * @param t
     * @param cdataAsText If false, avoids wrapping long strings as cdata inside a text element
     *
     * @return String The xml
     */
    String toXML(T t, boolean cdataasText);

    /**
     * Deserialize object from XML
     *
     * @param xml
     * @return T
     */
    T fromXML(String xml);
}

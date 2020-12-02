/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/PermissionsMask.java $
 * $Id: PermissionsMask.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.api.app.messageforums;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * This class is critical for the interaction with AuthorizationManager.
 * This class will be used for creating Authorizations and querying
 * Authorizations. The implementation of this class is not thread safe.
 *
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @version $Id: PermissionsMask.java 632 2005-07-14 21:22:50 +0000 (Thu, 14 Jul 2005) janderse@umich.edu $
 */
@Slf4j
public class PermissionsMask implements Map<String, Boolean> {

    final private Map<String, Boolean> map;

    public PermissionsMask() {
        map = new HashMap<>();
    }

    public PermissionsMask(int initialCapacity) {
        map = new HashMap<>(initialCapacity);
    }

    public PermissionsMask(int initialCapacity, float loadFactor) {
        map = new HashMap<>(initialCapacity, loadFactor);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Set<Map.Entry<String, Boolean>> entrySet() {
        return map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return map.equals(o);
    }

    @Override
    public Boolean get(Object key) {
        return map.get(key);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Boolean put(String key, Boolean value) {
        log.debug("put(String {}, Boolean {})", key, value);
        if (key == null) throw new IllegalArgumentException("Illegal key argument passed!");
        if (value == null) throw new IllegalArgumentException("Illegal value argument passed!");
        return map.put(key, value);
    }

    /**
     * @throws IllegalArgumentException if the specified map is null or any of the
     *                                  keys are not Strings.
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends String, ? extends Boolean> m) {
        log.debug("putAll(Map {})", m);
        if (m == null) throw new IllegalArgumentException("Illegal map argument passed!");
        map.putAll(m);
    }

    @Override
    public Boolean remove(Object key) {
        return map.remove(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Collection<Boolean> values() {
        return map.values();
    }

    @Override
    public String toString() {
        return map.toString();
    }
}

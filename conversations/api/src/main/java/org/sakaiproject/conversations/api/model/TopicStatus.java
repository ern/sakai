/*
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.conversations.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONV_TOPIC_STATUS",
    uniqueConstraints = { @UniqueConstraint(name = "UniqueTopicStatus", columnNames = { "TOPIC_ID", "USER_ID" }) },
    indexes = { @Index(name = "conv_topic_status_topic_user_idx", columnList = "TOPIC_ID, USER_ID") })
@Getter
@Setter
public class TopicStatus implements PersistableEntity<Long> {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "SITE_ID", nullable = false)
    private String siteId;

    @Column(name = "TOPIC_ID", length = 36, nullable = false)
    private String topicId;

    @Column(name = "USER_ID", length = 99, nullable = false)
    private String userId;

    @Column(name = "BOOKMARKED")
    private Boolean bookmarked = Boolean.FALSE;

    @Column(name = "UNREAD")
    private Integer unread = 0;

    @Column(name = "POSTED")
    private Boolean posted = Boolean.FALSE;

    @Column(name = "VIEWED")
    private Boolean viewed = Boolean.FALSE;

    public TopicStatus() {
    }

    public TopicStatus(String siteId, String topicId, String userId) {

        this.siteId = siteId;
        this.topicId = topicId;
        this.userId = userId;
    }
}

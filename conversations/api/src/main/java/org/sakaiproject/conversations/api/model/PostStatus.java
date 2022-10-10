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

import java.time.Instant;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONV_POST_STATUS",
    indexes = { @Index(name = "conv_post_status_user_idx", columnList = "USER_ID"),
                @Index(name = "conv_post_status_post_idx", columnList = "POST_ID"),
                @Index(name = "conv_post_status_post_user_idx", columnList = "POST_ID, USER_ID"),
                @Index(name = "conv_post_status_topic_user_idx", columnList = "TOPIC_ID, USER_ID") },
    uniqueConstraints = { @UniqueConstraint(name = "UniquePostStatus", columnNames = { "POST_ID", "USER_ID" }) })
@Getter
@Setter
public class PostStatus implements PersistableEntity<Long> {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "TOPIC_ID", length = 36, nullable = false)
    private String topicId;

    @Column(name = "POST_ID", length = 36, nullable = false)
    private String postId;

    @Column(name = "USER_ID", length = 99, nullable = false)
    private String userId;

    @Column(name = "VIEWED_DATE")
    private Instant viewedDate;

    @Column(name = "VIEWED")
    private Boolean viewed = Boolean.FALSE;

    @Column(name = "UPVOTED")
    private Boolean upvoted = Boolean.FALSE;

    public PostStatus() {
    }

    public PostStatus(String topicId, String postId, String userId) {

        this.topicId = topicId;
        this.postId = postId;
        this.userId = userId;
    }
}

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
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

import lombok.Getter;

@Entity
@Table(name = "CONV_USER_STATISTICS", indexes = @Index(name = "conv_user_stats_user_idx", columnList = "USER_ID"))
@Getter
public class UserStatistics {

    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "USER_ID", length = 99, nullable = false)
    private String userId;

    @Column(name = "TOPIC_ID", length = 36)
    private String topicId;

    @Column(name = "NUMBER_OF_POSTS")
    private Integer numberOfPosts = 0;

    @Column(name = "LAST_POST_DATE")
    private Instant lastPostDate;

    @Column(name = "NUMBER_OF_UPVOTES")
    private Integer numberOfUpvotes = 0;

    @Column(name = "NUMBER_OF_REACTIONS")
    private Integer numberOfReactions = 0;

    @Column(name = "NUMBER_OF_REPLIES")
    private Integer numberOfReplies = 0;

    @Column(name = "NUMBER_READ")
    private Integer numberRead = 0;

}

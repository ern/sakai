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

import org.sakaiproject.conversations.api.Reaction;
import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONV_POST_REACTION_TOTALS",
    indexes = { @Index(name = "conv_post_reaction_totals_post_idx", columnList = "POST_ID") },
    uniqueConstraints = { @UniqueConstraint(name = "UniquePostReactionTotals", columnNames = { "POST_ID", "REACTION" }) })
@Getter
@Setter
public class PostReactionTotal implements PersistableEntity<Long> {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;

    @Column(name = "POST_ID", length = 36, nullable = false)
    private String postId;

    @Column(name = "REACTION", nullable = false)
    private Reaction reaction;

    @Column(name = "TOTAL", nullable = false)
    private Integer total;
}

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
package org.sakaiproject.assignment.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Defines a relation between a submission and the submission's submitters.
 * <br/> - A submitter can have its own grade separate from the grade of the submission,
 * useful in providing user with different grades in group submissions.
 * <br/> - A submitter can have its own feedback separate from the feedback of the submission,
 * useful when different feedback is needed in group submissions
 * <p>
 * <b>Constraints</b>
 * <br/>- submission and submitter are unique,
 * meaning a user can't be a submitter more than once on a submission.
 * Notice that equals and hashcode also reflect this relationship.
 */
@Entity
@Table(name = "ASN_SUBMISSION_SUBMITTER",
        uniqueConstraints = @UniqueConstraint(columnNames = {"SUBMISSION_ID", "SUBMITTER"}),
        indexes = {	@Index(columnList = "SUBMITTER"),
                @Index(name = "FK_ASN_SUBMISSION_SUB_I", columnList = "SUBMISSION_ID")
        })
@Data
@NoArgsConstructor
@ToString(exclude = {"submission"})
@EqualsAndHashCode(of = {"submission", "submitter"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class AssignmentSubmissionSubmitter {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "assignment_submission_submitters_sequence")
    @SequenceGenerator(name = "assignment_submission_submitters_sequence", sequenceName = "ASN_SUBMISSION_SUBMITTERS_S")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "SUBMISSION_ID", nullable = false)
    @JsonBackReference
    private AssignmentSubmission submission;

    @Column(name = "SUBMITTER", length = 99, nullable = false)
    private String submitter;

    @Column(name = "SUBMITTEE", nullable = false)
    private Boolean submittee = Boolean.FALSE;

    @Column(name = "GRADE", length = 32)
    private String grade;

    @Lob
    @Column(name = "FEEDBACK", length = 65535)
    private String feedback;
    
    @Column(name = "TIME_SPENT", length = 255)
    private String timeSpent;

}

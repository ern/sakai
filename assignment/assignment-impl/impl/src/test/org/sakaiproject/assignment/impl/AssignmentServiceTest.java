/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.assignment.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.util.StringUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-test-context.xml"})
public class AssignmentServiceTest {

    @InjectMocks
    private AssignmentService assignmentService;

//    // test interation number
//    int testNumber = 5;
//
//    // specify the total number of users
//    int userNumber = 200;
//
//    // attachment text size in kB
//    private int attachmentSize = 3000;
//
//    // attachment id
//    String attachmentId = null;
//
//    // attachment reference
//    String attachmentReference = null;
//
//    // test zip bunction of the JVM
//    boolean testZipFunction = false;
//
//    String bigAttachmentContentString = null;
//
//    @Before
//    public void setUp() {
//        bigAttachmentContentString = generateBigTestString(attachmentSize);
//    }

    @Test
    public void AssignmentServiceIsValid() {
        Assert.assertNotNull(assignmentService);
    }
	
	public void tearDown() throws Exception {
		log.debug("Tearing down an AuthzIntegrationTest test");
		
		/*// Remove the assignment, submission objects created for testing
		for (int i = 0; i < submissionList.size(); i++)
		{
			AssignmentSubmission s = (AssignmentSubmission) submissionList.get(i);
			assignmentService.removeSubmission(assignmentService.editSubmission(s.getReference()));
		}
		
		assignmentService.removeAssignment(assignmentService.editAssignment(assignmentReference));
		assignmentService.removeAssignmentContent(assignmentService.editAssignmentContent(contentReference));
		
		// remove all users generated
		for (int userIndex = 0; userIndex < userNumber; userIndex++)
		{
			UserDirectoryService.removeUser(UserDirectoryService.editUser("test_user" + userIndex));
		}*/
		
		// remove the attachment resouce object created for testing
		//ContentHostingService.removeResource(ContentHostingService.editResource(attachmentId));
		//ContentHostingService.removeCollection(ContentHostingService.editCollection(ContentHostingService.getResource(attachmentId).getContainingCollection().getId()));

	}

	protected void zipWithFlushing(boolean flushing)
	{
		String assignmentTitle = "Test Assignment Title";
		
		try
		{
			ZipOutputStream out = new ZipOutputStream(new ByteArrayOutputStream());

			// create the folder structor - named after the assignment's title
			String root = assignmentTitle + Entity.SEPARATOR;

			// submission text of size 2KB
			String submittedText = generateBigTestString(2);

			// Create the ZIP file
			String submittersName = "";
			for (int count =0; count < userNumber; count++)
			{
				submittersName = root;
				String submittersString = "test user " + count;
					
				if (StringUtils.trimToNull(submittersString) != null)
				{
					submittersName = submittersName.concat(StringUtils.trimToNull(submittersString));

					try
					{
						submittersName = submittersName.concat("/");
						// create the folder structure - named after the submitter's name
						// create the text file only when a text submission is allowed
						String entryName = submittersName + submittersString + "_submissionText.html";
						ZipEntry textEntry = new ZipEntry(entryName);
						out.putNextEntry(textEntry);
						out.write(submittedText.getBytes());
						out.closeEntry();

						// create the attachment file(s)
						// buffered stream input
						InputStream content = new ByteArrayInputStream(bigAttachmentContentString.getBytes());
						byte data[] = new byte[1024 * 10];
						BufferedInputStream bContent = new BufferedInputStream(content, data.length);
						
						ZipEntry attachmentEntry = new ZipEntry(submittersName + "Test Attachment");
						out.putNextEntry(attachmentEntry);
						int bCount = -1;
						while ((bCount = bContent.read(data, 0, data.length)) != -1) 
						{
							out.write(data, 0, bCount);
						}
						out.closeEntry();
						content.close();

						// flush or not
						if (flushing)
						{
							out.flush();
						}
					}
					catch (IOException e)
					{
						log.debug(this
										+ ": --IOException: Problem in creating the attachment file: submittersName="
										+ submittersName + " attachment reference=" + attachmentReference);
					}
				}
			}	// for
			
			// clean up
			out.finish();
			out.close();
		}
		catch (IOException e)
		{
			log.debug(this + ": --IOException unable to create the zip file for assignment "
					+ assignmentTitle);
		}
	}
	
	/**
	 * 
	 * test the zip routine without/with flushing for 5 times
	 *
	 */
	public void testZipSubmissionsWithoutFlushing() 
	{
		if( testZipFunction ) {	
		System.out.println("student number = " + userNumber);
		System.out.println("attachment size = " + attachmentSize + "KB");
		System.gc();
		
		for (int index = 1; index <= testNumber; index++)
		{
			Random ran = new Random();
			boolean flushing = ran.nextBoolean();
			
			Runtime r = Runtime.getRuntime();
			System.out.println("with flushing = " + flushing);
			long mBefore = r.freeMemory();
			long tBefore = System.currentTimeMillis();
			
			zipWithFlushing(flushing);
			long mAfter = r.freeMemory();
			long tAfter = System.currentTimeMillis();
			System.out.println("free memory before invoke " + mBefore + " after " + mAfter);
			System.out.println("minute time used " + (tAfter-tBefore)/(1000.0*60.0));
			System.out.println("percent  " + (mBefore-mAfter)* 100/(mBefore*1.0));
			System.out.println("*************");
			// gc
			System.gc();
		}
		}
	}

//    protected void zipWithFlushing(boolean flushing) {
//        String assignmentTitle = "Test Assignment Title";
//
//        try {
//            ZipOutputStream out = new ZipOutputStream(new ByteArrayOutputStream());
//
//            // create the folder structor - named after the assignment's title
//            String root = assignmentTitle + Entity.SEPARATOR;
//
//            // submission text of size 2KB
//            String submittedText = generateBigTestString(2);
//
//            // Create the ZIP file
//            String submittersName = "";
//            for (int count = 0; count < userNumber; count++) {
//                submittersName = root;
//                String submittersString = "test user " + count;
//
//                if (StringUtil.trimToNull(submittersString) != null) {
//                    submittersName = submittersName.concat(StringUtil.trimToNull(submittersString));
//
//                    try {
//                        submittersName = submittersName.concat("/");
//                        // create the folder structure - named after the submitter's name
//                        // create the text file only when a text submission is allowed
//                        String entryName = submittersName + submittersString + "_submissionText.html";
//                        ZipEntry textEntry = new ZipEntry(entryName);
//                        out.putNextEntry(textEntry);
//                        out.write(submittedText.getBytes());
//                        out.closeEntry();
//
//                        // create the attachment file(s)
//                        // buffered stream input
//                        InputStream content = new ByteArrayInputStream(bigAttachmentContentString.getBytes());
//                        byte data[] = new byte[1024 * 10];
//                        BufferedInputStream bContent = new BufferedInputStream(content, data.length);
//
//                        ZipEntry attachmentEntry = new ZipEntry(submittersName + "Test Attachment");
//                        out.putNextEntry(attachmentEntry);
//                        int bCount = -1;
//                        while ((bCount = bContent.read(data, 0, data.length)) != -1) {
//                            out.write(data, 0, bCount);
//                        }
//                        out.closeEntry();
//                        content.close();
//
//                        // flush or not
//                        if (flushing) {
//                            out.flush();
//                        }
//                    } catch (IOException e) {
//                        log.debug(this
//                                + ": --IOException: Problem in creating the attachment file: submittersName="
//                                + submittersName + " attachment reference=" + attachmentReference);
//                    }
//                }
//            }    // for
//
//            // clean up
//            out.finish();
//            out.close();
//        } catch (IOException e) {
//            log.debug(this + ": --IOException unable to create the zip file for assignment "
//                    + assignmentTitle);
//        }
//    }
//
//    /**
//     * test the zip routine without/with flushing for 5 times
//     */
//    public void testZipSubmissionsWithoutFlushing() {
//        if (testZipFunction) {
//            System.out.println("student number = " + userNumber);
//            System.out.println("attachment size = " + attachmentSize + "KB");
//            System.gc();
//
//            for (int index = 1; index <= testNumber; index++) {
//                Random ran = new Random();
//                boolean flushing = ran.nextBoolean();
//
//                Runtime r = Runtime.getRuntime();
//                System.out.println("with flushing = " + flushing);
//                long mBefore = r.freeMemory();
//                long tBefore = System.currentTimeMillis();
//
//                zipWithFlushing(flushing);
//                long mAfter = r.freeMemory();
//                long tAfter = System.currentTimeMillis();
//                System.out.println("free memory before invoke " + mBefore + " after " + mAfter);
//                System.out.println("minute time used " + (tAfter - tBefore) / (1000.0 * 60.0));
//                System.out.println("percent  " + (mBefore - mAfter) * 100 / (mBefore * 1.0));
//                System.out.println("*************");
//                // gc
//                System.gc();
//            }
//        }
//    }
//
//    private String generateBigTestString(int sizeKB) {
//        // auto generate string with size specified
//        StringBuilder buffer = new StringBuilder();
//
//        int realSize = sizeKB * 1024;
//
//        for (int i = 0; i < realSize; i++) {
//            buffer.append("1");
//        }
//        return buffer.toString();
//    }
}

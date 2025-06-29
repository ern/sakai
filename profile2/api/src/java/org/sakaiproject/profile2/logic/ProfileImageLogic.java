/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.logic;

import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.ProfileImage;

/**
 * An interface for dealing with images in Profile2
 * 
 * <p>Also takes care of image conversion from classic Profile to Profile2</p>
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
public interface ProfileImageLogic {

    /**
     * Get the blank profile image, the one a user sees if there is
     * no other image available.
     */
	public ProfileImage getBlankProfileImage();

	/**
	 * Get the profile image for a user. Takes into account all global settings, user preferences .
	 * 
	 * <p>If making a request for your own image</p>
	 * <ul>
	 *  <li>You should provide 'prefs' (if available) otherwise it will be looked up.</li>
	 * </ul>
	 * 
	 * <p>If making a request for someone else's image</p>
	 * <ul>
	 *  <li>You should provide the preferences object for that user (if available), otherwise it will be looked up.</li>
	 *  <li>If preferences is still null, the global preference will be used, which may not exist and therefore be default.</li>
	 * </ul>
	 * 
	 * <p>The returned ProfileImage object is a wrapper around all of the types of image that can be set. use the getBinarty and getUrl() methods on this object to get the data.
	 * See the docs on ProfileImage for how to use this.
	 *  
	 * @param userUuid
	 * @param prefs
	 * @param size
	 * @return
	 */
	public ProfileImage getProfileImage(String userUuid, int size);
	
	/**
	 * Gets the official profile image for a user.
	 * @param userUuid
	 * @param siteId siteId to check that the requesting user has roster.viewofficialphoto permission
	 * @return The ProfileImage object, populated with either a url or binary data.
	 */
	public ProfileImage getOfficialProfileImage(String userUuid, String siteId);
	
	/**
	 * Get the profile image for a user. Takes into account all global settings, user preferences and permissions in the given site.
	 * @param userUuid
	 * @param prefs
	 * @param size
	 * @param siteId - optional siteid to check if the current user has permissions in this site to see the target user's image (PRFL-411)
	 * @return
	 */
	public ProfileImage getProfileImage(String userUuid, int size, String siteId);

	
	/**
	 * Get the profile image for a user.
	 * @param person	Person object that contains all info about a user
	 * @param size		size of image to return.
	 * @return
	 */
	public ProfileImage getProfileImage(Person person, int size);
	
	/**
	 * Get the profile image for a user.
	 * @param person	Person object that contains all info about a user
	 * @param size		size of image to return.
	 * @param siteId - optional siteid to check if the current user has permissions in this site to see the target user's image (PRFL-411)
	 * @return
	 */
	public ProfileImage getProfileImage(Person person, int size, String siteId);
	
	/**
	 * Update the profile image for a user given the byte[] of the image.
	 * <p>Will work, but not have visible effect if the setting for the image type used in sakai.properties is not upload. ie its using URL instead
	 * 
	 * @param userUuid - uuid for the user
	 * @param imageBytes - byte[] of image
	 * @param mimeType - mimetype of image, ie image/jpeg
	 * @param filename - name of file, used by ContentHosting, optional.
	 * @return
	 */
	public boolean setUploadedProfileImage(String userUuid, byte[] imageBytes, String mimeType, String fileName); 
	
	
	/**
	 * Update the profileImage for a user given the URL to an image
	 * <p>Will work, but not have visible effect if the setting for the image type used in sakai.properties is not url. ie its using an uploaded image instead
	 * 
	 * @param userUuid - uuid for the user
	 * @param fullSizeUrl - url for main image
	 * @param thumbnailUrl - thumbnail for main image to be used when thumbnail sizes are requested. 
	 * Leave blank or null for none and when a thumbnail is requested it will return the full image which can be scaled in the markup.
	 * @param avatar - avatar for main image to be used when avatar sizes are requested. Can be blank and it will fallback as per thumbnail
	 * @return
	 */
	public boolean setExternalProfileImage(String userUuid, String fullSizeUrl, String thumbnailUrl,  String avatar);
	
	/**
	 * Get the full URL to the default unavailable image defined in ProfileConstants
	 * @return
	 */
	public String getUnavailableImageURL();
	
	/**
	 * Get the full URL to the default unavailable image thumbnail defined in ProfileConstants
	 * @return
	 */
	public String getUnavailableImageThumbnailURL();
	
	/**
	 * Save the official image url that institutions can set.
	 * @param userUuid		uuid of the user
	 * @param url			url to image
	 * @return
	 */
	public boolean saveOfficialImageUrl(final String userUuid, final String url);
	
	/**
	 * Get the entity url to a profile image for a user.
	 *  
	 * It can be added to any profile without checks as the retrieval of the image does the checks, and a default image
	 * is used if not allowed or none available.
	 * 
	 * @param userUuid	uuid for the user
	 * @param size		size of image, from ProfileConstants
	 */
	public String getProfileImageEntityUrl(String userUuid, int size);
	
	/**
	 * Reset the profile image for a user
	 * 
	 * @param userUuid uuid for the user
	 * @return
	 */
	public boolean resetProfileImage(final String userUuid);
	
	/**
	 * Does this use have a default profile image?
	 * 
	 * @param userUuid uuid for the user
	 * @return
	 */
	public boolean profileImageIsDefault(final String userUuid);

	/**
	 * Generate a profile image for a user with his name/last name initials
	 * @param userUuid uuid for the user
	 * @return The ProfileImage object, populated with either a url or binary data.
	 */
	public ProfileImage getProfileAvatarInitials(final String userUuid);

	/**
	 * Find out if the user is allowed to change his profile picture
	 * based on the related properties
	 * @return
	 */
	public boolean isPicEditorEnabled();
}

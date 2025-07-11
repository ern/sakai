<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
	<jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>
  <sakai:view toolCssHref="/messageforums-tool/css/msgcntr.css">
  	<h:form id="dfStatisticsForm">
				<!-- discussionForum/statistics/dfStatisticsDisplayInThread.jsp -->
  	    <script>includeLatestJQuery("msgcntr");</script>
       	<script src="/messageforums-tool/js/sak-10625.js"></script>
       	<script src="/messageforums-tool/js/forum.js"></script>
       	<script src="/messageforums-tool/js/messages.js"></script>
       	<script>
			$(document).ready(function() {
				$(".messageBody").each(function(index){
					let msgBody = $(this).html();
					msgBody = msgBody.replace(/\n/g,',').replace(/\s/g,' ').replace(/  ,/g,',');
					const wordCountId = $(this).attr('id').substring(11, $(this).attr('id').length);
					$("#wordCountSpan" + wordCountId).html(getWordCount(msgBody));
				});
				var menuLink = $('#forumsStatisticsMenuLink');
				var menuLinkSpan = menuLink.closest('span');
				menuLinkSpan.addClass('current');
				menuLinkSpan.html(menuLink.text());
			});
		</script>
		<link rel="stylesheet" type="text/css" href="/messageforums-tool/css/msgcntr_statistics.css" />
		<%@ include file="/jsp/discussionForum/menu/forumsMenu.jsp" %>

		<h:panelGroup layout="block" styleClass="breadCrumb" rendered="#{ForumTool.instructor}"><h3>
		<h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title=" #{msgs.cdfm_message_forums}"
			      	rendered="#{ForumTool.messagesandForums}" />
		<h:commandLink action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_discussions}" title=" #{msgs.cdfm_discussions}"
			      	rendered="#{ForumTool.forumsTool}" />
		<h:outputText value=" / "/>
		<h:commandLink action="#{ForumTool.processActionStatistics}" value="#{msgs.stat_list}" title="#{msgs.stat_list}" rendered="#{empty mfStatisticsBean.selectedAllTopicsTopicId && empty mfStatisticsBean.selectedAllTopicsForumId}"/>
		<h:commandLink action="#{mfStatisticsBean.processActionStatisticsByAllTopics}" value="#{msgs.stat_list}" title="#{msgs.stat_list}" rendered="#{!empty mfStatisticsBean.selectedAllTopicsTopicId || !empty mfStatisticsBean.selectedAllTopicsForumId}"/>				
	      <h:panelGroup rendered="#{!empty mfStatisticsBean.selectedAllTopicsForumId}">
		      <h:outputText value=" / "/>
		      <h:commandLink action="#{mfStatisticsBean.processActionStatisticsByTopic}" immediate="true">
			    <f:param value="" name="topicId"/>
			    <f:param value="#{mfStatisticsBean.selectedAllTopicsForumId}" name="forumId"/>
			    <h:outputText value="#{mfStatisticsBean.selectedAllTopicsForumTitle}" />
	      	  </h:commandLink>
		  </h:panelGroup>
		  <h:panelGroup rendered="#{!empty mfStatisticsBean.selectedAllTopicsTopicId}">
	      	  <h:outputText value=" / "/>
		      <h:commandLink action="#{mfStatisticsBean.processActionStatisticsByTopic}" immediate="true">
			    <f:param value="#{mfStatisticsBean.selectedAllTopicsTopicId}" name="topicId"/>
			    <f:param value="#{mfStatisticsBean.selectedAllTopicsForumId}" name="forumId"/>
			    <h:outputText value="#{mfStatisticsBean.selectedAllTopicsTopicTitle}" />
	      	  </h:commandLink>
	      </h:panelGroup> 
	    <h:outputText value=" / "/>
		<h:commandLink action="#{mfStatisticsBean.processActionBackToUser}" >
			<h:outputText value="#{mfStatisticsBean.selectedSiteUser}" />
		</h:commandLink>
		<h:outputText value=" / "/>
		<h:commandLink action="#{ForumTool.processActionShowFullTextForAll}" value="#{msgs.stat_authored}" />
		<h:outputText value=" / "/>
		<h:outputText value="#{ForumTool.selectedForum.forum.title}" />
		<h:outputText value=" / "/>
		<h:outputText value="#{ForumTool.selectedTopic.topic.title}" />
		</h3></h:panelGroup>

		<h:panelGroup id="forumsAction">
			<h:outputLink styleClass="button" id="print" value="javascript:printFriendly('#{ForumTool.printFriendlyDisplayInThread}');" title="#{msgs.cdfm_print}">
				<span class="bi bi-printer" aria-hidden="true"></span>
			</h:outputLink>
		</h:panelGroup>

  		<mf:hierDataTable id="allMessagesForOneTopic" value="#{ForumTool.messages}" var="msgDecorateBean" noarrows="true" styleClass="table table-hover table-striped table-bordered messagesThreaded" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">	
   			<h:column id="_msg_subject" >
   			<h:panelGroup layout="block" rendered="#{ForumTool.selectedMsgId!=msgDecorateBean.message.id}" styleClass="hierItemBlock">
				<f:verbatim><p style="border-bottom:1px solid #ccc;padding-bottom:5px;height:100%;overflow:hidden;font-size:110% !important;color:#000;font-weight:bold"></f:verbatim>
					<h:panelGroup rendered="#{!msgDecorateBean.message.deleted}">
						<h:outputText value="#{msgDecorateBean.message.title} - " />
						<h:outputText rendered="#{mfStatisticsBean.pureAnon}" styleClass="anonymousAuthor" value="#{msgDecorateBean.anonAwareAuthor}" />
						<h:outputText rendered="#{!mfStatisticsBean.pureAnon}" value="#{msgDecorateBean.anonAwareAuthor}" />
						<h:outputText rendered="#{mfStatisticsBean.pureAnon && msgDecorateBean.currentUserAndAnonymous}" value=" #{msgs.cdfm_me}" />
						<h:outputText value=" #{msgDecorateBean.message.created}">
							<f:convertDateTime pattern="#{msgs.date_format_paren}" timeZone="#{ForumTool.userTimeZone}" locale="#{ForumTool.userLocale}"/>
						</h:outputText>
					</h:panelGroup>
				<f:verbatim></p></f:verbatim>				
				<%-- more deleted message stub rendering --%>
				<h:panelGroup styleClass="inactive" rendered="#{msgDecorateBean.message.deleted}">
					<h:outputText value="#{msgs.cdfm_msg_deleted_label}" />
				</h:panelGroup>
				<mf:htmlShowArea value="#{msgDecorateBean.message.body}" hideBorder="true" rendered="#{!msgDecorateBean.message.deleted}"/>
				<mf:htmlShowArea value="" hideBorder="true" rendered="#{msgDecorateBean.message.deleted}"/>	
			</h:panelGroup>

			<h:dataTable value="#{msgDecorateBean.attachList}" var="eachAttach" styleClass="attachListJSF indnt1"  style="margin-bottom:5px" rendered="#{!empty msgDecorateBean.attachList}">
				<h:column rendered="#{!empty msgDecorateBean.attachList}">
					<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>		
					<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />							
					<h:outputLink value="#{eachAttach.url}" target="_blank">
						<h:outputText value="#{eachAttach.attachment.attachmentName}" />
					</h:outputLink>								
				</h:column>
			</h:dataTable>
	

			<%-- the message the user wanted to see in the thread context --%>
			<h:panelGroup rendered="#{ForumTool.selectedMsgId==msgDecorateBean.message.id}">
				<a name="boldMsg"></a>
				<div class="hierItemBlock" style="border:1px solid #fc6;background:#ffe;padding:0 5px">
						<span id="messageBody" class="messageBody" style="display: none" class="messageBody">
							<h:outputText escape="false" value="#{msgDecorateBean.message.body}"/>
						</span>	
						<span>
							<span class="bi bi-plus-square-fill" aria-hidden="true"></span>
						</span>
						<h:outputText value="#{msgs.cdfm_message_count}: " escape="false" />
						<span id="wordCountSpan<h:outputText value="#{stat.msgId}"/>"> </span>
						<a name="boldMsg"></a>
						<p style="border-bottom:1px solid #ccc;padding-bottom:5px;height:100%;overflow:hidden;font-size:110% !important;color:#000;font-weight:bold">
						<h:panelGroup rendered="#{!msgDecorateBean.message.deleted}">
							<h:outputText value="#{msgDecorateBean.message.title} - " />
							<h:outputText rendered="#{mfStatisticsBean.pureAnon}" styleClass="anonymousAuthor" value="#{msgDecorateBean.anonAwareAuthor}" />
							<h:outputText rendered="#{!mfStatisticsBean.pureAnon}" value="#{msgDecorateBean.anonAwareAuthor}" />
							<h:outputText rendered="#{mfStatisticsBean.pureAnon && msgDecorateBean.currentUserAndAnonymous}" value=" #{msgs.cdfm_me}" />
							<h:outputText value=" #{msgDecorateBean.message.created}">
								<f:convertDateTime pattern="#{msgs.date_format_paren}" timeZone="#{ForumTool.userTimeZone}" locale="#{ForumTool.userLocale}"/>
							</h:outputText>
						</h:panelGroup>
					<f:verbatim></p></f:verbatim>								
				<h:panelGroup styleClass="inactive" rendered="#{msgDecorateBean.message.deleted}">
					<f:verbatim><span></f:verbatim>
						<h:outputText value="#{msgs.cdfm_msg_deleted_label}" />
					<f:verbatim></span></f:verbatim>
				</h:panelGroup>
				<mf:htmlShowArea value="#{msgDecorateBean.message.body}" hideBorder="true" rendered="#{!msgDecorateBean.message.deleted}"/>
				<mf:htmlShowArea value="" hideBorder="true" rendered="#{msgDecorateBean.message.deleted}"/>
				<h:panelGroup rendered="#{!empty msgDecorateBean.attachList}">
					<h:dataTable value="#{msgDecorateBean.attachList}" var="eachAttach" styleClass="attachListJSF indnt1" rendered="#{!empty msgDecorateBean.attachList}">
						<h:column rendered="#{!empty msgDecorateBean.attachList}">
							<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>		
							<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />							
							<h:outputLink value="#{eachAttach.url}" target="_blank">
								<h:outputText value="#{eachAttach.attachment.attachmentName}" />
							</h:outputLink>								
						</h:column>
						</h:dataTable>
				</h:panelGroup>

				<f:verbatim></div></f:verbatim>
				</h:panelGroup>				
  			
 		</h:column>
	</mf:hierDataTable>


  		<h:panelGroup styleClass="act" style="display:block">
  			<h:commandButton action="#{mfStatisticsBean.processActionBackToUser}" value="#{mfStatisticsBean.buttonUserName}"  
			               title="#{mfStatisticsBean.buttonUserName}">			               			
			</h:commandButton>
		</h:panelGroup>
		
			<%
				String thisId = request.getParameter("panel");
				if (thisId == null) {
					thisId = "Main"	+ org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
				}
			%>
			<script>
				function resize(){
  					mySetMainFrameHeight('<%=org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
  				}
			</script>

			<script>
				resize();				
			//find the anchor	
				document.location.href=document.location.href + "#boldMsg";	
			//Set attribute onload here to skip calling portal's setMainFrameHeight, otherwise the scroll bar will reset to go to the top. Put setFocus method here is because portal's onload has two methods. one is setMainFrameHeight, another is setFocus. 			
				document.body.setAttribute("onload", "setFocus(focus_path)");
			</script>
  	</h:form>
  </sakai:view>
 </f:view>

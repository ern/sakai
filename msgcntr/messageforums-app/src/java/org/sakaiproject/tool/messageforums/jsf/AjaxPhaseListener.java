package org.sakaiproject.tool.messageforums.jsf;

import java.util.Map;

import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseStream;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.el.ValueBinding;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.messageforums.DiscussionForumTool;

@Slf4j
public class AjaxPhaseListener implements PhaseListener {

	public void afterPhase(PhaseEvent event) {
		FacesContext context = FacesContext.getCurrentInstance();
		Application app = context.getApplication();
		ValueBinding binding = app.createValueBinding("#{ForumTool}");
		DiscussionForumTool forumTool = (DiscussionForumTool) binding
				.getValue(context);
		Map requestParams = context.getExternalContext()
				.getRequestParameterMap();

		String action = (String) requestParams.get("action");
		String messageId = (String) requestParams.get("messageId");
		String topicId = (String) requestParams.get("topicId");
		String ajax = (String) requestParams.get("ajax");

		HttpServletResponse response = (HttpServletResponse)context.getExternalContext().getResponse();
		if ("true".equals(ajax)) {
			try {
				ServletOutputStream out = response.getOutputStream();
				response.setHeader("Pragma", "No-Cache");
				response.setHeader("Cache-Control",
						"no-cache,no-store,max-age=0");
				response.setDateHeader("Expires", 1);
				if (action == null) {
					out.println("FAIL");
				} else if ("markMessageAsRead".equals(action)) {
					// Ajax call to mark messages as read for user
					if (messageId != null && topicId != null) {
						if (!forumTool.isMessageReadForUser(Long.valueOf(topicId),
								Long.valueOf(messageId))) {
							forumTool.markMessageReadForUser(Long.valueOf(topicId),
									Long.valueOf(messageId), true);
							out.println("SUCCESS");
						} else {
							// also output success in case message is read, but
							// page rendered mail icon (old state)
							out.println("SUCCESS");
						}
					}
				}
				out.flush();
			} catch (Exception ee) {
				log.error(ee.getMessage(), ee);
			}
			context.responseComplete();
		}
		;
	}

	public void beforePhase(PhaseEvent arg0) {
		// TODO Auto-generated method stub

	}

	public PhaseId getPhaseId() {
		return PhaseId.RESTORE_VIEW;
	}

}

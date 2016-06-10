package org.sakaiproject.contentreview.service.advisors;

import lombok.Setter;
import org.sakaiproject.contentreview.advisors.ContentReviewSiteAdvisor;
import org.sakaiproject.site.api.Site;

import java.util.Collections;
import java.util.List;

/**
 * Evaluates all the advisors in the list with the operation and returns the result.
 * Basically is does a AND or OR on all the advisors. OR is the default.
 */
public class ChainedPropertyAdvisor implements ContentReviewSiteAdvisor {

    @Setter
    private String operation;

    private List<ContentReviewSiteAdvisor> advisors = Collections.emptyList();

    public void setAdvisors(List<ContentReviewSiteAdvisor> advisors) {
        if (advisors != null) {
            this.advisors = advisors;
        }
    }

    @Override
    public boolean siteCanUseReviewService(Site site) {
        return and()
                ? (advisors.stream().filter(advisor -> advisor.siteCanUseReviewService(site)).count() == advisors.size())
                : advisors.stream().filter(advisor -> advisor.siteCanUseReviewService(site)).findFirst().isPresent();
    }

    @Override
    public boolean siteCanUseLTIReviewService(Site site) {
        return and()
                ? (advisors.stream().filter(advisor -> advisor.siteCanUseLTIReviewService(site)).count() == advisors.size())
                : advisors.stream().filter(advisor -> advisor.siteCanUseLTIReviewService(site)).findFirst().isPresent();
    }

    @Override
    public boolean siteCanUseDirectReviewService(Site site) {
        return and()
                ? (advisors.stream().filter(advisor -> advisor.siteCanUseDirectReviewService(site)).count() == advisors.size())
                : advisors.stream().filter(advisor -> advisor.siteCanUseDirectReviewService(site)).findFirst().isPresent();
    }

    private boolean and() {
        if ("and".equalsIgnoreCase(operation)) {
            return true;
        }
        return false;
    }
}
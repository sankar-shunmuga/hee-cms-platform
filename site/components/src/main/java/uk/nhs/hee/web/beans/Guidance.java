package uk.nhs.hee.web.beans;

import org.hippoecm.hst.content.beans.Node;
import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;

import java.util.List;

/** 
 * TODO: Beanwriter: Failed to create getter for node type: hippo:compound
 */
@HippoEssentialsGenerated(internalName = "hee:guidance")
@Node(jcrType = "hee:guidance")
public class Guidance extends BaseDocument {
    @HippoEssentialsGenerated(internalName = "hee:title")
    public String getTitle() {
        return getSingleProperty("hee:title");
    }

    @HippoEssentialsGenerated(internalName = "hee:summary")
    public String getSummary() {
        return getSingleProperty("hee:summary");
    }

    public List<?> getContentBlocks() {
        return getChildBeansByName("hee:contentBlocks");
    }

    @HippoEssentialsGenerated(internalName = "hee:quickLinks")
    public List<Link> getQuickLinks() {
        return getChildBeansByName("hee:quickLinks", Link.class);
    }

    @HippoEssentialsGenerated(internalName = "hee:relatedContent")
    public ContentCards getRelatedContent() {
        return getBean("hee:relatedContent", ContentCards.class);
    }
}

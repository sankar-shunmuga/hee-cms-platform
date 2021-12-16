package uk.nhs.hee.web.beans;

import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;
import org.hippoecm.hst.content.beans.Node;
import uk.nhs.hee.web.beans.ContentCards;

/** 
 * TODO: Beanwriter: Failed to create getter for node type: hippo:compound
 */
@HippoEssentialsGenerated(internalName = "hee:newsItem")
@Node(jcrType = "hee:newsItem")
public class NewsItem extends BaseDocument {
    @HippoEssentialsGenerated(internalName = "hee:title")
    public String getTitle() {
        return getSingleProperty("hee:title");
    }

    @HippoEssentialsGenerated(internalName = "hee:summary")
    public String getSummary() {
        return getSingleProperty("hee:summary");
    }

    @HippoEssentialsGenerated(internalName = "hee:author")
    public String getAuthor() {
        return getSingleProperty("hee:author");
    }

    @HippoEssentialsGenerated(internalName = "hee:categories")
    public String[] getCategories() {
        return getMultipleProperty("hee:categories");
    }

    @HippoEssentialsGenerated(internalName = "hee:relatedContent")
    public ContentCards getRelatedContent() {
        return getBean("hee:relatedContent", ContentCards.class);
    }
}

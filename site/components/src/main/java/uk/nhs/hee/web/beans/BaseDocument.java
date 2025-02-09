package uk.nhs.hee.web.beans;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoDocument;
import org.hippoecm.repository.HippoStdPubWfNodeType;
import uk.nhs.hee.web.utils.DateUtils;

import javax.jcr.RepositoryException;

@Node(jcrType="hee:basedocument")
public class BaseDocument extends HippoDocument {
    public String getPublishedDate() throws RepositoryException {
        return DateUtils.formatDate(
                getNode().getProperty(HippoStdPubWfNodeType.HIPPOSTDPUBWF_LAST_MODIFIED_DATE).getDate().getTime(),
                DateUtils.DD_MMMM_YYYY_PATTERN);
    }
}

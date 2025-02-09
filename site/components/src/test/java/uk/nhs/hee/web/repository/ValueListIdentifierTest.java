package uk.nhs.hee.web.repository;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class ValueListIdentifierTest {

    @Test
    public void valueListIdentifiers() {
        // Verify
        assertThat(ValueListIdentifier.BLOG_CATEGORIES.getName()).isEqualTo("blogCategories");
        assertThat(ValueListIdentifier.BULLETIN_CATEGORIES.getName()).isEqualTo("bulletinCategories");
        assertThat(ValueListIdentifier.CASE_STUDY_IMPACT_GROUPS.getName()).isEqualTo("caseStudyImpactGroups");
        assertThat(ValueListIdentifier.CASE_STUDY_IMPACT_TYPES.getName()).isEqualTo("caseStudyImpactTypes");
        assertThat(ValueListIdentifier.CASE_STUDY_SECTORS.getName()).isEqualTo("caseStudySectors");
        assertThat(ValueListIdentifier.CASE_STUDY_REGIONS.getName()).isEqualTo("caseStudyRegions");
        assertThat(ValueListIdentifier.CASE_STUDY_PROVIDERS.getName()).isEqualTo("caseStudyProviders");
        assertThat(ValueListIdentifier.LOGO_TYPES.getName()).isEqualTo("logoTypes");
        assertThat(ValueListIdentifier.NAV_MAP_REGIONS.getName()).isEqualTo("navMapRegions");
        assertThat(ValueListIdentifier.SEARCH_BANK_KEY_TERMS.getName()).isEqualTo("searchBankKeyTerms");
        assertThat(ValueListIdentifier.SEARCH_BANK_PROVIDERS.getName()).isEqualTo("searchBankProviders");
        assertThat(ValueListIdentifier.SEARCH_BANK_TOPICS.getName()).isEqualTo("searchBankTopics");
    }
}

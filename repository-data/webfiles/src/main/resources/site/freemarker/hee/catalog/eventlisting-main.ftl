<#ftl output_format="HTML">

<#include "../../include/imports.ftl">
<#include "../macros/list-item.ftl">
<#include "../macros/select.ftl">
<#include "../macros/checkbox-group.ftl">
<#include "../macros/hero-section.ftl">

<@hst.setBundle basename="uk.nhs.hee.web.listing"/>

<#-- @ftlvariable name="document" type="uk.nhs.hee.web.beans.ListingPage" -->
<#-- @ftlvariable name="pageable" type="org.onehippo.cms7.essentials.components.paging.Pageable" -->
<#-- @ftlvariable name="item" type="uk.nhs.hee.web.beans.Event" -->
<#-- @ftlvariable name="years" type="java.util.List" -->
<#-- @ftlvariable name="selectedYears" type="java.util.List" -->

<#if document??>
    <#assign showHero=false>
    <#if document.heroImage??>
        <#assign showHero=true>
        <@heroSection document=document />
    </#if>
    <main id="maincontent" role="main" class="nhsuk-main-wrapper" xmlns="http://www.w3.org/1999/html">
        <div class="nhsuk-width-container">
            <#if showHero=false>
                <h1>${document.title}</h1>
                <#if document.summary??>
                    <p class="nhsuk-lede-text"><@hst.html formattedText="${document.summary!?replace('\n', '<br>')}"/></p>
                </#if>
            </#if>
            <div class="nhsuk-listing">
                <div class="nhsuk-grid-row">
                    <div class="nhsuk-grid-column-one-third">
                        <#-- Filters -->
                        <@hst.renderURL var="pagelink"/>
                        <form class="nhsuk-filter" method="get" action="${pagelink}">
                            <@fmt.message key="filters.label" var="filtersLabel"/>
                            <p class="nhsuk-filter__title nhsuk-heading-l">${filtersLabel}</p>

                            <div class="nhsuk-filter__groups">
                                <@fmt.message key="filter.year.label" var="yearLabel"/>

                                <div class="nhsuk-filter__group">
                                    <@checkboxGroup title=yearLabel name="year" items=years selectedItemsList=selectedYears />
                                </div>
                            </div>
                            <input type="hidden" name="sortBy" value="${selectedSortOrder}">
                        </form>
                        <#-- End Filters -->
                    </div>

                    <div class="nhsuk-listing__list nhsuk-grid-column-two-thirds">
                        <div class="nhsuk-listing__summary o-flex@tablet">
                            <#-- Results number -->
                            <@fmt.message key="results.count.text" var="resultsCountText"/>
                            <h2 class="nhsuk-listing__title nhsuk-heading-l o-flex__grow">
                                ${pageable.total} ${resultsCountText}
                            </h2>
                            <#-- End Results number -->

                            <#-- Sort DropDown-->
                            <@hst.renderURL var="pagelink" />
                            <form method="get" class="nhsuk-listing__sort o-flex o-flex--align-center"
                                  action="${pagelink}">
                                <div class="o-flex__grow">
                                    <#list selectedYears as year>
                                        <input type="hidden" name="year" value="${year}">
                                    </#list>
                                    <@fmt.message key="sort.label" var="sortLabel"/>
                                    <@fmt.message key="sort.option.oldest" var="sortByOldestLabel"/>
                                    <@fmt.message key="sort.option.newest" var="sortByNewestLabel"/>
                                    <@fmt.message key="sort.option.az" var="sortByAZ"/>
                                    <#assign selectOptions= {"asc": "${sortByOldestLabel}", "desc":"${sortByNewestLabel}", "az":"${sortByAZ}"} />
                                    <@select label="${sortLabel}" name="sortBy" optionsMap=selectOptions selectedValue=selectedSortOrder/>
                                </div>
                            </form>
                            <#-- End Sort DropDown -->
                        </div>

                        <#-- Active Filters -->
                        <#if selectedYears?has_content>
                            <div class="nhsuk-listing__active-filters nhsuk-u-margin-bottom-5">
                                <#list selectedYears as year>
                                    <div class="nhsuk-filter-tag nhsuk-tag" data-filter="${year}">
                                        <span>${year}</span>
                                        <@hst.link path='/static/assets/icons/icon-close-white.svg' var="closeIcon"/>
                                        <img class="nhsuk-filter-tag__icon" src="${closeIcon}" alt="Remove" hidden/>
                                    </div>
                                </#list>
                            </div>
                        </#if>
                        <#-- End Active Filters -->

                        <#if pageable??>
                            <ul class="nhsuk-list nhsuk-list--border">
                                <@.vars["${document.listingPageType}ListItem"] items=pageable.items/>
                            </ul>
                            <#include "../../include/pagination-nhs.ftl">
                        </#if>
                    </div>
                </div>
            </div>
        </div>
    </main>
</#if>

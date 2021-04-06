<#ftl output_format="HTML">
<#assign hst=JspTaglibs["http://www.hippoecm.org/jsp/hst/core"] >
<#--<@hst.setBundle basename="uk.nhs.hee.web.bulletin"/>-->

<#macro bulletinListItem title category overview websiteURL websiteText>
    <dl class="nhsuk-summary-list">
        <h3><a href="${websiteURL}"> ${title}</a></h3>
        <#if category??>
            <@fmt.message key="bulletin.category" var="categoryLabel"/>
            <@listItemRow key="${categoryLabel}" value="${category}" />
        </#if>
        <#if overview??>
            <@fmt.message key="bulletin.overview" var="overviewLabel"/>
            <@listItemRow key="${overviewLabel}" value="${overview}" />
        </#if>
        <#if websiteURL??>
            <#assign website>
                <a href="${websiteURL}"> ${websiteText}</a>
            </#assign>
            <@fmt.message key="bulletin.website" var="websiteLabel"/>
            <@listItemRow key="${websiteLabel}" value="${website}" />
        </#if>
    </dl>
</#macro>
<#macro listItem listItem>
    <li>
        <span class="app-search-results-category">Components</span>
        <h3><a href="<@hst.link hippobean=listItem/>">${listItem.title}</a></h3>
        <div>${listItem.overview}</div>
    </li>
</#macro>

<#macro listItemRow key value>
    <div class="nhsuk-summary-list__row">
        <dt class="nhsuk-summary-list__key">
            ${key}
        </dt>
        <dd class="nhsuk-summary-list__value">
            ${value}
        </dd>
    </div>
</#macro>

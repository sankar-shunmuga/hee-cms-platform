<#macro anchorLinks anchor>
    <#if anchor??>
        <div class="nhsuk-anchor-links" data-headings="${anchor.headings}">
            <h2 data-anchorlinksignore="true">${anchor.title}</h2>
        </div>
    </#if>
</#macro>

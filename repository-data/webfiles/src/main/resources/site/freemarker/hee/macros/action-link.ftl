<#import "./link.ftl" as hlink>

<#macro actionLink actionLink>
    <#if actionLink??>
        <#assign href="${hlink.link(actionLink.link)}"/>

        <#if href?has_content>
            <div class="nhsuk-action-link">
                <a class="nhsuk-action-link__link" href="${href}" ${hlink.linkTarget(actionLink.link)}>
                    <svg class="nhsuk-icon nhsuk-icon__arrow-right-circle" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" aria-hidden="true">
                        <path d="M0 0h24v24H0z" fill="none"></path>
                        <path d="M12 2a10 10 0 0 0-9.95 9h11.64L9.74 7.05a1 1 0 0 1 1.41-1.41l5.66 5.65a1 1 0 0 1 0 1.42l-5.66 5.65a1 1 0 0 1-1.41 0 1 1 0 0 1 0-1.41L13.69 13H2.05A10 10 0 1 0 12 2z"></path>
                    </svg>
                    <span class="nhsuk-action-link__text">${actionLink.link.text}<@hlink.linkTextSuffix link=actionLink.link/></span>
                </a>
            </div>
        </#if>
    </#if>
</#macro>
<#include "../../include/imports.ftl">
<@hst.defineObjects />

<div class="nhsuk-width-container nhsuk-header__container">
    <#assign hasMultiOrgLogo=false>
    <@hst.include var="multiOrgLogoHTML" ref="multi-org-logo"/>

    <#if (multiOrgLogoHTML?index_of('nhsuk-header__multi-logo')) != -1>
        <#assign hasMultiOrgLogo=true>
    </#if>

    <#--  Multi org logo  -->
    ${multiOrgLogoHTML}

    <#--  Base logo and org name & descriptor: START  -->
    <div class="nhsuk-header__clear"></div>
    <div class="${hasMultiOrgLogo?then('nhsuk-header__multi-logo-organisation-name', 'nhsuk-header__logo')}">
        <a class="nhsuk-header__link" href="<@hst.link siteMapItemRefId="root"/>" aria-label="Home">
            <#if !hasMultiOrgLogo>
                <#--  Default/Base logo: START  -->
                <#--  Rendered if multi-logo isn't available  -->
                <svg class="nhsuk-logo" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" role="presentation" focusable="false" viewBox="0 0 40 16">
                    <path class="nhsuk-logo__background" d="M0 0h40v16H0z"></path>
                    <path class="nhsuk-logo__text" d="M3.9 1.5h4.4l2.6 9h.1l1.8-9h3.3l-2.8 13H9l-2.7-9h-.1l-1.8 9H1.1M17.3 1.5h3.6l-1 4.9h4L25 1.5h3.5l-2.7 13h-3.5l1.1-5.6h-4.1l-1.2 5.6h-3.4M37.7 4.4c-.7-.3-1.6-.6-2.9-.6-1.4 0-2.5.2-2.5 1.3 0 1.8 5.1 1.2 5.1 5.1 0 3.6-3.3 4.5-6.4 4.5-1.3 0-2.9-.3-4-.7l.8-2.7c.7.4 2.1.7 3.2.7s2.8-.2 2.8-1.5c0-2.1-5.1-1.3-5.1-5 0-3.4 2.9-4.4 5.8-4.4 1.6 0 3.1.2 4 .6"></path>
                    <image src="static/assets/logos/nhs-logo.pn" xlink:href=""></image>
                </svg>
                <#--  Default/Base logo: END  -->
            </#if>

            <#--  Base org name & descriptor: START  -->
            <#--  ${hstRequestContext.resolvedMount.mount.channelInfo.organisationName}
                    defaults to 'Health Education England' via 'uk.nhs.hee.web.channels.WebsiteInfo' ChannelInfo class  -->
            <span class="nhsuk-organisation-name">${hstRequestContext.resolvedMount.mount.channelInfo.organisationName}</span>
            <#if hstRequestContext.resolvedMount.mount.channelInfo.organisationDescriptor?has_content>
                <span class="nhsuk-organisation-descriptor">
                    ${hstRequestContext.resolvedMount.mount.channelInfo.organisationDescriptor}
                </span>
            </#if>
            <#--  Base org name & descriptor: END  -->
        </a>
    </div>
    <#--  Base logo and org name & descriptor: END  -->

    <div class="nhsuk-header__content" id="content-header">
        <div class="nhsuk-header__menu">
            <button class="nhsuk-header__menu-toggle" id="toggle-menu" aria-controls="header-navigation" aria-label="Open menu">Menu</button>
        </div>
        <div class="nhsuk-header__search">
            <button class="nhsuk-header__search-toggle" id="toggle-search" aria-controls="search" aria-label="Open search">
                <svg class="nhsuk-icon nhsuk-icon__search" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                    <path d="M19.71 18.29l-4.11-4.1a7 7 0 1 0-1.41 1.41l4.1 4.11a1 1 0 0 0 1.42 0 1 1 0 0 0 0-1.42zM5 10a5 5 0 1 1 5 5 5 5 0 0 1-5-5z"></path>
                </svg>
                <span class="nhsuk-u-visually-hidden">Search</span>
            </button>
            <div class="nhsuk-header__search-wrap" id="wrap-search">
                <@hst.link var="searchLink" siteMapItemRefId="search"/>
                <form class="nhsuk-header__search-form" id="search" action="${searchLink}" method="get" role="search">
                    <label class="nhsuk-u-visually-hidden" for="search-field">Search the HEE website</label>
                    <input class="nhsuk-search__input" id="search-field" name="q" type="search" placeholder="Search" autocomplete="off">
                    <button class="nhsuk-search__submit" type="submit">
                        <svg class="nhsuk-icon nhsuk-icon__search" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                            <path d="M19.71 18.29l-4.11-4.1a7 7 0 1 0-1.41 1.41l4.1 4.11a1 1 0 0 0 1.42 0 1 1 0 0 0 0-1.42zM5 10a5 5 0 1 1 5 5 5 5 0 0 1-5-5z"></path>
                        </svg>
                        <span class="nhsuk-u-visually-hidden">Search</span>
                    </button>
                    <button class="nhsuk-search__close" id="close-search">
                        <svg class="nhsuk-icon nhsuk-icon__close" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
                            <path d="M13.41 12l5.3-5.29a1 1 0 1 0-1.42-1.42L12 10.59l-5.29-5.3a1 1 0 0 0-1.42 1.42l5.3 5.29-5.3 5.29a1 1 0 0 0 0 1.42 1 1 0 0 0 1.42 0l5.29-5.3 5.29 5.3a1 1 0 0 0 1.42 0 1 1 0 0 0 0-1.42z"></path>
                        </svg>
                        <span class="nhsuk-u-visually-hidden">Close search</span>
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

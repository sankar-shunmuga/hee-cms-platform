<#include "../../include/imports.ftl">
<@hst.defineObjects />

<@hst.setBundle basename="uk.nhs.hee.web.homepage"/>
<section class="nhsuk-section">
  <div class="nhsuk-width-container">
    <h1><@fmt.message key="homepage.title" var="title"/>${title?html}</h1>
    <p><@fmt.message key="homepage.text" var="text"/>${text?html}</p>
    <#if !hstRequest.requestContext.channelManagerPreviewRequest>
      <p>
        [This text can be edited
        <a href="http://localhost:8080/cms/?1&path=/content/documents/administration/labels/homepage" target="_blank">here</a>.]
      </p>
    </#if>
  </div>
</section>
<div>
  <@hst.include ref="container"/>
</div>
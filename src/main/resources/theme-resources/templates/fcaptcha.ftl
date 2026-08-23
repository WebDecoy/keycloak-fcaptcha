<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=true; section>
  <#if section = "header">
    Human verification
  <#elseif section = "form">
    <form id="kc-fcaptcha-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
      <input id="fcaptcha-token" type="hidden" name="fcaptcha_token" value="">
      <div id="fcaptcha-widget"
           data-site-key="${fcaptchaSiteKey}"
           data-instance-url="${fcaptchaInstanceUrl}"></div>
      <div class="${properties.kcFormGroupClass!}">
        <input id="kc-fcaptcha-submit"
               class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
               type="submit" value="Continue" disabled>
      </div>
    </form>
    <script src="${fcaptchaScriptUrl}"></script>
    <script src="${url.resourcesCommonPath}/js/fcaptcha-init.js"></script>
  </#if>
</@layout.registrationLayout>


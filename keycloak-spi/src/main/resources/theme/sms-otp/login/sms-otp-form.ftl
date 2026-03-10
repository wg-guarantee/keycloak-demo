<#-- sms-otp-form.ftl  – Keycloak FreeMarker OTP input template
     Deploy location: /opt/keycloak/themes/<theme>/login/ -->
<#import "template.ftl" as layout>

<@layout.registrationLayout displayInfo=true; section>
    <#if section = "header">
        ${msg("smsOtpTitle", "SMS Verification")}
    <#elseif section = "form">
        <form id="kc-otp-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="sms_otp_code" class="${properties.kcLabelClass!}">
                        ${msg("smsOtpLabel", "Verification Code")}
                    </label>
                </div>

                <div class="${properties.kcInputWrapperClass!}">
                    <input id="sms_otp_code"
                           name="sms_otp_code"
                           autocomplete="one-time-code"
                           type="text"
                           inputmode="numeric"
                           pattern="[0-9]{6}"
                           maxlength="6"
                           class="${properties.kcInputClass!}"
                           autofocus
                           aria-label="${msg("smsOtpLabel", "Verification Code")}"/>
                </div>
            </div>

            <#if message?has_content && message.type = "error">
                <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
                    <div id="input-error" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                        ${kcSanitize(message.summary)?no_esc}
                    </div>
                </div>
            </#if>

            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}"
                           name="login"
                           id="kc-login"
                           type="submit"
                           value="${msg("doLogIn")}"/>
                </div>
            </div>
        </form>
    <#elseif section = "info">
        <p id="kc-otp-info">
            ${msg("smsOtpInfo", "A 6-digit code has been sent to") } <strong>${phoneHint!}</strong>
        </p>
    </#if>
</@layout.registrationLayout>

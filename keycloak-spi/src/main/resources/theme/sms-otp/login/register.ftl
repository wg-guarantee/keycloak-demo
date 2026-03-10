<#-- register.ftl – extends keycloak default registration page, adds phoneNumber field
     Placed in sms-otp theme so it's picked up when demo realm uses this theme. -->
<#import "template.ftl" as layout>

<@layout.registrationLayout displayMessage=!messagesPerField.existsError('email','username','password','password-confirm','phoneNumber','phone_otp'); section>

    <#if section = "header">
        ${msg("registerTitle")}

    <#elseif section = "form">
        <form id="kc-register-form" class="${properties.kcFormClass!}" action="${url.registrationAction}" method="post">

            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="email" class="${properties.kcLabelClass!}">${msg("email")}</label>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input type="text" id="email" class="${properties.kcInputClass!}"
                           name="email" value="${(register.formData['email']!'')}" autocomplete="email"
                           aria-invalid="<#if messagesPerField.existsError('email')>true</#if>"/>
                    <#if messagesPerField.existsError('email')>
                        <span id="input-error-email" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                            ${kcSanitize(messagesPerField.get('email'))?no_esc}
                        </span>
                    </#if>
                </div>
            </div>

            <#if !realm.registrationEmailAsUsername>
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="username" class="${properties.kcLabelClass!}">${msg("username")}</label>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input type="text" id="username" class="${properties.kcInputClass!}"
                           name="username" value="${(register.formData['username']!'')}" autocomplete="username"
                           aria-invalid="<#if messagesPerField.existsError('username')>true</#if>"/>
                    <#if messagesPerField.existsError('username')>
                        <span id="input-error-username" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                            ${kcSanitize(messagesPerField.get('username'))?no_esc}
                        </span>
                    </#if>
                </div>
            </div>
            </#if>

<#-- ── 手机号字段 + 发送验证码按钮 ─────────────────────────── -->
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="user.attributes.phoneNumber" class="${properties.kcLabelClass!}">
                        ${msg("phoneNumber")}
                    </label>
                </div>
                <div class="${properties.kcInputWrapperClass!}" style="display:flex;gap:8px;align-items:flex-start;">
                    <input type="tel" id="user.attributes.phoneNumber"
                           class="${properties.kcInputClass!}"
                           name="user.attributes.phoneNumber"
                           value="${(register.formData['user.attributes.phoneNumber']!'')}" 
                           placeholder="+8613800138000"
                           autocomplete="tel"
                           style="flex:1;"
                           aria-invalid="<#if messagesPerField.existsError('phoneNumber')>true</#if>"/>
                    <#-- type=button 避免触发 Keycloak 父主题的密码校验 JS，
                         点击后由 JS 直接调用 form.submit()（绕过 submit 事件监听器）-->
                    <button type="button"
                            id="btn-send-otp"
                            class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!}"
                            style="white-space:nowrap;flex-shrink:0;">
                        ${msg("sendPhoneOtp")}
                    </button>
                </div>
                <#-- 隐藏字段：点击发送按钮时由 JS 设置为 send_code，注册时清空 -->
                <input type="hidden" id="reg-action-input" name="reg_action" value="">
                <#if messagesPerField.existsError('phoneNumber')>
                    <span id="input-error-phone" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                        ${kcSanitize(messagesPerField.get('phoneNumber'))?no_esc}
                    </span>
                </#if>
            </div>

            <#-- ── 验证码输入框（始终显示，发送后输入）─────────────────────────── -->
            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="phone_otp" class="${properties.kcLabelClass!}">
                        ${msg("phoneOtpLabel")}
                    </label>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <#if phoneOtpSent??>
                    <p style="margin:0 0 6px;font-size:.85rem;color:#2e7d32;">
                        ${msg("phoneOtpSentHint", phoneOtpMasked!"")}
                    </p>
                    <#else>
                    <p style="margin:0 0 6px;font-size:.85rem;color:#888;">
                        ${msg("phoneOtpNotSentHint")}
                    </p>
                    </#if>
                    <input type="text" id="phone_otp" name="phone_otp"
                           class="${properties.kcInputClass!}"
                           inputmode="numeric" pattern="[0-9]{6}" maxlength="6"
                           placeholder="------"
                           autocomplete="one-time-code"
                           aria-invalid="<#if messagesPerField.existsError('phone_otp')>true</#if>"/>
                    <#if messagesPerField.existsError('phone_otp')>
                        <span id="input-error-phone-otp" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                            ${kcSanitize(messagesPerField.get('phone_otp'))?no_esc}
                        </span>
                    </#if>
                </div>
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="password" class="${properties.kcLabelClass!}">${msg("password")}</label>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input type="password" id="password" class="${properties.kcInputClass!}"
                           name="password" autocomplete="new-password"
                           aria-invalid="<#if messagesPerField.existsError('password','password-confirm')>true</#if>"/>
                    <#if messagesPerField.existsError('password')>
                        <span id="input-error-password" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                            ${kcSanitize(messagesPerField.get('password'))?no_esc}
                        </span>
                    </#if>
                </div>
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <div class="${properties.kcLabelWrapperClass!}">
                    <label for="password-confirm" class="${properties.kcLabelClass!}">${msg("passwordConfirm")}</label>
                </div>
                <div class="${properties.kcInputWrapperClass!}">
                    <input type="password" id="password-confirm" class="${properties.kcInputClass!}"
                           name="password-confirm" autocomplete="new-password"
                           aria-invalid="<#if messagesPerField.existsError('password-confirm')>true</#if>"/>
                    <#if messagesPerField.existsError('password-confirm')>
                        <span id="input-error-password-confirm" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                            ${kcSanitize(messagesPerField.get('password-confirm'))?no_esc}
                        </span>
                    </#if>
                </div>
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                           type="submit" value="${msg("doRegister")}"/>
                </div>
                <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                        <span><a href="${url.loginUrl}">${kcSanitize(msg("backToLogin"))?no_esc}</a></span>
                    </div>
                </div>
            </div>

        </form>

        <script>
        (function () {
            var COOLDOWN    = 60;
            var STORAGE_KEY = 'reg_otp_ts';
            var btn  = document.getElementById('btn-send-otp');
            var form = document.getElementById('kc-register-form');
            var regActionInput = document.getElementById('reg-action-input');
            if (!btn || !form) return;

            function startCountdown(remaining) {
                btn.disabled = true;
                if (!btn.dataset.original) btn.dataset.original = btn.textContent.trim();
                (function tick() {
                    if (remaining <= 0) {
                        btn.disabled = false;
                        btn.textContent = btn.dataset.original;
                        localStorage.removeItem(STORAGE_KEY);
                        return;
                    }
                    btn.textContent = remaining + 's';
                    remaining--;
                    setTimeout(tick, 1000);
                })();
            }

            // 页面重渲染后恢复倒计时（localStorage 跨页面保留）
            var saved = localStorage.getItem(STORAGE_KEY);
            if (saved) {
                var elapsed = Math.floor((Date.now() - parseInt(saved, 10)) / 1000);
                var left = COOLDOWN - elapsed;
                if (left > 0) startCountdown(left);
                else localStorage.removeItem(STORAGE_KEY);
            }

            btn.addEventListener('click', function () {
                var phoneInput = document.getElementById('user.attributes.phoneNumber');
                if (!phoneInput || !phoneInput.value.trim().match(/^\+[1-9]\d{6,14}$/)) {
                    phoneInput && phoneInput.focus();
                    return;
                }
                // 设置 reg_action=send_code
                regActionInput.value = 'send_code';
                // form.submit() 直接提交表单，不触发 submit 事件监听器
                // 从而绕过 Keycloak 父主题注入的密码校验 JS
                localStorage.setItem(STORAGE_KEY, Date.now().toString());
                startCountdown(COOLDOWN);
                form.submit();
            });

            // 点击"注册"按钮时清空 reg_action，确保走正常注册流程
            var registerBtn = form.querySelector('input[type="submit"]');
            if (registerBtn) {
                registerBtn.addEventListener('click', function () {
                    regActionInput.value = '';
                });
            }
        })();
        </script>
    </#if>
</@layout.registrationLayout>

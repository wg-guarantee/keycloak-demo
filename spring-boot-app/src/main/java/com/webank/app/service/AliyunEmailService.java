package com.webank.app.service;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.webank.app.config.AliyunConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class AliyunEmailService {
    
    private final AliyunConfig aliyunConfig;
    private IAcsClient client;
    
    private synchronized IAcsClient getClient() throws ClientException {
        if (client == null) {
            AliyunConfig.Email emailConfig = aliyunConfig.getEmail();
            DefaultProfile profile = DefaultProfile.getProfile(
                emailConfig.getRegionId(),
                emailConfig.getAccessKeyId(),
                emailConfig.getAccessKeySecret()
            );
            client = new DefaultAcsClient(profile);
        }
        return client;
    }
    
    /**
     * 发送邮件
     * @param toAddress 收件人邮箱
     * @param subject 邮件主题
     * @param htmlBody 邮件 HTML 内容
     * @return 邮件发送状态
     */
    public boolean sendEmail(String toAddress, String subject, String htmlBody) {
        if (!aliyunConfig.isEmailConfigured()) {
            log.warn("阿里云邮件未配置，无法发送实际邮件");
            return true;
        }
        
        try {
            AliyunConfig.Email emailConfig = aliyunConfig.getEmail();
            
            CommonRequest request = new CommonRequest();
            request.setMethod(MethodType.POST);
            request.setDomain("dm.aliyuncs.com");
            request.setVersion("2015-11-23");
            request.setAction("SingleSendMail");
            
            request.putQueryParameter("RegionId", emailConfig.getRegionId());
            request.putQueryParameter("AccountName", emailConfig.getFromAddress());
            request.putQueryParameter("FromAlias", emailConfig.getFromAlias());
            request.putQueryParameter("ToAddress", toAddress);
            request.putQueryParameter("Subject", subject);
            request.putQueryParameter("HtmlBody", htmlBody);
            request.putQueryParameter("ReplyToAddress", "false");
            
            CommonResponse response = getClient().getCommonResponse(request);
            
            if (response.getHttpStatus() == 200) {
                log.info("邮件发送成功 - 收件人: {}", toAddress);
                return true;
            } else {
                log.error("邮件发送失败 - 收件人: {}, HTTP 状态: {}, 响应: {}", 
                    toAddress, response.getHttpStatus(), response.getData());
                return false;
            }
        } catch (ClientException e) {
            log.error("邮件发送异常 - 收件人: {}", toAddress, e);
            return false;
        }
    }
    
    /**
     * 发送激活邮件
     */
    public boolean sendActivationEmail(String toAddress, String activationLink) {
        String subject = "账户激活";
        String htmlBody = buildActivationEmailHtml(activationLink);
        return sendEmail(toAddress, subject, htmlBody);
    }
    
    /**
     * 发送密码重置邮件
     */
    public boolean sendPasswordResetEmail(String toAddress, String resetLink) {
        String subject = "密码重置";
        String htmlBody = buildPasswordResetEmailHtml(resetLink);
        return sendEmail(toAddress, subject, htmlBody);
    }
    
    private String buildActivationEmailHtml(String activationLink) {
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <style>\n" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; }\n" +
            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n" +
            "        .header { background-color: #007bff; color: white; padding: 20px; text-align: center; border-radius: 5px; }\n" +
            "        .content { padding: 20px; background-color: #f9f9f9; }\n" +
            "        .button { display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px; margin-top: 10px; }\n" +
            "        .footer { text-align: center; padding: 10px; color: #666; font-size: 12px; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"header\">\n" +
            "            <h1>欢迎加入 Webank</h1>\n" +
            "        </div>\n" +
            "        <div class=\"content\">\n" +
            "            <p>尊敬的用户，</p>\n" +
            "            <p>感谢您注册 Webank 账户。请点击下方链接激活您的账户：</p>\n" +
            "            <a href=\"" + activationLink + "\" class=\"button\">激活账户</a>\n" +
            "            <p>或者复制此链接在浏览器中打开：</p>\n" +
            "            <p><code>" + activationLink + "</code></p>\n" +
            "            <p style=\"color: #666; font-size: 12px;\">链接有效期为 24 小时，请及时激活。</p>\n" +
            "        </div>\n" +
            "        <div class=\"footer\">\n" +
            "            <p>&copy; 2024 Webank. All rights reserved.</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";
    }
    
    private String buildPasswordResetEmailHtml(String resetLink) {
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <style>\n" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; }\n" +
            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n" +
            "        .header { background-color: #dc3545; color: white; padding: 20px; text-align: center; border-radius: 5px; }\n" +
            "        .content { padding: 20px; background-color: #f9f9f9; }\n" +
            "        .button { display: inline-block; padding: 10px 20px; background-color: #dc3545; color: white; text-decoration: none; border-radius: 5px; margin-top: 10px; }\n" +
            "        .footer { text-align: center; padding: 10px; color: #666; font-size: 12px; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"header\">\n" +
            "            <h1>密码重置</h1>\n" +
            "        </div>\n" +
            "        <div class=\"content\">\n" +
            "            <p>尊敬的用户，</p>\n" +
            "            <p>我们收到了您的密码重置请求。请点击下方链接重置您的密码：</p>\n" +
            "            <a href=\"" + resetLink + "\" class=\"button\">重置密码</a>\n" +
            "            <p>或者复制此链接在浏览器中打开：</p>\n" +
            "            <p><code>" + resetLink + "</code></p>\n" +
            "            <p style=\"color: #666; font-size: 12px;\">链接有效期为 24 小时。如果您没有请求重置密码，请忽略此邮件。</p>\n" +
            "        </div>\n" +
            "        <div class=\"footer\">\n" +
            "            <p>&copy; 2024 Webank. All rights reserved.</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";
    }
}

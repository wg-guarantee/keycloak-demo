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

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AliyunSmsService {
    
    private final AliyunConfig aliyunConfig;
    private IAcsClient client;
    
    private synchronized IAcsClient getClient() throws ClientException {
        if (client == null) {
            AliyunConfig.Sms smsConfig = aliyunConfig.getSms();
            DefaultProfile profile = DefaultProfile.getProfile(
                smsConfig.getRegionId(),
                smsConfig.getAccessKeyId(),
                smsConfig.getAccessKeySecret()
            );
            client = new DefaultAcsClient(profile);
        }
        return client;
    }
    
    /**
     * 发送短信验证码
     * @param phoneNumber 目标电话号码
     * @param verificationCode 验证码
     * @return 阿里云消息 ID
     */
    public String sendVerificationCode(String phoneNumber, String verificationCode) {
        if (!aliyunConfig.isSmsConfigured()) {
            log.warn("阿里云短信未配置，无法发送实际短信");
            return "MOCK_MESSAGE_ID_" + System.currentTimeMillis();
        }
        
        try {
            AliyunConfig.Sms smsConfig = aliyunConfig.getSms();
            
            CommonRequest request = new CommonRequest();
            request.setMethod(MethodType.POST);
            request.setDomain("dysmsapi.aliyuncs.com");
            request.setVersion("2017-05-25");
            request.setAction("SendSms");
            
            request.putQueryParameter("RegionId", smsConfig.getRegionId());
            request.putQueryParameter("PhoneNumbers", phoneNumber);
            request.putQueryParameter("SignName", smsConfig.getSignName());
            request.putQueryParameter("TemplateCode", smsConfig.getTemplateCode());
            
            // 构建模板参数
            Map<String, String> templateParams = new HashMap<>();
            templateParams.put("code", verificationCode);
            String json = new com.google.gson.Gson().toJson(templateParams);
            request.putQueryParameter("TemplateParam", json);
            
            CommonResponse response = getClient().getCommonResponse(request);
            
            if (response.getHttpStatus() == 200) {
                log.info("短信发送成功 - 电话: {}, 阿里云响应: {}", phoneNumber, response.getData());
                // 从响应中提取 MessageId
                String data = response.getData();
                if (data.contains("MessageId")) {
                    return extractMessageId(data);
                }
                return "OK";
            } else {
                log.error("短信发送失败 - 电话: {}, HTTP 状态: {}, 响应: {}", 
                    phoneNumber, response.getHttpStatus(), response.getData());
                throw new RuntimeException("短信发送失败: " + response.getData());
            }
        } catch (ClientException e) {
            log.error("短信发送异常 - 电话: {}", phoneNumber, e);
            throw new RuntimeException("短信发送异常: " + e.getMessage(), e);
        }
    }
    
    private String extractMessageId(String responseData) {
        try {
            // 简单的 JSON 解析，提取 MessageId
            int start = responseData.indexOf("\"MessageId\":\"");
            if (start != -1) {
                int end = responseData.indexOf("\"", start + 13);
                if (end != -1) {
                    return responseData.substring(start + 13, end);
                }
            }
        } catch (Exception e) {
            log.warn("无法从响应中提取 MessageId", e);
        }
        return "OK";
    }
}

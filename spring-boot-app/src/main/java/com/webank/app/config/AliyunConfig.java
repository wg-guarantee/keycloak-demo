package com.webank.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aliyun")
@Data
public class AliyunConfig {
    
    private Sms sms;
    private Email email;
    
    @Data
    public static class Sms {
        private String accessKeyId;
        private String accessKeySecret;
        private String signName;
        private String templateCode;
        private String regionId = "cn-hangzhou";
        private String endpoint = "dysmsapi.aliyuncs.com";
    }
    
    @Data
    public static class Email {
        private String accessKeyId;
        private String accessKeySecret;
        private String fromAddress;
        private String fromAlias;
        private String regionId = "cn-hangzhou";
        private String endpoint = "dm.aliyuncs.com";
    }
    
    public boolean isSmsConfigured() {
        return sms != null && sms.getAccessKeyId() != null && !sms.getAccessKeyId().isEmpty();
    }
    
    public boolean isEmailConfigured() {
        return email != null && email.getAccessKeyId() != null && !email.getAccessKeyId().isEmpty();
    }
}

package com.vidara.tradecenter.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payhere")
public class PayHereProperties {

    private String merchantId;
    private String merchantSecret;
    private boolean sandbox = true;
    private String frontendUrl;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantSecret() {
        return merchantSecret;
    }

    public void setMerchantSecret(String merchantSecret) {
        this.merchantSecret = merchantSecret;
    }

    public boolean isSandbox() {
        return sandbox;
    }

    public void setSandbox(boolean sandbox) {
        this.sandbox = sandbox;
    }

    public String getFrontendUrl() {
        return frontendUrl;
    }

    public void setFrontendUrl(String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }
}

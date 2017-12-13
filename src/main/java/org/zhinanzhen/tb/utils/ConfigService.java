package org.zhinanzhen.tb.utils;

import lombok.Setter;

@Setter
public class ConfigService {
    private String host;
    private String appId;
    private String secret;
    private String key;
    private String clientId;
    private String clientSecret;
    private String mode;
    private String ipnUrl;
    private String partneCode;
    private String nonceStr;
    private String credentialCode;
    private double introduceAward;
    private String staticUrl;
    private String IosMerchantId;
    private String IosPublicKey;
    private String IosPrivateKey;
    private String IosRegist;
    public String getHost() {
	return host;
    }

    public String getAppId() {
	return appId;
    }

    public String getSecret() {
	return secret;
    }

    public String getKey() {
	return key;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getMode() {
        return mode;
    }

    public String getIpnUrl() {
        return ipnUrl;
    }

    public String getPartneCode() {
        return partneCode;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public String getCredentialCode() {
        return credentialCode;
    }

    public double getIntroduceAward() {
        return introduceAward;
    }

    public String getStaticUrl() {
        return staticUrl;
    }

    public String getIosMerchantId() {
        return IosMerchantId;
    }

    public String getIosPublicKey() {
        return IosPublicKey;
    }

    public String getIosPrivateKey() {
        return IosPrivateKey;
    }

    public String getIosRegist() {
        return IosRegist;
    }
}
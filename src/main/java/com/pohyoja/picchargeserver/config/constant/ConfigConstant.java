package com.pohyoja.picchargeserver.config.constant;

public class ConfigConstant {
    private ConfigConstant() {
    }

    public static final String EXPECTED_AUDIENCE = "piccharge-afbc7";
    public static final String EXPECTED_ISSUER = "https://securetoken.google.com/" + EXPECTED_AUDIENCE;
}

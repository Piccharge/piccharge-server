package com.pohyoja.picchargeserver.domain.family.util;

import java.util.Random;

public class InviteCodeGenerator {
    public static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static final Random RANDOM = new Random();

    private InviteCodeGenerator() {}

    public static String generateRandomCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CHAR_POOL.charAt(RANDOM.nextInt(CHAR_POOL.length())));
        }
        return sb.toString().toUpperCase();
    }
}

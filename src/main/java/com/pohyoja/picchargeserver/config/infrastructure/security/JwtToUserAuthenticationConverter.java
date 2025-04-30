package com.pohyoja.picchargeserver.config.infrastructure.security;

import static com.pohyoja.picchargeserver.config.constant.ConfigConstant.EXPECTED_AUDIENCE;
import static com.pohyoja.picchargeserver.config.constant.ConfigConstant.EXPECTED_ISSUER;

import com.pohyoja.picchargeserver.common.security.JwtUserDetails;
import java.util.Collection;
import java.util.Objects;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
public class JwtToUserAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {


    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // 1) issuer 검증
        String issuer = jwt.getIssuer().toString();
        if (!EXPECTED_ISSUER.equals(issuer)) {
            throw new JwtException("Invalid issuer: " + issuer);
        }

        // 2) audience 검증
        Collection<String> audience = jwt.getAudience();
        if (audience == null || !audience.contains(EXPECTED_AUDIENCE)) {
            throw new JwtException("Invalid audience: " + audience);
        }

        // 3) 필수 클레임(user_id, email) 존재 검증
        String uid = jwt.getClaimAsString("user_id");
        String email = jwt.getClaimAsString("email");
        if (Objects.isNull(uid) || Objects.isNull(email)) {
            throw new JwtException("Missing required claims");
        }

        JwtUserDetails principal = new JwtUserDetails(uid, email);

        return new UsernamePasswordAuthenticationToken(
                principal,
                jwt.getTokenValue(),
                principal.getAuthorities()
        );
    }
}

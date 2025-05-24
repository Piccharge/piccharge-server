package com.pohyoja.picchargeserver.config.infrastructure.security;

import static com.pohyoja.picchargeserver.config.constant.ConfigConstant.EXPECTED_AUDIENCE;
import static com.pohyoja.picchargeserver.config.constant.ConfigConstant.EXPECTED_ISSUER;

import com.pohyoja.picchargeserver.common.security.JwtUserDetails;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
public class JwtToUserAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String ADMIN_EMAIL = "child@test.com";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        validateToken(jwt);

        String uid = jwt.getClaimAsString("user_id");
        String email = jwt.getClaimAsString("email");

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (ADMIN_EMAIL.equals(email)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        JwtUserDetails principal = new JwtUserDetails(uid, email, authorities);

        return new UsernamePasswordAuthenticationToken(
                principal,
                jwt.getTokenValue(),
                authorities
        );
    }

    private void validateToken(Jwt jwt) {
        // issuer, audience 검증
        if (!EXPECTED_ISSUER.equals(jwt.getIssuer().toString()) ||
                !jwt.getAudience().contains(EXPECTED_AUDIENCE)) {
            throw new JwtException("Invalid token issuer or audience");
        }

        // claims 검증
        if (Objects.isNull(jwt.getClaimAsString("user_id")) ||
                Objects.isNull(jwt.getClaimAsString("email"))) {
            throw new JwtException("Missing required claims");
        }
    }
}

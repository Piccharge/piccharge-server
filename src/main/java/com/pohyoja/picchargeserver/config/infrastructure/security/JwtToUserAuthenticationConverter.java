package com.pohyoja.picchargeserver.config.infrastructure.security;

import static com.pohyoja.picchargeserver.config.constant.ConfigConstant.EXPECTED_AUDIENCE;
import static com.pohyoja.picchargeserver.config.constant.ConfigConstant.EXPECTED_ISSUER;

import com.pohyoja.picchargeserver.common.security.JwtUserDetails;
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
        validateToken(jwt);

        String uid = jwt.getClaimAsString("user_id");
        String email = jwt.getClaimAsString("email");

        JwtUserDetails principal = new JwtUserDetails(uid, email);

        return new UsernamePasswordAuthenticationToken(
                principal,
                jwt.getTokenValue(),
                principal.getAuthorities()
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

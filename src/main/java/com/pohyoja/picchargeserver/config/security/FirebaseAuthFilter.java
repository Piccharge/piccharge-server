package com.pohyoja.picchargeserver.config.security;

import com.google.firebase.ErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.pohyoja.picchargeserver.global.exception.CustomAuthErrorCode;
import com.pohyoja.picchargeserver.global.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class FirebaseAuthFilter extends OncePerRequestFilter {
    private final FirebaseAuth firebaseAuth;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String idToken = header.substring(7);
        try {
            FirebaseToken decoded = firebaseAuth.verifyIdToken(idToken);
            Authentication authentication = createAuthentication(decoded);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Firebase 인증 성공 - uid: {}, email: {}", decoded.getUid(), decoded.getEmail());

        } catch (FirebaseAuthException e) {
            log.warn("Firebase 토큰 검증 실패: {}", e.getMessage());

            CustomAuthErrorCode errorCode = mapFirebaseErrorToAuthError(e.getErrorCode());
            throw new CustomException(errorCode);

        } catch (IllegalArgumentException e) {
            log.warn("Authorization 헤더가 잘못된 형식입니다.");
            throw new CustomException(CustomAuthErrorCode.EMPTY_TOKEN);
        }

        filterChain.doFilter(request, response);
    }

    private Authentication createAuthentication(FirebaseToken decodedToken) {
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .uid(decodedToken.getUid())
                .email(decodedToken.getEmail())
                .build();

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                Collections.emptyList()
        );
    }

    private CustomAuthErrorCode mapFirebaseErrorToAuthError(ErrorCode code) {
        return switch (code) {
            case UNAUTHENTICATED -> CustomAuthErrorCode.EXPIRED_TOKEN;
            case INVALID_ARGUMENT, FAILED_PRECONDITION -> CustomAuthErrorCode.INVALID_TOKEN_TYPE;
            case PERMISSION_DENIED -> CustomAuthErrorCode.WRONG_TOKEN_SIGNATURE;
            case INTERNAL, UNKNOWN, DATA_LOSS, DEADLINE_EXCEEDED -> CustomAuthErrorCode.TOKEN_VALIDATION_FAIL;
            default -> CustomAuthErrorCode.INVALID_TOKEN;
        };
    }
}
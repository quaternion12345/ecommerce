package com.example.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
//@Configuration
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private Environment env;

    public JwtAuthenticationFilter(Environment env) {
        this.env = env;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("JWT Filter Starts");

        String jwtToken = resolveToken(request);

        log.debug("Token Information is ", jwtToken);

        if(jwtToken == null) response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        else{
            // 토큰 검증
            Claims claims = null;
            try {
                claims = Jwts.parser()
                        .setSigningKey(env.getProperty("token.secret"))
                        .parseClaimsJws(jwtToken)
                        .getBody();
            }catch(Exception e){
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            String userIdFromToken = claims.getSubject();
            log.debug("userId from Token is ", userIdFromToken);

            if(userIdFromToken == null || userIdFromToken.isEmpty()){
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String userIdFromURI = null;
            try{
                userIdFromURI = request.getContextPath().split("/")[1];
            }catch(IndexOutOfBoundsException e){}
            log.debug("userId from URI is ", userIdFromURI);

            if(userIdFromURI == null || userIdFromURI.isEmpty()){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if(userIdFromURI != userIdFromToken){
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            log.debug("JWT Filter Success");
            filterChain.doFilter(request, response);
        }
    }

    //Request Header에서 토큰 정보를 꺼내오는 로직
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

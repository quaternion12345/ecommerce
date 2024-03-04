package com.example.apigatewayservice.filter;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class UserAuthorizationFilter extends AbstractGatewayFilterFactory<UserAuthorizationFilter.Config> {
    Environment env;

    public UserAuthorizationFilter(Environment env){
        super(Config.class);
        this.env = env;
    }
    public static class Config{

    }

    // Login -> issue Token -> User(with Token) -> Header(include Token)
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            log.debug("JWT Filter Starts");
            ServerHttpRequest request =  exchange.getRequest();

            if(!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION))
                return onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);

            String jwtToken = resolveToken(request);
            log.debug("Token Information is {}", jwtToken);

            if(jwtToken == null) return onError(exchange, "Invalid Token format", HttpStatus.UNAUTHORIZED);

            // 토큰 검증
            String subject = null;
            try {
                subject = Jwts.parser()
                        .setSigningKey(env.getProperty("token.secret"))
                        .parseClaimsJws(jwtToken)
                        .getBody()
                        .getSubject();
            }catch(Exception e){
                return onError(exchange, "JWT Token is Invalid", HttpStatus.UNAUTHORIZED);
            }

            if(subject == null || subject.isEmpty()) return onError(exchange, "JWT Token is Invalid", HttpStatus.UNAUTHORIZED);

            log.debug("userId from Token is {}", subject);

            // userID 검증
            String userIdFromURI = null;
            try {
                userIdFromURI = request.getURI().getPath().split("/")[1];
            }catch(IndexOutOfBoundsException e){ }

            log.debug("userId from URI is {}", userIdFromURI);

            if(userIdFromURI == null || userIdFromURI.isEmpty()) return onError(exchange, "Request URI is Invalid", HttpStatus.BAD_REQUEST);

            if(!userIdFromURI.equals(subject)) return onError(exchange, "You don't have permission to edit", HttpStatus.FORBIDDEN);

            log.debug("JWT Filter Success");

            return chain.filter(exchange);
        };
    }

    //Request Header에서 토큰 정보를 꺼내오는 로직
    private String resolveToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Spring MVC 대신, Spring WebFlux를 사용하여 비동기방식으로 데이터를 처리
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);

        byte[] bytes = err.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);

        log.error(err);

        return response.writeWith(Flux.just(buffer));
    }
}

package co.kr.nakdong.config;

import co.kr.nakdong.entity.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.impl.JWTParser;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JWTUtil {

    private static final Logger logger = LoggerFactory.getLogger(JWTUtil.class);

    public static String SECRET;

    @Value("${jwt.secret}")
    public void setSECRET(String value) {
        SECRET = value;
    }

    //    private static final Algorithm ALGORITHM = Algorithm.HMAC512(secret);
    private static final long AUTH_TIME = 60; //1시간
    private static final long REFRESH_TIME = 60 * 60 * 24 * 7;//일주일

    public static Algorithm algorithm(String secret) {
        return Algorithm.HMAC512(secret);
    }

    public static String makeAuthToken(User user) {
        String authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return JWT.create().withSubject(user.getUsername())
                .withClaim("exp", Instant.now().getEpochSecond() + AUTH_TIME)
                .withClaim("auth", authorities)
                .sign(algorithm(SECRET));
    }

    public static String makeRefreshToken(User user) {
        String authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return JWT.create().withSubject(user.getUsername())
                .withClaim("exp", Instant.now().getEpochSecond() + REFRESH_TIME)
                .withClaim("auth", authorities)
                .sign(algorithm(SECRET));
    }

    public static VerifyResult verify(String token) {
        try {
            DecodedJWT verify = JWT.require(algorithm(SECRET)).build().verify(token);
            return VerifyResult.builder().success(true)
                    .username(verify.getSubject()).build();
        } catch (TokenExpiredException expiredException) {
            DecodedJWT decode = JWT.decode(token);
            return VerifyResult.builder().success(false)
                    .exception(expiredException).build();
        } catch (InvalidClaimException invalidClaimException) {

            DecodedJWT decode = JWT.decode(token);
            return VerifyResult.builder().success(false)
                    .exception(invalidClaimException).build();
        } catch (JWTVerificationException verificationException) {
            DecodedJWT decode = JWT.decode(token);
            return VerifyResult.builder().success(false)
                    .exception(verificationException).build();
        }

    }
}

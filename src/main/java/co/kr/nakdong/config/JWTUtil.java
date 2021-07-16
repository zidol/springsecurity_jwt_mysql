package co.kr.nakdong.config;

import co.kr.nakdong.entity.author.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.sql.Date;
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

    private static long AUTH_TIME; //1시간

    @Value("${jwt.authTime}")
    public void setAuthTime(long value) {
        AUTH_TIME = value;
    }

    private static long REFRESH_TIME;//일주일

    @Value("${jwt.refreshTime}")
    public void setRefreshTime(long value) {
        REFRESH_TIME = value;
    }

    private static final Instant now = Instant.now();

    public static Algorithm algorithm(String secret) {
        return Algorithm.HMAC512(secret);
    }

    public static String makeAuthToken(User user) {
        String authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return JWT.create().withSubject(user.getUsername())
                .withClaim("exp", Instant.now().getEpochSecond() + AUTH_TIME)
                .withIssuedAt(Date.from(now))
                .withClaim("auth", authorities)
                .sign(algorithm(SECRET));
    }

    public static String makeRefreshToken(User user) {
        String authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        System.out.println("REFRESH_TIME : " + REFRESH_TIME);
        return JWT.create().withSubject(user.getUsername())
                .withClaim("exp", Instant.now().getEpochSecond() + REFRESH_TIME)
                .withIssuedAt(Date.from(now))
                .withClaim("auth", authorities)
                .sign(algorithm(SECRET));
    }

    public static VerifyResult verify(String token) {
        try {
            DecodedJWT verify = getDecodedJWT(token);
            return VerifyResult.builder().success(true)
                    .username(verify.getSubject()).build();
        } catch (TokenExpiredException e) {
            logger.info("토큰의 유효기간이 만료 되었습니다.");
            DecodedJWT decode = JWT.decode(token);
            return VerifyResult.builder().success(false)
                    .username(decode.getSubject()).exception(e).build();
        } catch (InvalidClaimException e) {
            logger.info("토큰의 클레임 정보가 유효하지 않습니다.");
            DecodedJWT decode = JWT.decode(token);
            return VerifyResult.builder().success(false)
                    .username(decode.getSubject()).exception(e).build();
        } catch (SignatureVerificationException e) {
            logger.info("토큰의 시그니쳐가 유효하지 않습니다.");
            DecodedJWT decode = JWT.decode(token);
            return VerifyResult.builder().success(false)
                    .username(decode.getSubject()).exception(e).build();
        } catch (JWTVerificationException e) {
            logger.info("토큰이 잘못 되었습니다.");
            DecodedJWT decode = JWT.decode(token);
            return VerifyResult.builder().success(false)
                    .username(decode.getSubject()).exception(e).build();
        }

    }

    private static DecodedJWT getDecodedJWT(String token) {
        DecodedJWT verify = JWT.require(algorithm(SECRET)).build().verify(token);
        return verify;
    }
}

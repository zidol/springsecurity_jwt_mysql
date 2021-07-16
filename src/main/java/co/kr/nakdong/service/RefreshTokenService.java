package co.kr.nakdong.service;

import co.kr.nakdong.config.JWTUtil;
import co.kr.nakdong.entity.author.RefreshToken;
import co.kr.nakdong.exception.TokenRefreshException;
import co.kr.nakdong.repository.RefreshTokenRepository;
import co.kr.nakdong.repository.UserRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
//  @Value("${bezkoder.app.jwtRefreshExpirationMs}")
//  private Long refreshTokenDurationMs;

  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;

  @Value("${jwt.secret}")
  private String SECRET;

  public Optional<RefreshToken> findByToken(String token) {
    return refreshTokenRepository.findByToken(token);
  }

  public Algorithm algorithm(String secret) {
    return Algorithm.HMAC512(secret);
  }
  public RefreshToken createRefreshToken(Long userId, String token) {
    RefreshToken refreshToken = new RefreshToken();

    DecodedJWT verify = JWT.require(algorithm(SECRET)).build().verify(token);
    Date expiresAt = verify.getExpiresAt();

    refreshToken.setUser(userRepository.findById(userId).get());

    refreshToken.setExpiryDate(expiresAt.toInstant());
    refreshToken.setToken(token);

    refreshToken = refreshTokenRepository.save(refreshToken);

    return refreshToken;
  }

  public RefreshToken verifyExpiration(RefreshToken token) {
    if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
      refreshTokenRepository.delete(token);
      throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
    }

    return token;
  }

  @Transactional
  public int deleteByUserId(String username) {
    return refreshTokenRepository.deleteByUser(userRepository.findByEmail(username).get());
  }
}

package co.kr.nakdong.config;


import co.kr.nakdong.dto.UserDto;
import co.kr.nakdong.dto.UserLoginDto;
import co.kr.nakdong.entity.author.RefreshToken;
import co.kr.nakdong.entity.author.User;
import co.kr.nakdong.service.RefreshTokenService;
import co.kr.nakdong.service.UserService;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JWTLoginFilter extends UsernamePasswordAuthenticationFilter {


    private static final Logger logger = LoggerFactory.getLogger(JWTLoginFilter.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private ObjectMapper objectMapper = new ObjectMapper();

    public JWTLoginFilter(AuthenticationManager authenticationManager, UserService userService, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;

//        setFilterProcessesUrl("/login");//로그인 url설정
        this.setAuthenticationSuccessHandler(new LoginSuccessHandler());
        this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/login","POST"));
    }

    @SneakyThrows
    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException
    {
        UserLoginDto userLogin = objectMapper.readValue(request.getInputStream(), UserLoginDto.class);
        String requestURI = request.getRequestURI();
        if(userLogin.getRefreshToken() == null) {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    userLogin.getUsername(), userLogin.getPassword(), null
            );
            // user details...
            return authenticationManager.authenticate(token);
        } else {
            VerifyResult verify = JWTUtil.verify(userLogin.getRefreshToken());
            if(verify.isSuccess()){
                User user = (User) userService.loadUserByUsername(verify.getUsername());
                return new UsernamePasswordAuthenticationToken(
                        user, user.getAuthorities()
                );
            } else {
                logger.debug("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
                throw new JWTVerificationException("토큰이 잘못되었습니다.");
            }
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult) throws IOException, ServletException
    {
        User user = (User) authResult.getPrincipal();
        //인증 성공시 access 토큰, refresh 토큰 재발급
        String accessToken = JWTUtil.makeAuthToken(user);
        String refreshToken = JWTUtil.makeRefreshToken(user);
//        response.setHeader("access_token", accessToken);
//        response.setHeader("refresh_token", refreshToken);
        
        //재발급 받은 refresh 토큰 DB에 저장
        refreshTokenService.createRefreshToken(user.getUserId(), refreshToken);
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        Cookie cookie = new Cookie("refreshToken",refreshToken);

        // expires in 7 days
        cookie.setMaxAge(7 * 24 * 60 * 60);

        // optional properties
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        // add cookie to response
        response.addCookie(cookie);

        UserDto userDto = UserDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .authorities(user.getAuthorities())
                .build();

        //access token
        Map<String, Object> map = new HashMap<>();
        map.put("token", accessToken);
        map.put("user", userDto);
        response.getOutputStream().write(objectMapper.writeValueAsBytes(map));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}

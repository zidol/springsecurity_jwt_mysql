package co.kr.nakdong.config;

import co.kr.nakdong.dto.UserLoginDto;
import co.kr.nakdong.entity.User;
import co.kr.nakdong.service.UserService;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InvalidClassException;
import java.util.HashMap;
import java.util.Map;

public class JWTLoginFilter extends UsernamePasswordAuthenticationFilter {


    private static final Logger logger = LoggerFactory.getLogger(JWTLoginFilter.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private ObjectMapper objectMapper = new ObjectMapper();

    public JWTLoginFilter(AuthenticationManager authenticationManager, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;

//        setFilterProcessesUrl("/login");//로그인 url설정
        this.setAuthenticationSuccessHandler(new LoginSuccessHandler());
        this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/login","POST"));
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
//                logger.debug("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
                if (verify.getException() instanceof TokenExpiredException) {
                    throw new TokenExpiredException("토큰 기간이 만료 되었습니다.");
                } else if (verify.getException() instanceof InvalidClassException) {
                    throw new InvalidClassException("유효한 claim값이 아닙니다.");
                } else {
                    throw new JWTVerificationException("인증이 받지 못한 토큰입니다.");
                }
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
        String accessToken = JWTUtil.makeAuthToken(user);
        String refreshToken = JWTUtil.makeRefreshToken(user);
        response.setHeader("access_token", accessToken);
//        response.setHeader("refresh_token", refreshToken);
        userService.updateRefreshToken(user.getUsername(), refreshToken);
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> map = new HashMap<>();
        map.put("user", user);
        map.put("token", accessToken);
        response.getOutputStream().write(objectMapper.writeValueAsBytes(map));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}

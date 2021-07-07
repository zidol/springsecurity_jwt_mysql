package co.kr.nakdong.config;

import co.kr.nakdong.entity.User;
import co.kr.nakdong.service.UserService;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
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

public class JWTLoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private ObjectMapper objectMapper = new ObjectMapper();
    private UserService userService;

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
        UserLoginForm userLogin = objectMapper.readValue(request.getInputStream(), UserLoginForm.class);
        if(userLogin.getRefreshToken() == null) {
            System.out.println("userLogin = " + userLogin);
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    userLogin.getUsername(), userLogin.getPassword(), null
            );
            System.out.println("token = " + token);
            // user details...
            return authenticationManager.authenticate(token);
        }else{
            VerifyResult verify = JWTUtil.verify(userLogin.getRefreshToken());
            if(verify.isSuccess()){
                User user = (User) userService.loadUserByUsername(verify.getUsername());
                return new UsernamePasswordAuthenticationToken(
                        user, user.getAuthorities()
                );
            }else{
                throw new TokenExpiredException("refresh token expired");
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
        response.setHeader("auth_token", JWTUtil.makeAuthToken(user));
        response.setHeader("refresh_token", JWTUtil.makeRefreshToken(user));
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getOutputStream().write(objectMapper.writeValueAsBytes(user));
    }
}

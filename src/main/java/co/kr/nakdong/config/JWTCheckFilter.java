package co.kr.nakdong.config;

import co.kr.nakdong.entity.User;
import co.kr.nakdong.service.UserService;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JWTCheckFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(JWTCheckFilter.class);
    private UserService userService;

    public JWTCheckFilter(UserService userService) {
//        super(authenticationManager);
        this.userService = userService;
    }

    //토큰에 대한 검사
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String bearer = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        String requestURI = httpServletRequest.getRequestURI();
        if(bearer == null || !bearer.startsWith("Bearer ")){
            chain.doFilter(request, response);
            return;
        }
        String token = bearer.substring("Bearer ".length());
        VerifyResult result = JWTUtil.verify(token);
        if(result.isSuccess()){
            User user = (User) userService.loadUserByUsername(result.getUsername());
            UsernamePasswordAuthenticationToken userToken = new UsernamePasswordAuthenticationToken(
                    user.getUsername(), null, user.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(userToken);
            chain.doFilter(request, response);
        }else{
            if (result.getException() instanceof TokenExpiredException) {
                httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token.");
            } else {
                httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid token.");
            }
        }
    }
}

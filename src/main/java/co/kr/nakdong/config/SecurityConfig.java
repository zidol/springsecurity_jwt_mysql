package co.kr.nakdong.config;

import co.kr.nakdong.entity.author.RefreshToken;
import co.kr.nakdong.service.RefreshTokenService;
import co.kr.nakdong.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

//    @Override //테스트용 인메모리에 저장
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication()
//                .withUser(
//                        User.withDefaultPasswordEncoder()
//                                .username("user1")
//                                .password("1111")
//                                .roles("USER")
//                                .build()
//                );
//    }

    private final CorsFilter corsFilter;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


//    @Bean
//    public JWTLoginFilter jwtLoginFilter(UserService userService) throws Exception {
//        JWTLoginFilter filter = new JWTLoginFilter(userService);
//        filter.setAuthenticationManager(authenticationManagerBean());
//        return filter;
//    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        JWTLoginFilter loginFilter = new JWTLoginFilter(authenticationManagerBean(), userService, refreshTokenService);
        JWTCheckFilter checkFilter = new JWTCheckFilter(userService);

        http
                .csrf().disable()
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)//로그인 필터
                .addFilterAt(checkFilter, BasicAuthenticationFilter.class);// 토큰 검증 필터

    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers(
                        "/h2-console/**"
                        ,"/favicon.ico"
                        ,"/error"
                );
    }
}

package co.kr.nakdong.config;

import co.kr.nakdong.entity.author.Authority;
import co.kr.nakdong.entity.author.User;
import co.kr.nakdong.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DBInit implements CommandLineRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {


        User admin = User.builder()
                .name("admin")
                .email("admin@test.com")
                .password(passwordEncoder.encode("1234"))
                .enabled(true)
                .build();
        admin = userService.save(admin);

        Authority authority = new Authority(admin.getUserId(), Authority.ROLE_ADMIN);
        Authority authority1 = new Authority(admin.getUserId(), Authority.ROLE_USER);
        Set<Authority> authorities = new HashSet<>();
        authorities.add(authority);
        authorities.add(authority1);
        admin.setAuthorities(authorities);
        userService.save(admin);

        User user1 = User.builder()
                .name("user1")
                .email("user1@test.com")
                .password(passwordEncoder.encode("1234"))
                .enabled(true)
                .build();
        user1 = userService.save(user1);

        Authority user1authority1 = new Authority(user1.getUserId(), Authority.ROLE_USER);
        Set<Authority> authorities2 = new HashSet<>();
        authorities2.add(user1authority1);
        user1.setAuthorities(authorities2);
        userService.save(user1);

    }


}

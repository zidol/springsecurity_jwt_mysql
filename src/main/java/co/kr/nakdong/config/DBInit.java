package co.kr.nakdong.config;

import co.kr.nakdong.entity.author.Authority;
import co.kr.nakdong.entity.author.ERole;
import co.kr.nakdong.entity.author.Role;
import co.kr.nakdong.entity.author.User;
import co.kr.nakdong.repository.AuthorityRepository;
import co.kr.nakdong.repository.RoleRepository;
import co.kr.nakdong.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.Root;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DBInit implements CommandLineRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityRepository authorityRepository;
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {


        User admin = User.builder()
                .name("admin")
                .email("admin@test.com")
                .password(passwordEncoder.encode("1234"))
                .enabled(true)
                .build();
        admin = userService.save(admin);

        Role role1 = Role.builder().authority(ERole.ROLE_ADMIN).build();
        Role save = roleRepository.save(role1);

        Role role2 = Role.builder().authority(ERole.ROLE_USER).build();
        Role save1 = roleRepository.save(role2);

        Authority authority = Authority.builder().user(admin).authority(save).build();
        authorityRepository.save(authority);
        Authority authority1 = Authority.builder().user(admin).authority(save1).build();
        authorityRepository.save(authority1);
//        Set<Authority> authorities = new HashSet<>();
//        authorities.add(authority);
//        authorities.add(authority1);
//        admin.setAuthorities(authorities);
//        userService.save(admin);

        User user1 = User.builder()
                .name("user1")
                .email("user1@test.com")
                .password(passwordEncoder.encode("1234"))
                .enabled(true)
                .build();
        user1 = userService.save(user1);

        Authority user1authority1 = new Authority(3L, user1, role2);
        Set<Authority> authorities2 = new HashSet<>();
        authorities2.add(user1authority1);
        user1.setAuthorities(authorities2);
//        userService.save(user1);

    }


}

package co.kr.nakdong.service;

import co.kr.nakdong.entity.author.Authority;
import co.kr.nakdong.entity.author.User;
import co.kr.nakdong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    public Optional<User> findUser(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void addAuthority(Long userId, String authority){
        userRepository.findById(userId).ifPresent(user->{
            Authority newRole = new Authority(user.getUserId(), authority);
            if(user.getAuthorities() == null){
                HashSet<Authority> authorities = new HashSet<>();
                authorities.add(newRole);
                user.setAuthorities(authorities);
                save(user);
            }else if(!user.getAuthorities().contains(newRole)){
                HashSet<Authority> authorities = new HashSet<>();
                authorities.addAll(user.getAuthorities());
                authorities.add(newRole);
                user.setAuthorities(authorities);
                save(user);
            }
        });
    }

    public void removeAuthority(Long userId, String authority){
        userRepository.findById(userId).ifPresent(user->{
            if(user.getAuthorities()==null) return;
            Authority targetRole = new Authority(user.getUserId(), authority);
            if(user.getAuthorities().contains(targetRole)){
                user.setAuthorities(
                        user.getAuthorities().stream().filter(auth->!auth.equals(targetRole))
                                .collect(Collectors.toSet())
                );
                save(user);
            }
        });
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

//    public void updateRefreshToken(Long id, String refreshToken) {
//        userRepository.findById(id).ifPresent(user -> {
//            user.setRefreshToken(refreshToken);
//            user.setUpdatedDate(LocalDateTime.now());
//            user.setUpdatedBy(user.getEmail());
//        });
//    }

}

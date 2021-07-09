package co.kr.nakdong.entity;

import co.kr.nakdong.entity.board.Board;
import co.kr.nakdong.entity.common.BaseEntity;
import lombok.*;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="spp_user")
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(foreignKey = @ForeignKey(name = "userId"))
    private Set<Authority> authorities;

//    @ManyToOne(fetch = FetchType.EAGER)
//    private School school;

    private boolean enabled;

//    @Column(updatable = false)
//    private LocalDateTime created;
//
//    private LocalDateTime updated;

    private String refreshToken;

    @OneToMany(mappedBy = "user")
    private List<Board> boards = new ArrayList<>();

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return enabled;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return enabled;
    }

}

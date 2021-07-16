package co.kr.nakdong.entity.author;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "spp_authority")
@IdClass(Authority.class)
public class Authority implements GrantedAuthority {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";
//    public static final String ROLE_STUDENT = "ROLE_STUDENT";

    public static final Authority ADMIN_AUTHORITY = Authority.builder().authority(ROLE_ADMIN).build();
    public static final Authority USER_AUTHORITY = Authority.builder().authority(ROLE_USER).build();
//    public static final Authority STUDENT_AUTHORITY = Authority.builder().authority(ROLE_STUDENT).build();

    @Id
    private Long userId;

    @Id
    private String authority;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Authority)) return false;
        Authority authority1 = (Authority) o;
        return Objects.equals(authority, authority1.authority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authority);
    }

}

package co.kr.nakdong.repository;

import co.kr.nakdong.entity.author.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}

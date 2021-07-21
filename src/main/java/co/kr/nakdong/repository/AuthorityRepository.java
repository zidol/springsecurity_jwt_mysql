package co.kr.nakdong.repository;

import co.kr.nakdong.entity.author.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
}

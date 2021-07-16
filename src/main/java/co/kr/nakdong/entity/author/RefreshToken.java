package co.kr.nakdong.entity.author;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "refreshtoken")
public class RefreshToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @OneToOne
  @JoinColumn(name = "userId", referencedColumnName = "userId")
  private User user;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(nullable = false)
  private Instant expiryDate;

}

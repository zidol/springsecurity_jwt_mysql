package co.kr.nakdong.entity.author;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "roles")
public class Role {
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private Integer id;

	@Id
	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private ERole authority;

}
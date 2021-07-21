package co.kr.nakdong.entity.board;

import co.kr.nakdong.entity.author.User;
import co.kr.nakdong.entity.common.BaseEntity;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "spp_board")
@Builder
@ToString(exclude = "user")
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    private String cotnents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public void setMember(User user) {
        this.user = user;
        user.getBoards().add(this);
    }
}

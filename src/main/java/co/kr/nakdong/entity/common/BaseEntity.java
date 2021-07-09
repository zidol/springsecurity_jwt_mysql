package co.kr.nakdong.entity.common;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

//    @Column(name = "INSERT_MEMBER")
    private String createdBy;
    private LocalDateTime createdDate;
//    @Column(name = "UPDATE_MEMBER")
    private String updatedBy;
    private LocalDateTime updatedDate;
}

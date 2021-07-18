package co.kr.nakdong.dto;

import co.kr.nakdong.entity.author.Authority;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    @NotNull
    @Size(min = 3, max = 50)
    private String name;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull
    @Size(min = 3, max = 100)
    private String password;

    @NotNull
    @Size(min = 3, max = 50)
    private String email;

    private Set<Authority> authorities;

}
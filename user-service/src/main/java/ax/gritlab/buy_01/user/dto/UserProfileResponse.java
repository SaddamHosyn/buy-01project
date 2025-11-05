package ax.gritlab.buy_01.user.dto;

import ax.gritlab.buy_01.user.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private String id;
    private String name;
    private String email;
    private Role role;
    private String avatar;
}

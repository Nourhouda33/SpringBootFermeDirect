// src/main/java/com/FermeDirecte/FermeDirecte/dto/user/UserProfileResponse.java
package com.FermeDirecte.FermeDirecte.dto.user;

import com.FermeDirecte.FermeDirecte.dto.address.AddressResponse;
import com.FermeDirecte.FermeDirecte.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String email;
    private String prenom;
    private String nom;
    private String telephone;
    private Role role;
    private LocalDateTime dateCreation;
    private List<AddressResponse> adresses;
}

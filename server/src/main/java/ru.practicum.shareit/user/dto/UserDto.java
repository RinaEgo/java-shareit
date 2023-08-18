package ru.practicum.shareit.user.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class UserDto {

    private Long id;

    private String name;

    private String email;

    public UserDto(String name, String email) {
        this.name = name;
        this.email = email;
    }
}

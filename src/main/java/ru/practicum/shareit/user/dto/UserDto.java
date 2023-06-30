package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Setter
@Getter
@AllArgsConstructor
public class UserDto {

    private Integer id;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;
}

package ru.practicum.shareit.user.model;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Setter
@Getter
@AllArgsConstructor
public class User {

    private Integer id;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;
}

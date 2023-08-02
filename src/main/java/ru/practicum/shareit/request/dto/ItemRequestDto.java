package ru.practicum.shareit.request.dto;

import lombok.*;
import ru.practicum.shareit.item.dto.ItemShortDto;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ItemRequestDto {
    private Long id;
    @NotBlank
    private String description;
    private Long requestorId;
    private LocalDateTime created;
    private List<ItemShortDto> items;

    public ItemRequestDto(Long id, String description, LocalDateTime created, List<ItemShortDto> items) {
        this.id = id;
        this.description = description;
        this.created = created;
        this.items = items;
    }

    public ItemRequestDto(Long id, String description, Long requestorId, List<ItemShortDto> items) {
        this.id = id;
        this.description = description;
        this.requestorId = requestorId;
        this.items = items;
    }

    public ItemRequestDto(String description, long requestorId, LocalDateTime created, List<ItemShortDto> items) {
        this.description = description;
        this.requestorId = requestorId;
        this.created = created;
        this.items = items;
    }
}

package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
class ItemShortDtoTest {
    @Autowired
    private JacksonTester<ItemShortDto> json;

    @Test
    void testConvert() throws IOException {
        ItemShortDto itemShortDto = new ItemShortDto(1L,
                "item",
                "description",
                true,
                1L);
        var result = json.write(itemShortDto);
        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.name");
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).hasJsonPath("$.available");
        assertThat(result).hasJsonPath("$.requestId");
        assertThat(result).extractingJsonPathValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("item");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathValue("$.requestId").isEqualTo(1);
    }

    @Test
    void testShortItem() {
        ItemMapper mapper = new ItemMapper();
        Item item = new Item(1L, "name", "description", true);
        User user = new User(1L, "user", "mail@ya.ru");
        ItemRequest request = new ItemRequest(1L, "need item");
        item.setOwner(user);
        item.setRequest(request);

        ItemShortDto shortDto = mapper.toItemShortDto(item);
        assertEquals(item.getId(), shortDto.getId());
        assertEquals(item.getName(), shortDto.getName());
        assertEquals(item.getDescription(), shortDto.getDescription());
        assertEquals(item.getAvailable(), shortDto.getAvailable());
        assertEquals(item.getOwner().getId(), shortDto.getOwnerId());
        assertEquals(item.getRequest().getId(), shortDto.getRequestId());
    }
}

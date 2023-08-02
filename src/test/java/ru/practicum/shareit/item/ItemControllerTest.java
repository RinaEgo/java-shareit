package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    @MockBean
    private ItemService itemService;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    MockMvc mockMvc;
    private ItemDto itemDto;
    private ItemDto itemDtoWithDate;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        itemDto = new ItemDto(1L, "itemDto1", "one", true, 1L);
        itemDtoWithDate = new ItemDto(1L, "dto", "dto with date", true,
                1L, null, null, null);
        commentDto = new CommentDto(1L, "comment", "Gregory", LocalDateTime.now());
    }

    @Test
    void testCreateItem() throws Exception {
        when(itemService.createItem(any(), anyLong())).thenReturn(itemDto);

        mockMvc.perform(
                        post("/items")
                                .content(mapper.writeValueAsString(itemDto))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()));
    }

    @Test
    void testUpdateItem() throws Exception {
        when(itemService.updateItem(any(), anyLong(), anyLong())).thenReturn(itemDto);

        mockMvc.perform(
                        patch("/items/{itemId}", itemDto.getId())
                                .content(mapper.writeValueAsString(itemDto))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()));
    }

    @Test
    void testGetItemById() throws Exception {
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(itemDtoWithDate);
        mockMvc.perform(
                        get("/items/{itemId}", 1L)
                                .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(itemDtoWithDate.getName()))
                .andExpect(jsonPath("$.description").value(itemDtoWithDate.getDescription()));
    }

    @Test
    void testFindAllItems() throws Exception {
        List<ItemDto> items = List.of(itemDtoWithDate);
        when(itemService.findAllItems(anyLong(), anyInt(), anyInt())).thenReturn(List.of(itemDtoWithDate));
        mockMvc.perform(
                        get("/items")
                                .header("X-Sharer-User-Id", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(items.size()));
    }

    @Test
    void testCreateComment() throws Exception {

        when(itemService.createComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .content(mapper.writeValueAsString(commentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.text").value("comment"))
                .andExpect(jsonPath("$.authorName").value("Gregory"));
        verify(itemService, times(1)).createComment(anyLong(), anyLong(), any(CommentDto.class));
    }

    @Test
    void testDeleteItem() throws Exception {
        doNothing().when(itemService).deleteItem(anyLong());

        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(itemService, times(1)).deleteItem(anyLong());
    }
}

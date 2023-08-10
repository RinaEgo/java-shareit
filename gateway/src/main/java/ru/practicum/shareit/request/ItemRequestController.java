package ru.practicum.shareit.request;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import static ru.practicum.shareit.Constant.HEADER_USER_ID;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    public ItemRequestController(ItemRequestClient itemRequestClient) {
        this.itemRequestClient = itemRequestClient;
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(HEADER_USER_ID) Long userId,
                                         @Valid @RequestBody ItemRequestDto itemRequestDto) {

        return itemRequestClient.createRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> findAllRequestsByUser(@RequestHeader(HEADER_USER_ID) Long userId) {
        return itemRequestClient.findAllRequestsByUser(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAllRequests(@RequestHeader(HEADER_USER_ID) Long userId,
                                                  @RequestParam(defaultValue = "0") @Min(0) int from,
                                                  @RequestParam(defaultValue = "20") @Min(1) int size) {

        return itemRequestClient.findAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@PathVariable Long requestId, @RequestHeader(HEADER_USER_ID) Long userId) {
        return itemRequestClient.getRequestById(requestId, userId);
    }
}

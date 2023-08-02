package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    private Item item;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("One", "one@gmail.com");
        item = new Item("item", "item 1", true, user, null);
    }

    @Test
    void testFindAllByOwnerId() {
        user = userRepository.save(user);
        item = itemRepository.save(item);

        List<Item> items = itemRepository.findAllByOwnerId(user.getId(), PageRequest.of(0, 2)).toList();
        assertThat(items).hasSize(1).contains(item);
    }

    @Test
    void testSearch() {
        user = userRepository.save(user);
        item = itemRepository.save(item);

        List<Item> items = itemRepository.search("item", PageRequest.of(0, 2)).toList();
        assertThat(items).hasSize(1).contains(item);
    }
}

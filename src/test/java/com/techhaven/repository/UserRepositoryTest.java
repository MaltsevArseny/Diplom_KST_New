package com.techhaven.repository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.techhaven.model.User;

class UserRepositoryTest {

    private final UserRepository userRepo = new UserRepository();

    // Вспомогательный метод: найти любого существующего пользователя
    private User getAnySeedUser() {
        List<User> users = userRepo.findAllIncludingAdmins();
        assertFalse(users.isEmpty(), "Должен быть хотя бы один пользователь");
        return users.get(0);
    }

    @Test
    void findByEmailReturnsUserForSeedData() {
        User seed = getAnySeedUser();
        User user = userRepo.findByEmail(seed.getEmail());
        assertNotNull(user, "findByEmail должен вернуть пользователя для email=" + seed.getEmail());
        assertEquals(seed.getEmail(), user.getEmail());
    }

    @Test
    void findByEmailReturnsNullForNonExistent() {
        User user = userRepo.findByEmail("nonexistent-xyz@test.local");
        assertNull(user);
    }

    @Test
    void findByIdReturnsUser() {
        User seed = getAnySeedUser();
        User user = userRepo.findById(seed.getId());
        assertNotNull(user);
        assertEquals(seed.getId(), user.getId());
    }

    @Test
    void findByIdReturnsNullForNonExistent() {
        User user = userRepo.findById(-999);
        assertNull(user);
    }

    @Test
    void emailExistsReturnsTrueForSeed() {
        User seed = getAnySeedUser();
        assertTrue(userRepo.emailExists(seed.getEmail()),
            "emailExists должен вернуть true для email=" + seed.getEmail());
    }

    @Test
    void emailExistsReturnsFalseForNonExistent() {
        assertFalse(userRepo.emailExists("fake-xyz@nonexist.xyz"));
    }

    @Test
    void findAllUsersReturnsNonEmpty() {
        List<User> users = userRepo.findAllUsers();
        assertNotNull(users);
        assertFalse(users.isEmpty());
    }

    @Test
    void findAllIncludingAdminsReturnsNonEmpty() {
        List<User> all = userRepo.findAllIncludingAdmins();
        assertNotNull(all);
        assertFalse(all.isEmpty());
        assertTrue(all.size() >= userRepo.findAllUsers().size());
    }

    @Test
    void emailExistsForOtherWorks() {
        User seed = getAnySeedUser();
        // email принадлежит seed — НЕ должен "существовать для другого" при exclude=seed.id
        assertFalse(userRepo.emailExistsForOther(seed.getEmail(), seed.getId()));
        // но должен "существовать для другого" при exclude другого id
        assertTrue(userRepo.emailExistsForOther(seed.getEmail(), -999));
    }

    @Test
    void findByEmailReturnsNullForNull() {
        User user = userRepo.findByEmail(null);
        assertNull(user);
    }
}

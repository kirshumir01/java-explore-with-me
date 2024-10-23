package ru.practicum.ewm.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.service.UserService;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getAllUsers(
            @RequestParam(required = false) Set<Long> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Main-service: received ADMIN request to GET all users by ID's: {}", ids);
        List<UserDto> userDtoList = userService.adminGetAllUsersByIds(ids, from, size);
        log.info("Main-service: users received: {}", userDtoList);
        return userDtoList;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody NewUserRequest newUserRequestDto) {
        log.info("Main-service: received ADMIN request to CREATE user: {}", newUserRequestDto);
        UserDto createdUser = userService.adminCreateUser(newUserRequestDto);
        log.info("Main-service: user was created: {}", createdUser);
        return createdUser;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable long userId) {
        log.info("Main-service: received ADMIN request to DELETE user with id: {}", userId);
        userService.adminDeleteUser(userId);
        log.info("Main-service: user was deleted");
    }
}
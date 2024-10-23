package ru.practicum.ewm.user.service;

import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;

import java.util.List;
import java.util.Set;

public interface UserService {
    List<UserDto> adminGetAllUsersByIds(Set<Long> ids, int from, int size);

    UserDto adminCreateUser(NewUserRequest createDto);

    void adminDeleteUser(long userId);
}
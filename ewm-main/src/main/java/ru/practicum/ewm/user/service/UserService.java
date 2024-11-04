package ru.practicum.ewm.user.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;

import java.util.List;
import java.util.Set;

public interface UserService {
    List<UserDto> getAllUsersByIds(Set<Long> ids, Pageable page);

    UserDto createUser(NewUserRequest createDto);

    void deleteUser(long userId);
}
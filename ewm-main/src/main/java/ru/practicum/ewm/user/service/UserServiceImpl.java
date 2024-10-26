package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.QUser;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllUsersByIds(Set<Long> ids, Pageable page) {
        List<User> users;

        if (ids != null) {
             users = userRepository.findAll(QUser.user.id.in(ids), page).getContent();
        } else {
            users = userRepository.findAll(page).getContent();
        }

        if (users.isEmpty()) {
            return Collections.emptyList();
        }

        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        Set<String> emails = userRepository.findAll().stream().map(User::getEmail).collect(Collectors.toSet());

        if (emails.contains(newUserRequest.getEmail())) {
            throw new ConflictException(String.format("User with same email '%s' exists", newUserRequest.getEmail()));
        }

        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(newUserRequest)));
    }

    @Override
    @Transactional
    public void deleteUser(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id = %d not found", userId));
        }
        userRepository.deleteById(userId);
    }
}
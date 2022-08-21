/*
 * Copyright (c) 2022.
 * For educational usages only.
 */

package xyz.tcbx99.geek.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.tcbx99.geek.model.User;
import xyz.tcbx99.geek.repository.UserRepository;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Resource
    UserRepository userRepository;

    @Validated
    @GetMapping(path = "{id}", produces = "application/json")
    public User getUserById(@PathVariable("id") String id) {
        Optional<User> optional = userRepository.findById(UUID.fromString(id));
        if (!optional.isPresent()) {
            throw new NotFoundException();
        }
        return optional.get();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class NotFoundException extends RuntimeException {
    }

    @Validated
    @GetMapping
    public Page<User> getUsersByName(
            @RequestParam("keyword") String keyword,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findByNameStartsWithIgnoreCase(keyword, pageable);
    }

}

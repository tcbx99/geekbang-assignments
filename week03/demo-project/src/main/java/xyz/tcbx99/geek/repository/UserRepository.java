/*
 * Copyright (c) 2022.
 * For educational usages only.
 */

package xyz.tcbx99.geek.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import xyz.tcbx99.geek.model.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {


    Page<User> findByNameStartsWithIgnoreCase(@NonNull String name, Pageable pageable);

}
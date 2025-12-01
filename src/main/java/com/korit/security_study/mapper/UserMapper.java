package com.korit.security_study.mapper;

import com.korit.security_study.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface UserMapper {
    Optional<User> getUserByUsername(String username);
    Optional<User> getUserByUserId(Integer userId);
    void addUser(User user);
    int updatePassword(User user);
    Optional<User> getUserByEmail(String email);
}

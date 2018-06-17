package com.netty.barrage.repository;

import com.netty.barrage.domain.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {

}
package io.sign.www.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import io.sign.www.pojo.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends JpaRepository<User,Integer> {

}

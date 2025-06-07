package com.project.QLDSV_HTC.repository;

import com.project.QLDSV_HTC.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    /**
     * Tìm User theo username (để kiểm tra login).
     */
    User findByUsername(String username);
}

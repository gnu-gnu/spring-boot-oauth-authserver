package com.gnu.AuthServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

import com.gnu.AuthServer.entity.UserEntity;

/**
 * 
 * User정보를 읽기 위한 Repository
 * Repository란 CRUD 작업 및 기타 필요한 작업에 대한 추상화를 제공해주는 인터페이스이다
 * 
 * @see JpaRepository
 * @see SimpleJpaRepository
 * 
 * @author Geunwoo Shim(gflhsin@gmail.com)
 *
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>{
	UserEntity findByUsername(String userName);
}

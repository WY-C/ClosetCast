package com.admc.closet_cast.repository;

import com.admc.closet_cast.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {
    boolean existsByLoginId(String s);

    Optional<Member> findByLoginId(String loginId);
}

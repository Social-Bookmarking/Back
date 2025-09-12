package com.sonkim.bookmarking.domain.tag.repository;

import com.sonkim.bookmarking.domain.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    // 그룹 내 모든 태그 조회
    List<Tag> findAllByTeam_Id(Long teamId);

    // 그룹 내 모든 태그 이름 중복 조회
    Boolean existsByNameAndTeam_Id(String name, Long teamId);

    // 아무도 사용하지 않는 Tag 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Tag t WHERE t.bookmarkTags IS EMPTY")
    void deleteUnusedTags();
}

package com.sonkim.bookmarking.domain.tag.repository;

import org.junit.jupiter.api.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
}

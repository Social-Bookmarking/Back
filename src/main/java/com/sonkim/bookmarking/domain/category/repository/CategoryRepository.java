package com.sonkim.bookmarking.domain.category.repository;

import com.sonkim.bookmarking.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, CategoryRepositoryCustom {
    boolean existsByNameAndTeam_Id(String name, Long teamId);
}

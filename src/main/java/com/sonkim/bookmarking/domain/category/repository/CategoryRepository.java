package com.sonkim.bookmarking.domain.category.repository;

import com.sonkim.bookmarking.domain.category.dto.CategoryDto;
import com.sonkim.bookmarking.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, CategoryRepositoryCustom {
    boolean existsByNameAndTeam_Id(String name, Long teamId);
}

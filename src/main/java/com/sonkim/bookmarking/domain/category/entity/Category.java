package com.sonkim.bookmarking.domain.category.entity;

import com.sonkim.bookmarking.domain.category.dto.CategoryDto;
import com.sonkim.bookmarking.domain.team.entity.Team;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 그룹 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // 카테고리 이름
    @Column(nullable = false, length = 20)
    private String name;

    // 카테고리 순서
    @Column(nullable = false)
    private Integer position;

    public void update(CategoryDto.CategoryRequestDto request) {
        if (request.getName() != null) this.name = request.getName();}

    public void updatePosition(Integer position) {
        this.position = position;
    }
}

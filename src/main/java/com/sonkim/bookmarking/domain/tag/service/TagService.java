package com.sonkim.bookmarking.domain.tag.service;

import com.sonkim.bookmarking.domain.tag.dto.TagDto;
import com.sonkim.bookmarking.domain.tag.entity.Tag;
import com.sonkim.bookmarking.domain.tag.repository.TagRepository;
import com.sonkim.bookmarking.domain.team.entity.Team;
import com.sonkim.bookmarking.domain.team.service.TeamMemberService;
import com.sonkim.bookmarking.domain.team.service.TeamService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class TagService {

    private final TagRepository tagRepository;
    private final TeamService teamService;
    private final TeamMemberService teamMemberService;

    // 새로운 태그 생성
    @Transactional
    public void createTag(Long userId, Long teamId, TagDto.TagRequestDto request) {
        log.info("userId: {}, teamId: {}, tag: {} 생성 요청", userId, teamId, request.getName());

        // 그룹 상태 검증
        teamService.validateGroupIsActive(teamId);

        // EDITOR 권한 검사
        teamMemberService.validateEditor(userId, teamId);

        // 그룹 내 태그 이름 중복 확인
        if (tagRepository.existsByNameAndTeam_Id(request.getName(), teamId)) {
            throw new IllegalStateException("이미 존재하는 태그입니다.");
        }

        Team team = teamService.getTeamById(teamId);
        Tag newTag = Tag.builder()
                .name(request.getName())
                .team(team)
                .build();
        tagRepository.save(newTag);
    }

    // 특정 그룹의 모든 태그 목록 조회
    @Transactional(readOnly = true)
    public List<TagDto.TagResponseDto> getTagsByTeamId(Long teamId) {
        List<Tag> tags = tagRepository.findAllByTeam_Id(teamId);
        return tags.stream()
                .map(tag -> TagDto.TagResponseDto.builder()
                            .id(tag.getId())
                            .name(tag.getName())
                            .build())
                .collect(Collectors.toList());
    }

    // 태그 삭제
    @Transactional
    public void deleteTag(Long userId, Long teamId, Long tagId) {
        log.info("userId: {}, teamId: {}, tagId: {} 삭제 요청", userId, teamId, tagId);

        // 그룹 상태 검증
        teamService.validateGroupIsActive(teamId);

        // 태그 존재 여부 확인
        tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("태그를 찾을 수 없습니다. tagId: " + tagId));

        // EDITOR 권한 검사
        teamMemberService.validateEditor(userId, teamId);

        // 태그 삭제
        tagRepository.deleteById(tagId);
    }

    // 사용되지 않는 태그 일괄 삭제
    @Transactional
    public void deleteUnusedTags() {
        tagRepository.deleteUnusedTags();
    }

    // 태그 이름 리스트를 받아 존재하지 않는 태그는 새로 생성
    public List<Tag> findOrCreateTags(Long userId, Long teamId, List<String> tagNames) {
        // EDITOR 권한 검사
        teamMemberService.validateEditor(userId, teamId);
        Team team = teamService.getTeamById(teamId);

        // 이미 존재하는 태그 검사
        List<Tag> existingTags = tagRepository.findByTeam_IdAndNameIn(teamId, tagNames);
        List<String> existingTagNames = existingTags.stream().map(Tag::getName).toList();

        // DB에 존재하지 않는 태그들 필터링
        List<String> newTagNames = tagNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .toList();

        // 새로운 태그들을 생성하고 저장
        List<Tag> newTags = new ArrayList<>();
        if (!newTagNames.isEmpty()) {
            for (String newTagName : newTagNames) {
                if (newTagName.length() > 20)
                    throw new IllegalArgumentException("태그는 20자를 초과할 수 없습니다.: " + newTagName);

                newTags.add(Tag.builder().team(team).name(newTagName).build());
            }

            tagRepository.saveAll(newTags);
        }

        // 이미 존재하는 태그 + 새로 만든 태그 종합
        List<Tag> resultTags = new ArrayList<>(existingTags);
        resultTags.addAll(newTags);

        return resultTags;
    }
}

package com.sonkim.bookmarking.domain.tag.controller;

import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.domain.tag.dto.TagDto;
import com.sonkim.bookmarking.domain.tag.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "태그 관리", description = "그룹 내 태그 생성, 조회, 삭제 관련 API")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class TagController {

    private final TagService tagService;

    @Operation(summary = "그룹 내 모든 태그 목록 조회", description = "특정 그룹에 속한 모든 태그의 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "태그 목록 조회 성공")
    @GetMapping("/groups/{groupId}/tags")
    public ResponseEntity<?> getTagsByGroupId(@PathVariable("groupId") Long groupId) {
        List<TagDto.ResponseDto> tags = tagService.getTagsByTeamId(groupId);
        return ResponseEntity.ok(tags);
    }

    @Operation(summary = "그룹 내 태그 생성", description = "특정 그룹에 새로운 태그를 생성합니다. EDITOR 이상의 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "태그 생성 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 태그 이름")
    })
    @PostMapping("/groups/{groupId}/tags")
    public ResponseEntity<?> createTag(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                       @PathVariable("groupId") Long groupId,
                                       @RequestBody TagDto.RequestDto request) {
        tagService.createTag(userDetails.getId(), groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "태그 삭제", description = "특정 태그를 삭제합니다. ADMIN 권한이 필요합니다. (주의: 해당 태그를 사용 중인 모든 북마크에서 태그가 사라집니다.)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "태그 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN이 아님)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 태그")
    })
    @DeleteMapping("/groups/{groupId}/tags/{tagId}")
    public ResponseEntity<?> deleteTag(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long groupId,
            @PathVariable Long tagId) {
        tagService.deleteTag(userDetails.getId(), groupId, tagId);
        return ResponseEntity.noContent().build();
    }
}

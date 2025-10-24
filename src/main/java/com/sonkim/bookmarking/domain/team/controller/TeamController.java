package com.sonkim.bookmarking.domain.team.controller;

import com.sonkim.bookmarking.auth.entity.UserDetailsImpl;
import com.sonkim.bookmarking.common.aop.Idempotent;
import com.sonkim.bookmarking.domain.team.dto.TeamDto;
import com.sonkim.bookmarking.domain.team.entity.Team;
import com.sonkim.bookmarking.domain.team.service.QrCodeService;
import com.sonkim.bookmarking.domain.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "그룹 관리", description = "그룹 생성, 가입, 관리 관련 API")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/groups")
public class TeamController {

    private final TeamService teamService;
    private final QrCodeService qrCodeService;

    @Operation(summary = "새로운 그룹 생성", description = "사용자가 새로운 그룹을 생성합니다. 생성자는 자동으로 그룹의 ADMIN이 됩니다.")
    @ApiResponse(responseCode = "201", description = "그룹 생성 성공")
    @PostMapping
    @Idempotent
    public ResponseEntity<TeamDto.CreateResponseDto> createTeam(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @RequestBody TeamDto.TeamRequestDto createDto) {
        Team newTeam = teamService.createTeam(userDetails.getId(), createDto);

        TeamDto.CreateResponseDto response = TeamDto.CreateResponseDto.builder()
                .teamId(newTeam.getId()).build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "그룹 상세 정보 조회", description = "특정 그룹의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음")
    })
    @GetMapping("/{groupId}")
    public ResponseEntity<TeamDto.TeamResponseDto> getTeamDetails(@PathVariable Long groupId) {
        TeamDto.TeamResponseDto teamResponseDto = teamService.getTeamDetails(groupId);
        return ResponseEntity.ok(teamResponseDto);
    }

    @Operation(summary = "그룹 상세 정보 수정", description = "그룹의 이름이나 설명을 수정합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN이 아님)"),
            @ApiResponse(responseCode = "404", description = "그룹을 찾을 수 없음")
    })
    @PatchMapping("/{groupId}")
    public ResponseEntity<Void> updateTeam(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @RequestBody TeamDto.TeamRequestDto updateDto,
                                        @PathVariable("groupId") Long groupId) {
        teamService.updateTeam(userDetails.getId(), groupId, updateDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "그룹 초대 코드 재발급", description = "그룹의 초대 코드를 새로 발급받습니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "초대 코드 생성 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/{groupId}/invite-code")
    public ResponseEntity<TeamDto.CodeResponseDto> updateInviteCode(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @PathVariable("groupId") Long groupId) {
        String newInviteCode = teamService.generateInviteCode(userDetails.getId(), groupId);

        TeamDto.CodeResponseDto response = TeamDto.CodeResponseDto.builder()
                .code(newInviteCode).build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "초대 코드로 그룹 정보 미리보기", description = "그룹에 가입하기 전, 초대 코드를 이용해 어떤 그룹인지 미리 봅니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "유효하지 않은 초대 코드")
    })
    @GetMapping("/join")
    public ResponseEntity<TeamDto.TeamResponseDto> getTeamDetailsByCode(@RequestParam String code) {
        TeamDto.TeamResponseDto teamResponseDto = teamService.getTeamPreviewByCode(code);

        return ResponseEntity.ok(teamResponseDto);
    }

    @Operation(summary = "초대 코드로 그룹 가입", description = "초대 코드를 사용하여 그룹에 멤버로 가입합니다. 가입 시 기본 권한은 VIEWER입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "그룹 가입 성공"),
            @ApiResponse(responseCode = "409", description = "이미 가입된 그룹")
    })
    @PostMapping("/join")
    public ResponseEntity<Void> joinTeamMember(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @RequestBody TeamDto.JoinRequestDto joinRequestDto) {
        teamService.joinTeam(userDetails.getId(), joinRequestDto.getInviteCode());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "그룹 초대 QR 코드 이미지 생성", description = "그룹 초대를 위한 QR 코드를 이미지(PNG) 형식으로 생성하여 반환합니다. ADMIN 이상의 권한을 가진 유저만 가능")
    @ApiResponse(responseCode = "200", description = "QR 코드 생성 성공", content = @Content(mediaType = "image/png"))
    @ApiResponse(responseCode = "403", description = "권한 부족")
    @GetMapping("/{groupId}/invite-qr")
    public ResponseEntity<byte[]> generateInviteCode(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                @PathVariable("groupId") Long groupId) {
        try {
            // 그룹의 초대 코드 가져오기
            String inviteCode = teamService.getInviteCodeByTeamId(groupId);

            // QR 코드에 담을 URL 생성
            String joinUrl = "https://marksphere.link/api/groups/join?code=" + inviteCode;    // 차후 도메인으로 수정

            // QR 코드 이미지 생성
            byte[] qrCodeImage = qrCodeService.generateQrCodeImage(userDetails.getId(), groupId, joinUrl);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(qrCodeImage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "그룹 삭제 요청", description = "그룹의 상태를 '삭제 대기' 상태로 변경합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "그룹 삭제 요청 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN이 아님)"),
            @ApiResponse(responseCode = "404", description = "그룹을 찾지 못함")
    })
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> scheduleTeamDeletion(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long groupId
    ) {
        teamService.scheduleTeamDeletion(userDetails.getId(), groupId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "그룹 삭제 취소", description = "'삭제 대기' 상태인 그룹을 다시 '활성화' 상태로 변경합니다. ADMIN 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "그룹 삭제 취소 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN이 아님)"),
            @ApiResponse(responseCode = "404", description = "그룹을 찾지 못함")
    })
    @PostMapping("/{groupId}/cancel-deletion")
    public ResponseEntity<Void> cancelTeamDeletion(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long groupId
    ) {
        teamService.cancelTeamDeletion(userDetails.getId(), groupId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "그룹 초대 코드 조회", description = "그룹의 초대 코드를 조회합니다.")
    @GetMapping("/{groupId}/invite-code")
    public ResponseEntity<TeamDto.CodeResponseDto> getInviteCode(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long groupId
    ) {
        String inviteCode = teamService.getInviteCode(userDetails.getId(), groupId);

        TeamDto.CodeResponseDto response = TeamDto.CodeResponseDto.builder()
                .code(inviteCode).build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "그룹 소유권 이전", description = "그룹의 소유권을 다른 멤버에게 이전합니다. 현재 소유주만 실행할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "소유권 이전 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (소유주가 아님)"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 멤버가 아닌 사용자에게 이전 시도)")
    })
    @PostMapping("/{groupId}/transfer-ownership")
    public ResponseEntity<Void> transferOwnership(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Parameter(description = "소유권을 이전할 그룹 ID") @PathVariable Long groupId,
            @RequestBody TeamDto.OwnerTransferDto transferDto)
    {
        teamService.transferOwnership(userDetails.getId(), transferDto.getNewOwnerId(), groupId);
        return ResponseEntity.ok().build();
    }
}

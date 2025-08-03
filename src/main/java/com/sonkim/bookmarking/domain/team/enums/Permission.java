package com.sonkim.bookmarking.domain.team.enums;

public enum Permission {
    ADMIN,      // 관리자 : 멤버 관리, 북마크 관리
    EDITOR,     // 작성자 : 북마크 작성 가능, 자기가 등록한 북마크 삭제 가능
    VIEWER      // 뷰어 : 조회만 가능
}
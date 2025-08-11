package com.sonkim.bookmarking.common.exception;

public class MemberAlreadyExistsException extends RuntimeException {
    public MemberAlreadyExistsException() {
        super("이미 가입된 그룹입니다.");
    }
}

package com.sonkim.bookmarking.common.s3.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PresignedUrlDto {
    private String presignedUrl;
    private String fileKey;
}

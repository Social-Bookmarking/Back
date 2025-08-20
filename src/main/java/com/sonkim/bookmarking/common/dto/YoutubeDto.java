package com.sonkim.bookmarking.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 모르는 필드는 무시
public class YoutubeDto {
    private List<Item> items;

    // 동영상 정보 배열
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private Snippet snippet;
    }

    // 동영상 상세 정보
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Snippet {
        private Thumbnails thumbnails;
        private Localized localized;
    }

    // 썸네일 정보
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Thumbnails {
        private ThumbnailInfo maxres;
        private ThumbnailInfo standard;
        private ThumbnailInfo high;
    }

    // 썸네일 url
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ThumbnailInfo {
        private String url;
    }

    // Localized 필드 정보
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Localized {
        private String title;
        private String description;
    }
}

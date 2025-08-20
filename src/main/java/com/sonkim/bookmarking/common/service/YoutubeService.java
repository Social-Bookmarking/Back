package com.sonkim.bookmarking.common.service;

import com.sonkim.bookmarking.common.dto.YoutubeDto;
import com.sonkim.bookmarking.domain.bookmark.dto.BookmarkOGDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class YoutubeService {

    private final RestTemplate restTemplate;

    @Value("${google.youtube.api.key}")
    private String apiKey;

    private static final String YOUTUBE_API_URL =
            "https://www.googleapis.com/youtube/v3/videos?part=snippet&id={videoId}&key={apiKey}&regionCode=KR&hl=ko";

    public BookmarkOGDto getVideoDetails(String videoId) {
        // API URL에 파라미터를 담아 요청 전송, 응답을 DTO로 자동 변환
        YoutubeDto response = restTemplate.getForObject(YOUTUBE_API_URL, YoutubeDto.class, videoId, apiKey);

        if (response != null && response.getItems() != null && !response.getItems().isEmpty()) {
            YoutubeDto.Snippet snippet = response.getItems().get(0).getSnippet();

            // 제목, 설명 가져오기
            String title = snippet.getLocalized().getTitle();
            String description = snippet.getLocalized().getDescription();

            // 가장 높은 화질의 썸네일 선택
            String imageUrl = "";
            if (snippet.getThumbnails().getMaxres() != null) {
                imageUrl = snippet.getThumbnails().getMaxres().getUrl();
            } else if (snippet.getThumbnails().getStandard() != null) {
                imageUrl = snippet.getThumbnails().getStandard().getUrl();
            } else if (snippet.getThumbnails().getHigh() != null) {
                imageUrl = snippet.getThumbnails().getHigh().getUrl();
            }

            // BookmarkOGDto로 변환하여 반환
            return BookmarkOGDto.builder()
                    .title(title)
                    .description(description)
                    .image(imageUrl)
                    .build();
        }

        return null;
    }
}

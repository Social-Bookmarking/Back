package com.sonkim.bookmarking.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeUrlUtil {
    public static String extractVideoId(String youtubeUrl) {
        if (youtubeUrl == null || youtubeUrl.trim().isEmpty()) {
            return null;
        }

        String videoId = null;

        // 정규식을 사용하여 다양한 유튜브 URL 패턴에서 동영상 ID를 추출합니다.
        // 1. watch?v=... (기본)
        // 2. youtu.be/... (단축)
        // 3. shorts/... (쇼츠)
        // 4. embed/... (임베드)
        String pattern = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/shorts/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#&?\\n]*";

        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youtubeUrl);

        if (matcher.find()) {
            videoId = matcher.group();
        }

        return videoId;
    }
}

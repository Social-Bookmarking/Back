package com.sonkim.bookmarking.common.util;

public class YoutubeUrlUtil {
    public static String extractVideoId(String youtubeUrl) {
        String videoId = null;
        if (youtubeUrl != null && (youtubeUrl.contains("youtube.com") || youtubeUrl.contains("youtu.be"))) {
            String[] splitUrl = youtubeUrl.replace("https://", "")
                    .replace("http://", "")
                    .replace("www.", "")
                    .split("[?&/=]");
            for (String param : splitUrl) {
                if (param.equals("v") || param.equals("youtu.be")) {
                    int index = java.util.Arrays.asList(splitUrl).indexOf(param);
                    if (index + 1 < splitUrl.length) {
                        videoId = splitUrl[index + 1];
                        break;
                    }
                }
            }
            if (videoId == null && splitUrl.length > 1 && splitUrl[0].equals("youtu.be")) {
                videoId = splitUrl[1];
            }
        }
        return videoId;
    }
}

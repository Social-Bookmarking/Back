package com.sonkim.bookmarking.common.util;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;

public final class RemoteImageUrlValidator {
    private static final int MAX_URL_LENGTH = 2048;

    private RemoteImageUrlValidator() {
    }

    public static String validate(String value) {
        if (value == null || value.isBlank() || value.length() > MAX_URL_LENGTH) {
            throw new IllegalArgumentException("유효하지 않은 OpenGraph 이미지 URL입니다.");
        }

        URI uri;
        try {
            uri = URI.create(value.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 OpenGraph 이미지 URL입니다.", e);
        }

        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!("http".equals(scheme) || "https".equals(scheme))
                || uri.getHost() == null || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("OpenGraph 이미지는 HTTP 또는 HTTPS URL이어야 합니다.");
        }
        if (uri.getPort() != -1 && uri.getPort() != 80 && uri.getPort() != 443) {
            throw new IllegalArgumentException("OpenGraph 이미지 URL은 80 또는 443 포트만 사용할 수 있습니다.");
        }

        validatePublicHost(uri.getHost());
        return uri.toString();
    }

    private static void validatePublicHost(String host) {
        String normalizedHost = host.toLowerCase(Locale.ROOT);
        if ("localhost".equals(normalizedHost) || normalizedHost.endsWith(".local")) {
            throw new IllegalArgumentException("내부 네트워크의 이미지는 사용할 수 없습니다.");
        }
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isAnyLocalAddress()
                        || address.isLoopbackAddress()
                        || address.isLinkLocalAddress()
                        || address.isSiteLocalAddress()
                        || address.isMulticastAddress()
                        || isUniqueLocalIpv6(address)) {
                    throw new IllegalArgumentException("내부 네트워크의 이미지는 사용할 수 없습니다.");
                }
            }
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("OpenGraph 이미지 호스트를 확인할 수 없습니다.", e);
        }
    }

    private static boolean isUniqueLocalIpv6(InetAddress address) {
        byte[] bytes = address.getAddress();
        return bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc;
    }
}

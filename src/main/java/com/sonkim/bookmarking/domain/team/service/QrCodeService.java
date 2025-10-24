package com.sonkim.bookmarking.domain.team.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrCodeService {
    private final TeamMemberService teamMemberService;

    public byte[] generateQrCodeImage(Long userId, Long teamId, String url) throws IOException, WriterException {
        log.info("userId: {}, url: {} QR코드 생성 요청", userId, url);

        // ADMIN 권한 이상만 QR코드 생성 가능
        teamMemberService.validateAdmin(userId, teamId);

        // 전달된 url을 BitMatrix 형태로 인코딩
        BitMatrix bitMatrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 300, 300);

        // BitMatrix를 이미지로 변환
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        }
    }
}

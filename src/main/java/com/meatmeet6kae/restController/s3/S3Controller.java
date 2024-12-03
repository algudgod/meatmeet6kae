package com.meatmeet6kae.restController.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.meatmeet6kae.service.s3.S3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/s3") // 모든 요청 경로는 /s3로 시작합니다.
public class S3Controller {

    // S3Service를 의존성 주입받아 사용합니다.
    private final S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    /**
     * 클라이언트가 업로드한 여러 파일을 S3에 업로드하고, 파일 URL을 반환합니다.
     *
     * @param files 업로드할 파일들 (form-data로 전달된 파일 목록)
     * @return 업로드된 파일들의 URL 목록 또는 오류 메시지
     */
    @PostMapping("/upload") // POST 요청 처리 (URL: /s3/upload)
    public ResponseEntity<?> uploadFiles(@RequestParam("files") List<MultipartFile> files) {
        try {
            // 파일 개수 제한 (최대 10개)
            if (files.size() > 10) {
                return ResponseEntity.badRequest().body("You can upload up to 10 files only."); // 400 Bad Request
            }

            // S3Service를 호출하여 파일 업로드 처리
            List<String> fileUrls = s3Service.uploadFiles(files);

            // 업로드된 파일 URL 목록 반환 (JSON 형식)
            return ResponseEntity.ok(fileUrls);
        } catch (IOException e) {
            // 업로드 실패 시 500 Internal Server Error 반환
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed: " + e.getMessage());
        }
    }
}
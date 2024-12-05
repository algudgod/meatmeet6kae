package com.meatmeet6kae.restController.s3;
import com.meatmeet6kae.service.s3.S3Service;
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
public class S3RestController {

    private final S3Service s3Service;
    public S3RestController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/upload") // POST 요청 처리 (URL: /s3/upload)
    public ResponseEntity<?> uploadFiles(@RequestParam("files") List<MultipartFile> files) {
        try {
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
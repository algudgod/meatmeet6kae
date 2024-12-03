package com.meatmeet6kae.service.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class S3Service {

    private final AmazonS3 amazonS3; // S3 클라이언트 객체


    @Value("${cloud.aws.s3.bucket-name}") // application.properties에서 S3 버킷 이름가져오기
    private String bucket;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public List<String> uploadFiles(List<MultipartFile> files) throws IOException {
        List<String> fileUrls = new ArrayList<>(); // 업로드된 파일들의 URL을 저장할 리스트

        // 파일 목록을 순회하며 각 파일을 처리
        for (MultipartFile file : files) {
            // S3에 저장할 고유 파일 이름 생성 (현재 시간 + 원래 파일 이름)
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // S3에 저장할 파일의 메타데이터 설정 (파일 크기와 타입)
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType()); // 파일의 Content-Type 설정 (예: image/jpeg)
            metadata.setContentLength(file.getSize()); // 파일 크기 설정

            // S3 버킷에 파일 업로드
            amazonS3.putObject(bucket, fileName, file.getInputStream(), metadata);

            // 업로드된 파일의 URL 생성 및 리스트에 추가
            String fileUrl = "https://" + bucket + ".s3." + amazonS3.getRegionName() + ".amazonaws.com/" + fileName;
            fileUrls.add(fileUrl);
        }

        // 모든 파일의 URL 반환
        return fileUrls;
    }
}
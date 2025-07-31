package com.recipehub.backendrecipehub.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@ConditionalOnBean(S3Client.class)
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
        if (s3Client != null) {
            this.s3Presigner = S3Presigner.builder()
                    .region(s3Client.serviceClientConfiguration().region())
                    .credentialsProvider(s3Client.serviceClientConfiguration().credentialsProvider())
                    .build();
        } else {
            this.s3Presigner = null;
        }
    }

    public String uploadImage(MultipartFile file) throws IOException {
        if (s3Client == null) {
            throw new UnsupportedOperationException("S3 client not configured. Please set AWS credentials.");
        }
        
        String fileName = generateFileName(file.getOriginalFilename());
        String contentType = file.getContentType();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return fileName;
    }

    public String getImageUrl(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        
        if (s3Presigner == null) {
            throw new UnsupportedOperationException("S3 client not configured. Please set AWS credentials.");
        }

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(24))
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .build())
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    public void deleteImage(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }
        
        if (s3Client == null) {
            throw new UnsupportedOperationException("S3 client not configured. Please set AWS credentials.");
        }

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    private String generateFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return "recipe-images/" + UUID.randomUUID().toString() + extension;
    }
} 
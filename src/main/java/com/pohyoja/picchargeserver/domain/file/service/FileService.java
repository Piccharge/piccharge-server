package com.pohyoja.picchargeserver.domain.file.service;

import static com.pohyoja.picchargeserver.domain.file.exception.FileErrorCode.INVALID_FILE_EXTENSION;
import static com.pohyoja.picchargeserver.domain.file.exception.FileErrorCode.INVALID_FILE_NAME;

import com.pohyoja.picchargeserver.common.exception.CustomException;
import com.pohyoja.picchargeserver.domain.file.component.S3ImageComponent;
import com.pohyoja.picchargeserver.domain.file.dto.response.UrlResponse;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    public static final String IMAGE_FOLDER_NAME = "prod";
    private final S3Presigner presigner;
    private final S3ImageComponent s3ImageComponent;
    private final Set<String> VALID_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "gif", "bmp", "heif", // 이미지
            "mp4", "mov", "avi", "mkv", "webm", "flv"  // 비디오
    );

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public UrlResponse getPresignedPutUrl(String extension, String optionalFileName) {
        validateExtension(extension);

        String fileName = (optionalFileName == null || optionalFileName.isBlank())
                ? createPath(extension)
                : createPath(extension, optionalFileName);

        String presignUrl = createPresignedPutUrl(bucket, fileName);
        return new UrlResponse(presignUrl);
    }

    private String createPresignedPutUrl(String bucketName, String keyName) {
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)) // URL 은 10분간 유효하다.
                .putObjectRequest(req -> req
                        .bucket(bucketName)
                        .key(keyName)
                        .build())
                .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
        log.info("Presigned URL: [{}]", presignedRequest.url().toString());
        log.info("HTTP method: [{}]", presignedRequest.httpRequest().method());
        log.info("File path: [{}]", keyName);

        return presignedRequest.url().toExternalForm();
    }


    public void deleteImageFromS3(String imageName) {
        s3ImageComponent.deleteImageFromS3ByImageName(imageName);
    }

    private String createPath(String extension) {
        String fileId = createFileId();
        return String.format("%s/%s.%s", IMAGE_FOLDER_NAME, fileId, extension);
    }

    private String createPath(String extension, String fileName) {
        fileName = fileName.trim().toUpperCase();
        validateFileName(fileName);
        return String.format("%s/%s.%s", IMAGE_FOLDER_NAME, fileName, extension);
    }

    private String createFileId() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    private void validateExtension(String extension) {
        if (!VALID_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new CustomException(INVALID_FILE_EXTENSION);
        }
    }

    private void validateFileName(String fileName) {
        try {
            UUID.fromString(fileName);
        } catch (IllegalArgumentException e) {
            throw new CustomException(INVALID_FILE_NAME);
        }
    }
}

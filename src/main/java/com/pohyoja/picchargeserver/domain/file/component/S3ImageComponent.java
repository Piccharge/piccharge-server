package com.pohyoja.picchargeserver.domain.file.component;

import com.pohyoja.picchargeserver.common.exception.CustomException;
import com.pohyoja.picchargeserver.domain.file.exception.FileErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3ImageComponent {
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // MARK: 요청이 많지 않을것으로 예상되어 동기 방식으로 구현
    public void deleteImageFromS3ByImageName(String imageName) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(imageName)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("S3에서 이미지 삭제 성공: [{}]", imageName);
        } catch (Exception e) {
            throw new CustomException(FileErrorCode.IO_EXCEPTION_ON_IMAGE_DELETE);
        }
    }
}
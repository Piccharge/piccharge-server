package com.pohyoja.picchargeserver.domain.photo.service;

import com.pohyoja.picchargeserver.domain.photo.dto.request.FirebasePhotoCreateRequest;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebasePhotoService {

    private final RestTemplate restTemplate;

    @Value("${firebase.functions.create-photo-url}")
    private String createPhotoUrl;

    @Value("${firebase.functions.delete-photo-url}")
    private String deletePhotoUrl;

    @Async
    public void createPhotoDocument(String photoId, String uploadBy, String urlString, List<String> sharedWith) {
        Map<String, Integer> reactions = Map.of(
                "love", 0,
                "fire", 0,
                "star", 0,
                "like", 0
        );

        FirebasePhotoCreateRequest request = new FirebasePhotoCreateRequest(
                photoId,
                reactions,
                sharedWith,
                uploadBy,
                urlString
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FirebasePhotoCreateRequest> entity = new HttpEntity<>(request, headers);

        try {
            restTemplate.exchange(
                    createPhotoUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            log.info("Successfully created photo document in Firebase for photoId: {}", photoId);
        } catch (Exception e) {
            log.error("Failed to create photo document in Firebase for photoId: {}", photoId, e);
        }
    }

    @Async
    public void deletePhotoDocument(String photoId) {
        String url = deletePhotoUrl + "?photoId=" + photoId;

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    String.class
            );
            log.info("Successfully deleted photo document in Firebase for photoId: {}", photoId);
        } catch (Exception e) {
            log.error("Failed to delete photo document in Firebase for photoId: {}", photoId, e);
        }
    }
}
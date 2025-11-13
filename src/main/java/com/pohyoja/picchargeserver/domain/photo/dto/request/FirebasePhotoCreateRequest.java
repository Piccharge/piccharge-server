package com.pohyoja.picchargeserver.domain.photo.dto.request;

import java.util.List;
import java.util.Map;

public record FirebasePhotoCreateRequest(
        String photoId,
        Map<String, Integer> reactions,
        List<String> sharedWith,
        String uploadBy,
        String urlString
) {} 
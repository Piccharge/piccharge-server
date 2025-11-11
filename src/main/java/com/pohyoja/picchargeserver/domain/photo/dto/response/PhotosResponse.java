package com.pohyoja.picchargeserver.domain.photo.dto.response;

import java.util.List;

public record PhotosResponse(
        List<PhotoUrlResponse> photoUrls,
        boolean hasNext
) {}

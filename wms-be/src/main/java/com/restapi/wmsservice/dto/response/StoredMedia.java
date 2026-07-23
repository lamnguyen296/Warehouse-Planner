package com.restapi.wmsservice.dto.response;

public record StoredMedia(String publicId, String secureUrl, long fileSize) {
}

package org.example.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MarketplaceQrCodeApiService {
    public String createQrCodeUrl(String data) {
        String encoded = URLEncoder.encode(data == null ? "" : data, StandardCharsets.UTF_8);
        return "https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=" + encoded;
    }
}

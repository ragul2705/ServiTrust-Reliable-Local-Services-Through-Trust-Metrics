package com.servitrust.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeoCodingService {

    private final RestTemplate restTemplate;

    public GeoCodingService() {
        this.restTemplate = new RestTemplate();
    }

    public LatLng geocode(String query) {
        String q = query == null ? "" : query.trim();
        if (q.isBlank()) return null;

        try {
            String url = "https://nominatim.openstreetmap.org/search?format=json&limit=1&q=" +
                    URLEncoder.encode(q, StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "ServiTrust/1.0 (student-project)");
            headers.setAccept(MediaType.parseMediaTypes("application/json"));

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<NominatimResult[]> resp =
                    restTemplate.exchange(url, HttpMethod.GET, entity, NominatimResult[].class);

            NominatimResult[] arr = resp.getBody();
            if (arr == null || arr.length == 0) return null;

            double lat = Double.parseDouble(arr[0].lat);
            double lon = Double.parseDouble(arr[0].lon);
            return new LatLng(lat, lon);

        } catch (Exception e) {
            return null;
        }
    }

    public static class LatLng {
        public final Double lat;
        public final Double lng;
        public LatLng(Double lat, Double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }

    public static class NominatimResult {
        public String lat;
        public String lon;
    }
}
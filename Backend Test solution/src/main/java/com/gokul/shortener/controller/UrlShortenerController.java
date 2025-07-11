package com.gokul.shortener.controller;

import com.gokul.shortener.model.ShortUrl;
import com.gokul.shortener.service.UrlShortenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UrlShortenerController {

    @Autowired
    private UrlShortenerService service;

    @PostMapping("/shorturls")
    public ResponseEntity<?> createShort(@RequestBody Map<String, Object> req) {
        try {
            String url = (String) req.get("url");
            Integer validity = req.get("validity") != null ? (int) req.get("validity") : null;
            String shortcode = (String) req.get("shortcode");

            ShortUrl s = service.createShortUrl(url, validity, shortcode);

            Map<String, Object> response = new HashMap<>();
            response.put("shortLink", "http://localhost:8080/" + s.getShortcode());
            response.put("expiry", s.getExpiryAt().format(DateTimeFormatter.ISO_DATE_TIME));

            return ResponseEntity.status(201).body(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> redirect(@PathVariable String code) {
        try {
            ShortUrl s = service.getByCode(code).orElseThrow(() -> new Exception("Not found"));
            if (s.getExpiryAt().isBefore(java.time.LocalDateTime.now())) {
                return ResponseEntity.status(410).body(Map.of("error", "URL expired"));
            }
            service.incrementClick(code);
            return ResponseEntity.status(302).header("Location", s.getOriginalUrl()).build();
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/shorturls/{code}")
    public ResponseEntity<?> getStats(@PathVariable String code) {
        try {
            ShortUrl s = service.getByCode(code).orElseThrow(() -> new Exception("Not found"));

            Map<String, Object> stats = new HashMap<>();
            stats.put("clicks", s.getClickCount());
            stats.put("originalUrl", s.getOriginalUrl());
            stats.put("createdAt", s.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
            stats.put("expiry", s.getExpiryAt().format(DateTimeFormatter.ISO_DATE_TIME));

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}

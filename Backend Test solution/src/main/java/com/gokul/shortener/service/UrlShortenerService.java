package com.gokul.shortener.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.gokul.shortener.Repository.ShortUrlRepository;
import com.gokul.shortener.model.ShortUrl;

@Service
public class UrlShortenerService {

    @Autowired
    private ShortUrlRepository repo;

    public ShortUrl createShortUrl(String url, Integer validity, String shortcode) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        String code = (shortcode != null && !shortcode.isEmpty())
                ? shortcode
                : NanoIdUtils.randomNanoId();

        if (repo.existsById(code)) {
            throw new Exception("Shortcode already in use: " + code);
        }

        ShortUrl s = new ShortUrl();
        s.setShortcode(code);
        s.setOriginalUrl(url);
        s.setCreatedAt(now);
        s.setExpiryAt(now.plusMinutes(validity != null ? validity : 30));
        s.setClickCount(0);

        return repo.save(s);
    }

    public Optional<ShortUrl> getByCode(String code) {
        return repo.findById(code);
    }

    public ShortUrl incrementClick(String code) throws Exception {
        ShortUrl s = repo.findById(code).orElseThrow(() -> new Exception("Shortcode not found"));
        s.setClickCount(s.getClickCount() + 1);
        return repo.save(s);
    }
}

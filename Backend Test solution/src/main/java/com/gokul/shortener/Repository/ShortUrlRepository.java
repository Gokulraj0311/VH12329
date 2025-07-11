package com.gokul.shortener.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.gokul.shortener.model.ShortUrl;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, String> {
	
}

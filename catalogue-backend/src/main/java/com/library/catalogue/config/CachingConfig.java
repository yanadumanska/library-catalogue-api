package com.library.catalogue.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import java.util.concurrent.TimeUnit;

@Configuration
public class CachingConfig {

    @Bean
    public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
        return new ShallowEtagHeaderFilter();
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("books", "book", "authors", "author",
                "categoryTree", "categoryFlat", "category");
    }
    public static CacheControl authorsCache() {
        return CacheControl.maxAge(15, TimeUnit.MINUTES).cachePublic();
    }

    public static CacheControl categoriesCache() {
        return CacheControl.maxAge(30, TimeUnit.MINUTES).cachePublic();
    }
}

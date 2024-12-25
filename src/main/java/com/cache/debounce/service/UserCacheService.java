package com.cache.debounce.service;

import com.cache.debounce.dao.UserDao;
import com.cache.debounce.model.User;
import com.cache.debounce.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserCacheService {
    private static final String CACHE_KEY_PREFIX = "user:";
    private static final String LOCK_KEY_PREFIX = "user-lock:";
    private static final long LOCK_TIMEOUT = 10000; // 10 seconds
    private static final long CACHE_TIMEOUT = 3600; // 1 hour

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserDao userDao;

    @Autowired
    public UserCacheService(RedisTemplate<String, Object> redisTemplate, UserDao userDao) {
        this.redisTemplate = redisTemplate;
        this.userDao = userDao;
    }

    public User getUserById(Long id) throws InterruptedException {
        String cacheKey = CACHE_KEY_PREFIX + id;
        String lockKey = LOCK_KEY_PREFIX + id;

        // Try to get from cache first
        User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            log.debug("Cache hit for user {}", id);
            return cachedUser;
        }

        // Try to acquire lock
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "locked", LOCK_TIMEOUT, TimeUnit.MILLISECONDS);

        if (Boolean.TRUE.equals(locked)) {
            try {
                // Double check cache after acquiring lock
                cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
                if (cachedUser != null) {
                    return cachedUser;
                }

                // Get from database
                User user = userDao.findById(id)
                        .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

                // Update cache
                redisTemplate.opsForValue()
                        .set(cacheKey, user, CACHE_TIMEOUT, TimeUnit.SECONDS);

                return user;
            } finally {
                // Release lock
                redisTemplate.delete(lockKey);
            }
        } else {
            // Wait for other thread to populate cache
            int attempts = 0;
            while (attempts < 3) {
                Thread.sleep(100); // Wait 100ms
                cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
                if (cachedUser != null) {
                    return cachedUser;
                }
                attempts++;
            }

            // If still not in cache after waiting, fetch from DB
            return userDao.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        }
    }

    public User updateUser(User user) {
        User updatedUser = userDao.save(user);
        String cacheKey = CACHE_KEY_PREFIX + user.getId();
        redisTemplate.delete(cacheKey);
        return updatedUser;
    }
}
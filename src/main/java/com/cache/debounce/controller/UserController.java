package com.cache.debounce.controller;

import com.cache.debounce.model.User;
import com.cache.debounce.service.UserCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserCacheService userCacheService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) throws InterruptedException {
        User user = userCacheService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        User updatedUser = userCacheService.updateUser(user);
        return ResponseEntity.ok(updatedUser);
    }
}

package com.cache.debounce.dao;

import com.cache.debounce.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class UserDao {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        return user;
    };

    @Autowired
    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<User> findById(Long id) {
        try {
            User user = jdbcTemplate.queryForObject(
                    "SELECT id, name, email FROM users WHERE id = ?",
                    userRowMapper,
                    id
            );
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public User save(User user) {
        if (user.getId() != null) {
            return insert(user);
        } else {
            return update(user);
        }
    }

    private User insert(User user) {
        //KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users (id,name, email) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1,user.getId());
            ps.setString(2, user.getName());
            ps.setString(3, user.getEmail());
            return ps;
        });

        //user.setId(keyHolder.getKey().longValue());
        return user;
    }

    private User update(User user) {
        jdbcTemplate.update(
                "UPDATE users SET name = ?, email = ? WHERE id = ?",
                user.getName(),
                user.getEmail(),
                user.getId()
        );
        return user;
    }
}
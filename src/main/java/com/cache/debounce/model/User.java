package com.cache.debounce.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class User implements Serializable {
    private Long id;
    private String name;
    private String email;
}

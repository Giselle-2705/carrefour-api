package com.carrefour.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String nome;
    private String email;
    private String password;
    private String administrador;
    private String _id;
}

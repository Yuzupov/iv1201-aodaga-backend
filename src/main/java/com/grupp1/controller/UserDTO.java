package com.grupp1.controller;

public record UserDTO(
    String name,
    String surname,
    String email,
    String username,
    String password,
    String role
) {

}

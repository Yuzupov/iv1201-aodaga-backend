package com.grupp1.controller;

import java.util.List;

public record ApplicantDTO(
    String name,
    String surname,
    String status,
    List<Availability> availabilities
) {

}

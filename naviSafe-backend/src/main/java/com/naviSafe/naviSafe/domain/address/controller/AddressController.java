package com.naviSafe.naviSafe.domain.address.controller;

import com.naviSafe.naviSafe.domain.address.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/search-juso")
    public ResponseEntity<?> searchAddress(@RequestParam String keyword) {
        return ResponseEntity.ok(addressService.searchAddress(keyword));
    }
}

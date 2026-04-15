package com.naviSafe.naviSafe.domain.address.controller;

import com.naviSafe.naviSafe.domain.address.dto.SearchPlaceDto;
import com.naviSafe.naviSafe.domain.address.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/search-juso")
    public ResponseEntity<?> searchAddress(@RequestParam String keyword) {
        return ResponseEntity.ok(addressService.searchAddress(keyword));
    }

    @PostMapping("/search-place")
    public ResponseEntity<?> searchPlace(@RequestBody SearchPlaceDto searchPlaceDto) {
        return ResponseEntity.ok(addressService.searchPlace(searchPlaceDto));
    }
}

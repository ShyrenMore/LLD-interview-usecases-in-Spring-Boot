package com.personal.lld.controller;

import com.personal.lld.domain.Booking;
import com.personal.lld.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;

    @GetMapping("/users/{userId}/bookings")
    public List<Booking> listUserBookings(@PathVariable String userId) {
        return userService.listUserBookings(userId);
    }
}

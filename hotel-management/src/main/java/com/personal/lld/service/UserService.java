package com.personal.lld.service;

import com.personal.lld.domain.Booking;
import com.personal.lld.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final BookingRepository bookingRepository;

    public List<Booking> listUserBookings(String userId) {
        return bookingRepository.findByUser(userId);
    }
}

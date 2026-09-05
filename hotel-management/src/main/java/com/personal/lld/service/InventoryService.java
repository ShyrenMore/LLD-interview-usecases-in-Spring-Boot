package com.personal.lld.service;

import com.personal.lld.domain.DateRange;
import com.personal.lld.domain.Hotel;
import com.personal.lld.domain.RoomType;
import com.personal.lld.repository.BookingRepository;
import com.personal.lld.repository.HotelRepository;
import com.personal.lld.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    public static final long MILLIS_PER_DAY = 86_400_000L;

    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;

    public int getConfirmedBookingsCount(String hotelId, String roomTypeId, long dateUtc) {
        return bookingRepository.countConfirmedBookings(hotelId, roomTypeId, dateUtc);
    }

    public int getHeldBookingsCount(String hotelId, String roomTypeId, long dateUtc) {
        return bookingRepository.countHeldBookings(hotelId, roomTypeId, dateUtc, System.currentTimeMillis());
    }

    public int getCheckedInBookingsCount(String hotelId, String roomTypeId, long dateUtc) {
        return bookingRepository.countCheckedInBookings(hotelId, roomTypeId, dateUtc);
    }

    public boolean checkAvailability(String hotelId, String roomTypeId, DateRange range, int qty) {
        if (qty <= 0 || range.getCheckInDateUtc() >= range.getCheckOutDateUtc()) {
            return false;
        }

        Optional<RoomType> roomTypeOpt = roomTypeRepository.findById(roomTypeId);
        Optional<Hotel> hotelOpt = hotelRepository.findById(hotelId);

        if (roomTypeOpt.isEmpty() || hotelOpt.isEmpty()) {
            return false;
        }

        RoomType roomType = roomTypeOpt.get();
        Hotel hotel = hotelOpt.get();

        int overbookAllowed = (int) Math.ceil(
                roomType.getTotalRooms() * hotel.getDefaultOverbookPercent() / 100.0);

        for (long dateUtc = range.getCheckInDateUtc();
             dateUtc < range.getCheckOutDateUtc();
             dateUtc += MILLIS_PER_DAY) {

            int confirmed = getConfirmedBookingsCount(hotelId, roomTypeId, dateUtc);
            int held = getHeldBookingsCount(hotelId, roomTypeId, dateUtc);
            int checkedIn = getCheckedInBookingsCount(hotelId, roomTypeId, dateUtc);

            int available = roomType.getTotalRooms()
                    + overbookAllowed
                    - confirmed
                    - held
                    - checkedIn;

            if (available < qty) {
                return false;
            }
        }

        return true;
    }
}

package com.personal.lld;

import com.personal.lld.controller.*;
import com.personal.lld.domain.*;
import com.personal.lld.repository.RoomRepository;
import com.personal.lld.repository.UserRepository;
import com.personal.lld.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.UUID;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class HotelManagementApplication implements CommandLineRunner {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    private final SearchController searchController;
    private final BookingController bookingController;
    private final TransactionController transactionController;
    private final AdminController adminController;
    private final DashboardController dashboardController;

    public static void main(String[] args) {
        SpringApplication.run(HotelManagementApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("=== HOTEL MANAGEMENT SYSTEM SIMULATION ===");

        CancellationPolicy flexPolicy = new CancellationPolicy(
                UUID.randomUUID().toString(),
                "FLEX",
                100,
                24,
                System.currentTimeMillis());
        flexPolicy = adminController.createOrUpdatePolicy(flexPolicy);

        Hotel hotel = new Hotel(
                UUID.randomUUID().toString(),
                "Grand Hotel",
                "123 Main St",
                "New York",
                "USA",
                40.7128,
                -74.0060,
                4.5,
                true,
                10,
                flexPolicy.getId(),
                System.currentTimeMillis());
        hotel = adminController.createOrUpdateHotel(hotel);

        RoomType deluxeKing = new RoomType(
                UUID.randomUUID().toString(),
                hotel.getId(),
                "Deluxe King",
                2,
                "KING",
                10000,
                List.of("WiFi", "TV", "Mini Bar"),
                10,
                true,
                System.currentTimeMillis());
        deluxeKing = adminController.createOrUpdateRoomType(deluxeKing);

        User customer = new User(
                UUID.randomUUID().toString(),
                "John Doe",
                "john@example.com",
                UserRole.CUSTOMER,
                System.currentTimeMillis());
        customer = userRepository.save(customer);

        long tomorrow = alignToUtcDay(System.currentTimeMillis()) + InventoryService.MILLIS_PER_DAY;

        SeasonalPrice seasonalPrice = adminController.setSeasonalPrice(
                hotel.getId(),
                deluxeKing.getId(),
                tomorrow,
                15000);
        log.info("Seasonal price set: {}", seasonalPrice);

        DateRange range = new DateRange(
                tomorrow,
                tomorrow + 2 * InventoryService.MILLIS_PER_DAY);

        List<RoomTypeAvailability> availableRoomTypes =
                searchController.getAvailability(hotel.getId(), range);

        log.info("Available room types: {}", availableRoomTypes.size());
        availableRoomTypes.forEach(rta ->
                log.info("{} | nights={} | total={} minor units",
                        rta.getRoomTypeName(),
                        rta.getNightlyPrices().size(),
                        rta.getTotalPriceMinor()));

        if (availableRoomTypes.isEmpty()) {
            log.warn("No room type available; ending simulation.");
            return;
        }

        RoomTypeAvailability selected = availableRoomTypes.get(0);

        Booking booking = bookingController.createBooking(
                customer.getId(),
                hotel.getId(),
                selected.getRoomTypeId(),
                range,
                selected.getTotalPriceMinor());

        log.info("Created booking: {}", booking);

        Transaction transaction = transactionController.initiateTransaction(booking.getId());
        log.info("Transaction initiated: {}", transaction);

        transactionController.handleTransactionCallback(
                transaction.getProviderRef(),
                TransactionStatus.COMPLETED);

        Room room = new Room(
                UUID.randomUUID().toString(),
                hotel.getId(),
                booking.getRoomTypeId(),
                "101",
                true,
                System.currentTimeMillis());
        room = roomRepository.save(room);

        Booking checkedIn = adminController.checkIn(
                booking.getId(),
                room.getId(),
                System.currentTimeMillis());
        log.info("Guest checked in: {}", checkedIn);

        Booking checkedOut = adminController.checkOut(
                booking.getId(),
                System.currentTimeMillis() + InventoryService.MILLIS_PER_DAY);
        log.info("Guest checked out: {}", checkedOut);

        List<Booking> userBookings =
                dashboardController.listUserBookings(customer.getId());
        log.info("User {} has {} booking(s)", customer.getId(), userBookings.size());

        log.info("=== SIMULATION COMPLETED ===");
    }

    private long alignToUtcDay(long epochMillis) {
        return (epochMillis / InventoryService.MILLIS_PER_DAY)
                * InventoryService.MILLIS_PER_DAY;
    }
}

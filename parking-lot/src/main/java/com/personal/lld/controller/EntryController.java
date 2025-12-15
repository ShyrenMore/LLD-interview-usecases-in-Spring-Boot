package com.personal.lld.controller;

import com.personal.lld.domain.Ticket;
import com.personal.lld.domain.Vehicle;
import com.personal.lld.service.SlotService;
import com.personal.lld.service.TicketService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class EntryController {
    private final TicketService ticketService;
    private final SlotService slotService;

    public EntryResult enterVehicle(String licensePlate, Vehicle.VehicleType vehicleType) {
        System.out.println("[CONTROLLER] Vehicle entry request - License: " + licensePlate + ", Type: " + vehicleType);

        try {
            // Create vehicle
            Vehicle vehicle = new Vehicle(licensePlate, vehicleType);
            System.out.println("[CONTROLLER] Vehicle created: " + vehicle.getId());

            // Allocate slot
            Optional<UUID> slotId = slotService.allocateSlot(vehicleType)
                    .map(slot -> slot.getId());

            if (slotId.isEmpty()) {
                return new EntryResult(false, null, null, "No available slots for vehicle type: " + vehicleType);
            }

            // Generate ticket
            Ticket ticket = ticketService.generateTicket(vehicle, slotId.get());

            System.out.println("[CONTROLLER] Vehicle entry successful - Ticket: " + ticket.getId() + ", Slot: " + slotId.get());
            return new EntryResult(true, ticket.getId(), slotId.get(), "Entry successful");

        } catch (Exception e) {
            System.out.println("[CONTROLLER] Vehicle entry failed: " + e.getMessage());
            return new EntryResult(false, null, null, e.getMessage());
        }
    }

    @Data
    @AllArgsConstructor
    public static class EntryResult {
        private final boolean success;
        private final UUID ticketId;
        private final UUID slotId;
        private final String message;

    }
}

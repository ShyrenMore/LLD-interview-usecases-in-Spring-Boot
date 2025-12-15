package com.personal.lld.service;

import com.personal.lld.domain.Ticket;
import com.personal.lld.domain.Vehicle;
import com.personal.lld.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;

    public Ticket generateTicket(Vehicle vehicle, UUID slotId) {
        System.out.println("[SERVICE] Generating ticket for vehicle: " + vehicle.getLicensePlate());

        Ticket ticket = new Ticket(vehicle.getId(), slotId);
        ticketRepository.save(ticket);

        System.out.println("[SERVICE] Ticket generated successfully: " + ticket.getId());
        return ticket;
    }

    public Optional<Ticket> getTicket(UUID ticketId) {
        System.out.println("[SERVICE] Retrieving ticket: " + ticketId);
        return ticketRepository.findById(ticketId);
    }

    public void deactivateTicket(UUID ticketId) {
        System.out.println("[SERVICE] Deactivating ticket: " + ticketId);
        ticketRepository.deactivateTicket(ticketId);
    }
}

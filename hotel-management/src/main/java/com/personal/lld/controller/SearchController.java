package com.personal.lld.controller;

import com.personal.lld.domain.DateRange;
import com.personal.lld.domain.Hotel;
import com.personal.lld.domain.RoomTypeAvailability;
import com.personal.lld.domain.SearchFilter;
import com.personal.lld.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping("/hotels")
    public List<Hotel> searchHotels(@RequestBody SearchFilter filter) {
        return searchService.searchHotels(filter);
    }

    @PostMapping("/hotels/{hotelId}/availability")
    public List<RoomTypeAvailability> getAvailability(
            @PathVariable String hotelId,
            @RequestBody DateRange range) {

        return searchService.getAvailability(hotelId, range);
    }
}

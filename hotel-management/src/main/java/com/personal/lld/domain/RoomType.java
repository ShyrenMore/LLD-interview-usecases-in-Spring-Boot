package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomType {
    private String id;
    private String hotelId;
    private String name;
    private int capacity;
    private String bedType;
    private long basePriceMinor;
    private List<String> amenities;
    private int totalRooms;
    private boolean active;
    private long createdAt;
}

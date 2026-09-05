package com.personal.lld.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {
    private String id;
    private String name;
    private String address;
    private String city;
    private String country;
    private double latitude;
    private double longitude;
    private double rating;
    private boolean active;
    private int defaultOverbookPercent;
    private String cancellationPolicyId;
    private long createdAt;

    @Override
    public String toString() {
        return "Hotel{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", rating=" + rating +
                ", active=" + active +
                '}';
    }
}

package com.example.mogakko.domain.values.dto;

import com.example.mogakko.domain.values.domain.Location;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LocationDTO {

    private Long locationId;
    private String stationName;
    private String lineNumber;

    public LocationDTO(Location location) {
        this.locationId = location.getId();
        this.stationName = location.getStationName();
        this.lineNumber = location.getLineNumber();
    }

    public Location toEntity() {
        Location location = new Location();
        location.setId(locationId);
        location.setStationName(stationName);
        location.setLineNumber(lineNumber);
        return location;
    }

}

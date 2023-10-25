package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

@Data
public class SafetyStockLocation {

    private String type = "";
    private String name = "";

    private Location location;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}

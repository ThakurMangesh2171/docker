package com.vassarlabs.gp.pojo.TTOBMA;

import lombok.Data;

import java.util.List;

@Data
public class CapacityLists {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Entries> getEntries() {
        return entries;
    }

    public void setEntries(List<Entries> entries) {
        this.entries = entries;
    }

    private List<Entries> entries;

}

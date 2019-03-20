package com.example.piCarDriver.model;

public class GroupOrder extends Order {
    private String groupID;

    private Integer people;

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public Integer getPeople() {
        return people;
    }

    public void setPeople(Integer people) {
        this.people = people;
    }
}

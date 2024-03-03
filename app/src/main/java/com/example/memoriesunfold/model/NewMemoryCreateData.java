package com.example.memoriesunfold.model;

import java.util.ArrayList;

public class NewMemoryCreateData {

    int id;
    private String name;
    private String number;

    public NewMemoryCreateData(int id,String name, String number) {
        this.id=id;
        this.name = name;
        this.number = number;
    }

    public NewMemoryCreateData(){}

    public NewMemoryCreateData(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "NewMemoryCreateData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", number='" + number + '\'' +
                '}';
    }
}

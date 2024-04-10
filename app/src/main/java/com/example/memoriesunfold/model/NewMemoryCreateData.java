package com.example.memoriesunfold.model;

public class NewMemoryCreateData {

    int id;
    private String name;
    private String number;

    private int isSend;

    public NewMemoryCreateData(int id,String name, String number,int isSend) {
        this.id=id;
        this.name = name;
        this.number = number;
        this.isSend = isSend;
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

    public int isSend() {
        return isSend;
    }

    public void setSend(int send) {
        isSend = send;
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

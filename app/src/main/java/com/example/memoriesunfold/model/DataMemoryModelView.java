package com.example.memoriesunfold.model;

public class DataMemoryModelView {
    int id;
    int memory_id;
    String image;
    String Date;
    String Description;

    public DataMemoryModelView(int id, int memory_id, String image, String date, String description) {
        this.id = id;
        this.memory_id = memory_id;
        this.image = image;
        Date = date;
        Description = description;
    }

    @Override
    public String toString() {
        return "DataMemoryModelView{" +
                "id=" + id +
                ", memory_id=" + memory_id +
                ", image='" + image + '\'' +
                ", Date='" + Date + '\'' +
                ", Description='" + Description + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMemory_id() {
        return memory_id;
    }

    public void setMemory_id(int memory_id) {
        this.memory_id = memory_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }
}

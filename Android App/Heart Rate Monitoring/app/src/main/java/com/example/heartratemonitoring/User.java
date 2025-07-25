package com.example.heartratemonitoring;

public class User {
    String name, dateOfBirth, password;
    int weigh, height;

    public User() {
    }

    public User(String name, String dateOfBirth, String password, int weigh, int height) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.password = password;
        this.weigh = weigh;
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getWeigh() {
        return weigh;
    }

    public void setWeigh(int weigh) {
        this.weigh = weigh;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}

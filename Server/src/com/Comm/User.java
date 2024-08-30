package com.Comm;

import java.io.Serializable;
import java.sql.Date;

public class User implements Serializable {
    private static final long serialVersionUID=1l;

    private  Integer id;
    private String name;
    private String password;
    private Date  dates;
    private String sex;

    public User() {
    }

    public User(int id, String name, String password, Date dates, String sex) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.dates = dates;
        this.sex = sex;
    }
    public User(String name, String password, Date dates, String sex) {
        this.name = name;
        this.password = password;
        this.dates = dates;
        this.sex = sex;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getDates() {
        return dates;
    }

    public void setDates(Date dates) {
        this.dates = dates;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dates=" + dates +
                ", sex='" + sex + '\'' +
                '}';
    }
}

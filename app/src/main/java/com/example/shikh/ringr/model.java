package com.example.shikh.ringr;

/**
 * Created by shikh on 06-06-2018.
 */

public class model {
    String name;
    String number;

    model(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return this.name;
    }

    public String getNumber() {
        return this.number;
    }

}

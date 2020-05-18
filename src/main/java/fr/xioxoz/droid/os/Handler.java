/*
 * Copyright (C) 2020 - Damien Dejean <dam.dejean@gmail.com>
 */

package fr.xioxoz.droid.os;

public class Handler {

    private Handler() {}

    public static Handler aHandler() {
        return new Handler();
    }
}

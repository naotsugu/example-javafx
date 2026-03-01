package com.mammb.code.gclog;

import javafx.application.Application;
import java.util.Locale;

public class Main {

    public static void main(String[] args) {

        Locale.setDefault(Locale.US);
        System.setProperty("user.country", "US");
        System.setProperty("user.language", "en");

        Application.launch(App.class, args);

    }

}

package com.vodafone;

import com.vcs.flowpilot.action.database.api.DatabaseApi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;

public class CoreApplication {

    @Autowired
    private static DatabaseApi databaseApi;

    public static void main(String[] args) {
        SpringApplication.run(CoreApplication.class, args);
    }

}

package com.vsc.flowpilot.core;

import com.vcs.flowpilot.action.database.api.DatabaseActionApi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;

public class CoreApplication {

    @Autowired
    private static DatabaseActionApi databaseActionApi;

    public static void main(String[] args) {
        SpringApplication.run(CoreApplication.class, args);
    }

}

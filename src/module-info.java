module HotelManagerApp {
    requires transitive java.desktop;
    requires java.sql;

    exports main;
    exports UI;
    exports UI.panels;
    exports UI.dialogs;
    exports UI.components;
    exports dao;
    exports database;
    exports entity;
    exports service;
}

module com.store.inventory {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires java.naming;
    requires java.sql;
    requires com.h2database;
    
    requires org.slf4j;
    requires static lombok;
    
    opens com.store.inventory to javafx.fxml;
    opens com.store.inventory.controller to javafx.fxml;
    opens com.store.inventory.domain to org.hibernate.orm.core, javafx.base;
    
    exports com.store.inventory;
    exports com.store.inventory.controller;
    exports com.store.inventory.domain;
    exports com.store.inventory.repository;
    exports com.store.inventory.service;
    exports com.store.inventory.util;
}


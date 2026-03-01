module techhaven {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive java.sql;
    requires java.logging;
    requires java.prefs;
    requires java.desktop;

    opens com.techhaven to javafx.graphics;
    opens com.techhaven.model to javafx.base;
    opens com.techhaven.view to javafx.fxml;

    exports com.techhaven;
    exports com.techhaven.model;
    exports com.techhaven.view;
    exports com.techhaven.config;
    exports com.techhaven.security;
    exports com.techhaven.service;
    exports com.techhaven.repository;
}

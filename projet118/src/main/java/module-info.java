module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.desktop;
    requires jdk.jsobject;
    requires java.sql.rowset;
    requires java.net.http;
    requires org.json;
    requires org.apache.pdfbox;
    requires javax.mail.api;

    opens org.example to javafx.fxml;
    exports org.example;
}

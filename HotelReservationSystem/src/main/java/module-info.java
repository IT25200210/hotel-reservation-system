module com.example.hotelreservationsystem {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.hotelreservationsystem to javafx.fxml;
    exports com.example.hotelreservationsystem;
}
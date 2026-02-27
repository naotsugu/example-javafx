package org.example;

import atlantafx.base.theme.NordDark;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {

        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());

        final var xAxis = new NumberAxis(1, 31, 1);
        final var yAxis = new NumberAxis();
        final var areaChart =  new AreaChart<>(xAxis, yAxis);

        areaChart.setTitle("Temperature Monitoring (in Degrees C)");

        XYChart.Series<Number, Number> series1 = new XYChart.Series<>();
        series1.setName("April");
        series1.getData().add(new XYChart.Data<>(1, 4));
        series1.getData().add(new XYChart.Data<>(3, 10));
        series1.getData().add(new XYChart.Data<>(6, 15));
        series1.getData().add(new XYChart.Data<>(9, 8));
        series1.getData().add(new XYChart.Data<>(12, 5));
        series1.getData().add(new XYChart.Data<>(15, 18));
        series1.getData().add(new XYChart.Data<>(18, 15));
        series1.getData().add(new XYChart.Data<>(21, 13));
        series1.getData().add(new XYChart.Data<>(24, 19));
        series1.getData().add(new XYChart.Data<>(27, 21));
        series1.getData().add(new XYChart.Data<>(30, 21));

        XYChart.Series<Number, Number> series2 = new XYChart.Series<>();
        series2.setName("May");
        series2.getData().add(new XYChart.Data<>(1, 20));
        series2.getData().add(new XYChart.Data<>(3, 15));
        series2.getData().add(new XYChart.Data<>(6, 13));
        series2.getData().add(new XYChart.Data<>(9, 12));
        series2.getData().add(new XYChart.Data<>(12, 14));
        series2.getData().add(new XYChart.Data<>(15, 18));
        series2.getData().add(new XYChart.Data<>(18, 25));
        series2.getData().add(new XYChart.Data<>(21, 25));
        series2.getData().add(new XYChart.Data<>(24, 23));
        series2.getData().add(new XYChart.Data<>(27, 26));
        series2.getData().add(new XYChart.Data<>(31, 26));
        areaChart.getData().addAll(series1, series2);

        Scene scene  = new Scene(areaChart, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}

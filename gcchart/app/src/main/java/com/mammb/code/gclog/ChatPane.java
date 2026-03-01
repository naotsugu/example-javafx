package com.mammb.code.gclog;

import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class ChatPane extends StackPane {

    public ChatPane() {
        getChildren().add(new Label("Drop gc log file here"));
        setOnDragDetected(this::handleDragDetect);
        setOnDragOver(this::handleDragOver);
        setOnDragDropped(this::handleDragDropped);
    }

    private void handleDragDetect(MouseEvent e) {
    }

    private void handleDragOver(DragEvent e) {
        if (e.getDragboard().hasFiles()) {
            e.acceptTransferModes(TransferMode.COPY);
        }
    }

    private void handleDragDropped(DragEvent e) {
        Dragboard board = e.getDragboard();
        if (board.hasFiles()) {
            var areaChart = build(board.getFiles());
            getChildren().clear();
            getChildren().add(areaChart);
            e.consume();
        }
    }

    private AreaChart<Number, Number> build(List<File> files) {
        var gcLog = new GcLog();
        files.stream().map(File::toPath)
                .filter(Files::isReadable)
                .filter(Files::isRegularFile)
                .forEach(gcLog::analyze);

        var sizeSeries = new XYChart.Series<Number, Number>();
        sizeSeries.setName("Heap size");
        var usedSeries = new XYChart.Series<Number, Number>();
        usedSeries.setName("Used heap");
        gcLog.acceptSize((t, v) -> sizeSeries.getData().add(new XYChart.Data<>(t, v)));
        gcLog.acceptUsed((t, v) -> usedSeries.getData().add(new XYChart.Data<>(t, v)));

        var areaChart =  new AreaChart<>(new NumberAxis(), new NumberAxis());
        areaChart.setCreateSymbols(false);
        areaChart.getData().add(sizeSeries);
        areaChart.getData().add(usedSeries);

        return areaChart;
    }

}

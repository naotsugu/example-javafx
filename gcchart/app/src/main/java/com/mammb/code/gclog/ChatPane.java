package com.mammb.code.gclog;

import javafx.concurrent.Task;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ChatPane extends StackPane {

    /** The logger. */
    private static final System.Logger log = System.getLogger(ChatPane.class.getName());

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
            var task = buildAnalyzeTask(board.getFiles());

            ProgressBar progressBar = new ProgressBar();
            progressBar.progressProperty().bind(task.progressProperty());
            getChildren().clear();
            getChildren().add(progressBar);

            task.setOnSucceeded(event -> {
                getChildren().clear();
                getChildren().add(task.getValue());
            });
            task.setOnFailed(event -> {
                getChildren().clear();
                getChildren().add(new Label("Error processing file."));
                log.log(System.Logger.Level.WARNING, "Error processing file.", task.getException());
            });

            new Thread(task).start();
            e.consume();
        }
    }

    private Task<AreaChart<Number, Number>> buildAnalyzeTask(List<File> files) {
        return new Task<>() {
            @Override
            protected AreaChart<Number, Number> call() {
                List<Path> paths = files.stream().map(File::toPath)
                        .filter(Files::isReadable)
                        .filter(Files::isRegularFile)
                        .toList();
                var gcLog = new GcLog();
                if (paths.size() == 1) {
                    updateProgress(-1, 1);
                    gcLog.analyze(paths.getFirst());
                } else {
                    for (int i = 0; i < paths.size(); i++) {
                        gcLog.analyze(paths.get(i));
                        updateProgress(i + 1, paths.size());
                    }
                }
                return build(gcLog);
            }
        };
    }

    private AreaChart<Number, Number> build(GcLog gcLog) {

        var sizeSeries = new XYChart.Series<Number, Number>();
        sizeSeries.setName("Heap size");
        gcLog.acceptSize((t, v) -> sizeSeries.getData().add(new XYChart.Data<>(t, v)));

        var usedSeries = new XYChart.Series<Number, Number>();
        usedSeries.setName("Used heap");
        gcLog.acceptUsed((t, v) -> usedSeries.getData().add(new XYChart.Data<>(t, v)));

        var areaChart =  new AreaChart<>(new NumberAxis(), new NumberAxis());
        areaChart.setCreateSymbols(false);
        areaChart.getData().add(sizeSeries);
        areaChart.getData().add(usedSeries);

        return areaChart;
    }

}

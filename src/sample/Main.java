package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


// application for ping monitoring, author: Wojciech Brożonowicz

public class Main extends Application {
    private String hostAddress;
    private boolean running = false;
    final int WINDOW_SIZE = 10;
    private ScheduledExecutorService scheduledExecutorService;
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    XYChart.Series<String, Number> seriesC2avg = new XYChart.Series<>();
    XYChart.Series<String, Number> seriesC2min = new XYChart.Series<>();
    //XYChart.Series<String, Number> seriesC2max = new XYChart.Series<>();
    ArrayList<PingResult> res = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("JavaFX Realtime PING monitoring");

        //chart1
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time/s");
        xAxis.setAnimated(false);
        yAxis.setLabel("Respond time in ms");
        yAxis.setAnimated(false);
        final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Ping monitoring");
        lineChart.setAnimated(false);
        series.setName("Respond time");
        lineChart.getData().add(series);

        //chart2
        final CategoryAxis xAxisC2 = new CategoryAxis();
        final NumberAxis yAxisC2 = new NumberAxis();
        xAxisC2.setLabel("Hour");
        xAxisC2.setAnimated(false);
        yAxisC2.setLabel("Result in ms");
        yAxisC2.setAnimated(false);
        final BarChart<String, Number> lineChart2 = new BarChart<>(xAxisC2, yAxisC2);
        lineChart2.setTitle("Ping statistics [per hour]");
        lineChart2.setAnimated(false);
        seriesC2avg.setName("Avg respond time");
        seriesC2min.setName("Min respond time");
        //seriesC2max.setName("Max respond time");
        lineChart2.getData().add(seriesC2avg);
        lineChart2.getData().add(seriesC2min);
        //  lineChart2.getData().add(seriesC2max);
        for (int i = 0; i < 24; i++) {
            res.add(new PingResult(0, 0, 0));
        }
        VBox box = new VBox();
        HBox menuBox = new HBox();
        Button startBtn = new Button("Start");

        startBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                running = true;
                System.out.println("Start pinging");
            }
        });

        Button stopBtn = new Button("Stop");
        stopBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                running = false;
                System.out.println("Stop pinging");
            }
        });

        TextField tfStatus = new TextField("stopped");
        menuBox.getChildren().add(startBtn);
        menuBox.getChildren().add(stopBtn);
        menuBox.getChildren().add(tfStatus);
        box.getChildren().add(menuBox);
        box.getChildren().add(lineChart);
        box.getChildren().add(lineChart2);

        // setup scene
        Scene scene = new Scene(box, 800, 600);
        primaryStage.setScene(scene);

        // show the stage
        primaryStage.show();

        // setup a scheduled executor to periodically put data into the chart
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        // put dummy data onto graph per second
        scheduledExecutorService.scheduleAtFixedRate(() -> {

            // Update the chart
            Platform.runLater(() -> {
                if (running) {
                    ping();
                    tfStatus.setText("pinging " + hostAddress);
                } else {
                    tfStatus.setText("stopped");
                }
                if (series.getData().size() > WINDOW_SIZE)
                    series.getData().remove(0);
            });
        }, 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        scheduledExecutorService.shutdownNow();
    }


    public void ping() {
        try {
            hostAddress = "wsi.edu.pl";
            int port = 80;
            long timeToRespond = 0;
            LocalTime pingTime = LocalTime.now();
            InetAddress inetAddress = InetAddress.getByName(hostAddress);
            InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, port);
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(true);
            if (sc.connect(socketAddress)) {
                LocalTime stop = LocalTime.now();
                timeToRespond = Duration.between(pingTime, stop).toMillis();
            }
            addDataToChart(pingTime, timeToRespond);
            res.get(pingTime.getHour()).setMin(timeToRespond);
            res.get(pingTime.getHour()).setCount();
            res.get(pingTime.getHour()).setSum(timeToRespond);
            res.get(pingTime.getHour()).setAvg();

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void addDataToChart(LocalTime pingTime, long time) {
        series.getData().add(new XYChart.Data(pingTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")), time));
        seriesC2avg.getData().clear();
        seriesC2min.getData().clear();
        for (int i = 0; i < res.size(); i++) {
            seriesC2avg.getData().add(new XYChart.Data(i + ":", res.get(i).getAvg()));
            seriesC2min.getData().add(new XYChart.Data(i + ":", res.get(i).getMin()));
            //  seriesC2max.getData().add(new XYChart.Data(i+":",res.get(i).getMax()));
        }

    }

}
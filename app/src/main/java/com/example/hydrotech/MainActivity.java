package com.example.hydrotech;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

//import com.jjoe64.graphview.GraphView;
//import com.jjoe64.graphview.series.DataPoint;
//import com.jjoe64.graphview.series.LineGraphSeries;

import com.github.lzyzsd.circleprogress.ArcProgress;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.charts.Cartesian;
import com.anychart.data.Set;
import com.anychart.data.Mapping;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;

import viewmodel.SensorViewModel;

//import java.util.ArrayList;
//import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SensorViewModel sensorViewModel;
    private TextView relayHighPHStatus, phValue, humidityValue, temperatureValue, waterPumpStatus, waterSensorValue;
    private ArcProgress phProgressBar, humidityProgressBar, temperatureProgressBar;
    //private LineChart lineChart;
    //private LineDataSet lineDataSet;
   // private List<Entry> chartEntries;
    private Button toggleRelayButton;
    private AnyChartView anyChartView;
    private Set dataSet;
    private int xIndex = 0;

    //private LineGraphSeries<DataPoint> series;
   // private int xIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the UI elements
        relayHighPHStatus = findViewById(R.id.relay_status);
        phValue = findViewById(R.id.ph_Value);
        humidityValue = findViewById(R.id.humidity_value);
        temperatureValue = findViewById(R.id.temperature_Value);
        waterPumpStatus = findViewById(R.id.water_pump_status);
        waterSensorValue = findViewById(R.id.water_sensor_value);
        phProgressBar = findViewById(R.id.phProgressBar);
        humidityProgressBar = findViewById(R.id.humidityProgressBar);
        temperatureProgressBar = findViewById(R.id.temperatureProgressBar);

        // Configure progress bar maximums
        phProgressBar.setMax(14);
        humidityProgressBar.setMax(100);
        temperatureProgressBar.setMax(50);


        toggleRelayButton = findViewById(R.id.toggleWaterFlowRelay);
        toggleRelayButton.setOnClickListener(v -> {
            sensorViewModel.toggleWaterFlowRelay(); // Only toggle water flow relay
        });


// Find the toggle button
        Button toggleRelayButton = findViewById(R.id.toggleWaterFlowRelay);
        toggleRelayButton.setOnClickListener(v -> {
            boolean isRelayOn = toggleRelayButton.getText().toString().equals("Relay ON");
            // Toggle the relay and update the button text
            sensorViewModel.toggleWaterFlowRelay();
            toggleRelayButton.setText(isRelayOn ? "Water Flow Relay OFF" : "Water Flow Relay ON");
        });



        // Initialize ViewModel
        sensorViewModel = new ViewModelProvider(this).get(SensorViewModel.class);

        // Initialize AnyChart
        initializeAnyChart();

        // Start periodic data fetching
        sensorViewModel.startPeriodicFetching(); // Start periodic data fetching instead of single fetch

        // Observe LiveData from ViewModel
        observeViewModel();



    }

    private void initializeAnyChart() {
        anyChartView = findViewById(R.id.any_chart_view);
        //anyChartView.setProgressBar(findViewById(R.id.progress_bar)); // Optional loading progress bar

        Cartesian cartesian = AnyChart.line();
        cartesian.title("PH Value Over Time");

        cartesian.xAxis(0).title("Time");
        cartesian.yAxis(0).title("PH Value");

        cartesian.tooltip()
                .positionMode(TooltipPositionMode.POINT)
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        cartesian.interactivity().hoverMode(HoverMode.BY_X);
        cartesian.legend().enabled(true);

        dataSet = Set.instantiate();
        Mapping mapping = dataSet.mapAs("{ x: 'x', value: 'value' }");
        cartesian.line(mapping).name("PH Value").stroke("2 #FF5722");

        anyChartView.setChart(cartesian);
    }


    private void observeViewModel() {
        sensorViewModel.getPhValue().observe(this, ph -> {
            Log.d("MainActivity", "PH Value: " + ph);
            phValue.setText(getString(R.string.ph_value, ph));
            phProgressBar.setProgress(Math.round(ph));  // Convert float to int
            updateChart(ph);
        });

        sensorViewModel.getTemperature().observe(this, temperature -> {
            Log.d("MainActivity", "Temperature Value: " + temperature);
            temperatureValue.setText(getString(R.string.temperature_value, temperature));
            temperatureProgressBar.setProgress(Math.round(temperature));  // Convert float to int
        });

        sensorViewModel.getHumidity().observe(this, humidity -> {
            Log.d("MainActivity", "Humidity Value: " + humidity);
            humidityValue.setText(getString(R.string.humidity_value, humidity));
            humidityProgressBar.setProgress(Math.round(humidity));  // Convert float to int
        });

        sensorViewModel.getWaterSensorStatus().observe(this, isWet -> {
            Log.d("MainActivity", "Water Sensor Status: " + (isWet ? "Wet" : "Dry"));
            waterSensorValue.setText(isWet ? "Wet" : "Dry");
        });

        sensorViewModel.getWaterPumpStatus().observe(this, isOn -> {
            Log.d("MainActivity", "Water Pump Status: " + (isOn ? "On" : "Off"));
            waterPumpStatus.setText(isOn ? "On" : "Off");
        });

        sensorViewModel.getRelayStatus().observe(this, status -> {
            Log.d("MainActivity", "Relay Status: " + status);
            relayHighPHStatus.setText(status);
        });
    }

    private void updateChart(float ph) {
        if (ph < 0) {
            Log.e("MainActivity", "Invalid PH value: " + ph);
            return;
        }

        dataSet.append("{ x: " + xIndex++ + ", value: " + ph + " }");
    }


    @Override
    protected void onRestart() {
        super.onRestart();

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorViewModel.stopPeriodicFetching(); // Stop periodic data fetching when the activity is destroyed
        if (sensorViewModel != null) {
            sensorViewModel.stopFetchingData(); // Stop periodic data fetching
        }
    }
}

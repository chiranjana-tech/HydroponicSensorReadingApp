package viewmodel;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SensorViewModel extends ViewModel {

    private final MutableLiveData<Float> phValue = new MutableLiveData<>();
    private final MutableLiveData<Float> temperature = new MutableLiveData<>();
    private final MutableLiveData<Float> humidity = new MutableLiveData<>();
    private final MutableLiveData<Boolean> waterSensorStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> waterPumpStatus = new MutableLiveData<>();
    private final MutableLiveData<String> relayStatus = new MutableLiveData<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final OkHttpClient client = new OkHttpClient();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable fetchDataRunnable = new Runnable() {
        @Override
        public void run() {
            fetchData(); // Fetch new data
            handler.postDelayed(this, 500); // Repeat every 0.5 seconds (adjust as needed)
        }
    };

    private final MutableLiveData<Boolean> waterFlowRelayState = new MutableLiveData<>();
    public LiveData<Boolean> getWaterFlowRelayState() {
        return waterFlowRelayState;
    }


    // Expose LiveData for observation
    public LiveData<Float> getPhValue() {
        return phValue;
    }

    public LiveData<Float> getTemperature() {
        return temperature;
    }

    public LiveData<Float> getHumidity() {
        return humidity;
    }

    public LiveData<Boolean> getWaterSensorStatus() {
        return waterSensorStatus;
    }

    public LiveData<Boolean> getWaterPumpStatus() {
        return waterPumpStatus;
    }

    public LiveData<String> getRelayStatus() {
        return relayStatus;
    }

    public void startPeriodicFetching() {
        handler.post(fetchDataRunnable); // Start fetching data every 5 seconds
    }

    // Stop periodic data fetching
    public void stopPeriodicFetching() {
        handler.removeCallbacks(fetchDataRunnable); // Stop fetching data
    }


    // Method to fetch sensor data
    public void fetchData() {
        executor.execute(() -> {
            String url = "http://192.168.1.68/getSensorData"; // Replace with your ESP8266's IP address
            Request request = new Request.Builder().url(url).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        // Log the raw response body for debugging
                        String responseBody = response.body().string();
                        Log.d("SensorViewModel", "Response Body: " + responseBody); // Log the response body

                        try {
                            // Parse the response as JSON
                            JSONObject json = new JSONObject(responseBody);
                            updateSensorData(json); // Parse and update LiveData
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("SensorViewModel", "JSON parsing error: " + e.getMessage());
                        }
                    } else {
                        // Log if the response is not successful
                        Log.e("SensorViewModel", "Response was not successful: " + response.code());
                    }
                }

            });
        });
    }
    // Start periodic data fetching


    public void stopFetchingData() {
        executor.shutdownNow(); // This will attempt to stop all ongoing tasks
    }

    public void toggleWaterFlowRelay() {
        executor.execute(() -> {
            String url = "http://192.168.1.68/toggleWaterFlowRelay"; // Replace with your ESP8266's endpoint for the water flow relay
            Request request = new Request.Builder().url(url).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    Log.e("SensorViewModel", "Failed to toggle water flow relay: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d("SensorViewModel", "Water flow relay toggled successfully.");
                    } else {
                        Log.e("SensorViewModel", "Error toggling water flow relay: " + response.code());
                    }
                }
            });
        });
    }


    // Helper method to update LiveData
    private void updateSensorData(JSONObject json) {
        try {
            phValue.postValue((float) json.getDouble("ph"));
            temperature.postValue((float) json.getDouble("temperature"));
            humidity.postValue((float) json.getDouble("humidity"));
            waterSensorStatus.postValue(json.getInt("water") == 1); // 1 = Wet
            waterPumpStatus.postValue(json.getBoolean("pump"));
            waterFlowRelayState.postValue(json.getBoolean("waterFlowRelay")); // Add this line


            boolean relayLowPH = json.getBoolean("relayLowPH");
            boolean relayHighPH = json.getBoolean("relayHighPH");

            String relayStatusText;
            if (relayLowPH) {
                relayStatusText = "Low pH relay is ON";
            } else if (relayHighPH) {
                relayStatusText = "High pH relay is ON";
            } else {
                relayStatusText = "Both relays are OFF";
            }
            relayStatus.postValue(relayStatusText);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

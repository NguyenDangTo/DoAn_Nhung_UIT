package com.example.airqa.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.airqa.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.lang.Math;
import org.json.JSONException;
import org.json.JSONObject;
public class FeatureActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    final String host = "6d208b8432534bdebfcb61ff541abff7.s1.eu.hivemq.cloud";
    final String username = "Mobile";
    final String password = "Dangto2003";
    TextView assetName1, assetName2, humidValue, tempValue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature);
        // get variables
        assetName1 = findViewById(R.id.assetName1);
        assetName2 = findViewById(R.id.assetName2);
        humidValue = findViewById(R.id.humidValue);
        tempValue = findViewById(R.id.tempValue);
        // Handle navbar
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_features);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                startActivity(new Intent(FeatureActivity.this, MapActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            } else if (item.getItemId() == R.id.bottom_features) {
                return true;
            } else if (item.getItemId() == R.id.bottom_chart) {
                startActivity(new Intent(FeatureActivity.this, ChartActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            } else if (item.getItemId() == R.id.bottom_settings) {
                startActivity(new Intent(FeatureActivity.this, SettingsActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            } else {
                return false;
            }
        });
        FloatingActionButton fabButton = findViewById(R.id.fabBtn);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPredictDialog();
            }
        });

        // create an MQTT client
        final Mqtt5BlockingClient client = MqttClient.builder()
                .useMqttVersion5()
                .serverHost(host)
                .serverPort(8883)
                .sslWithDefaultConfig()
                .buildBlocking();
        client.connectWith()
                .simpleAuth()
                .username(username)
                .password(UTF_8.encode(password))
                .applySimpleAuth()
                .send();
        client.subscribeWith()
                .topicFilter("test")
                .send();
        // set a callback that is called when a message is received (using the async API style)
        client.toAsync().publishes(ALL, publish -> {
            String data = "" + UTF_8.decode(publish.getPayload().get());
            setInformation(data);
        });
        // set day night UI
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        ImageView periodIcon = findViewById(R.id.periodIcon);
        if (hourOfDay >= 5 && hourOfDay < 17) {
            periodIcon.setImageResource(R.drawable.sun);
        } else {
            periodIcon.setImageResource(R.drawable.moon);
        }
    }

    private void setInformation(String data){
        try {
            // Parse the JSON string
            JSONObject jsonObject = new JSONObject(data);

            // Extract temperature and humidity
            double temperature = jsonObject.getDouble("temperature");
            double humidity = jsonObject.getDouble("humidity");

            // Print the extracted values
            Log.d("test", "Temperature: " + temperature);
            Log.d("test", "Humidity: " + humidity);
            humidValue.setText(Math.round(humidity) + "%");
            tempValue.setText(Math.round(temperature) + "℃");
        } catch (JSONException e) {
            // Handle the parsing error (e.g., print error message)
            System.err.println("Error parsing JSON: " + e.getMessage());
        }


    }
    private void showPredictDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.predictsheetlayout);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);

        TextView tempVal = dialog.findViewById(R.id.assetIdTempValue1);
        TextView humidVal = dialog.findViewById(R.id.assetIdHumidValue1);
        TextView windSpeedVal = dialog.findViewById(R.id.assetIdWindSpeedValue1);
        SharedPreferences sharedPreferences = getSharedPreferences("preferences", MODE_PRIVATE);
        String T = sharedPreferences.getString("PredTemperature", "");
        String H = sharedPreferences.getString("PredHumidity", "");
        String W = sharedPreferences.getString("PredWindSpeed", "");

        tempVal.setText( String.valueOf(T));
        humidVal.setText(String.valueOf(H));
        windSpeedVal.setText(String.valueOf(W));
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        Log.d("click predict", "show dialog");
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = com.google.android.material.R.style.Animation_Material3_BottomSheetDialog;
        dialog.getWindow().setGravity(Gravity.CENTER);
    };

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.bottom_features);
    }
}
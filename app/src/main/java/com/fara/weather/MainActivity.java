package com.fara.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.URL;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=fc50456f9dc37a5ca791d63a5690caa0&lang=ru&units=metric";

    private TextView tvCity;
    private TextView tvTemp;
    private TextView tvDesc;
    private TextView tvPress;
    private TextView tvHumidity;
    private TextView tvWindSpeed;

    private ImageView ivWeather;
    private ImageView ivHumidity;
    private ImageView ivPressure;
    private ImageView ivWind;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCity = findViewById(R.id.tvCity);
        tvTemp = findViewById(R.id.tvTemp);
        tvDesc = findViewById(R.id.tvDesc);
        tvPress = findViewById(R.id.tvPress);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWindSpeed = findViewById(R.id.tvWindSpeed);
        ivWeather = findViewById(R.id.ivWeather);
        etSearch = findViewById(R.id.etSearch);
        ivHumidity = findViewById(R.id.ivHumidity);
        ivPressure = findViewById(R.id.ivPress);
        ivWind = findViewById(R.id.ivWindSpeed);

        setTime();
    }

    public void onClickShowWeather(View view) {
        String city = etSearch.getText().toString().trim();
        if (!city.isEmpty()) {
            DownloadWeatherTask task = new DownloadWeatherTask();
            String url = String.format(WEATHER_URL, city);
            task.execute(url);

            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getRootView().getWindowToken(), 0);

            ivHumidity.setVisibility(View.VISIBLE);
            ivPressure.setVisibility(View.VISIBLE);
            ivWind.setVisibility(View.VISIBLE);
        }
    }

    private class DownloadWeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            StringBuilder builder = new StringBuilder();
            URL url = null;
            HttpsURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpsURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));
                String line = reader.readLine();
                while (line != null) {
                    builder.append(line);
                    line = reader.readLine();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return builder.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                String city = jsonObject.getString("name") + ", " + jsonObject.getJSONObject("sys").getString("country");
                String jsonTemp = jsonObject.getJSONObject("main").getString("temp");
                String description = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                String icon = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");
                String temp = Math.round(Float.parseFloat(jsonTemp)) + "°C";
                String pressure = jsonObject.getJSONObject("main").getString("pressure") + " мм";
                String humidity = jsonObject.getJSONObject("main").getString("humidity") + " %";
                String windSpeed = jsonObject.getJSONObject("wind").getString("speed") + " м/с";

                tvCity.setText(city);
                tvTemp.setText(temp);
                tvDesc.setText(description);
                tvPress.setText(pressure);
                tvHumidity.setText(humidity);
                tvWindSpeed.setText(windSpeed);
                setImage(ivWeather, icon);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void setImage(final ImageView imageView, final String value) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (value) {
                        case "01d":
                        case "01n":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d01d));
                            break;
                        case "02d":
                        case "02n":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d02d));
                            break;
                        case "03d":
                        case "03n":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d03d));
                            break;
                        case "04d":
                        case "04n":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d04d));
                            break;
                        case "09d":
                        case "09n":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d09d));
                            break;
                        case "10d":
                        case "10n":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d10d));
                            break;
                        case "11d":
                        case "11n":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d11d));
                            break;
                        case "13d":
                        case "13n":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d13d));
                            break;
                    }
                }
            });
        }
    }

    private void setTime() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        View homeLayout = findViewById(R.id.view);

        if (timeOfDay >= 20 && timeOfDay < 5) {
            homeLayout.setBackgroundResource(R.drawable.cloud);

        } else if (timeOfDay >= 5 && timeOfDay < 20) {
            homeLayout.setBackgroundResource(R.drawable.sunny);
        }
    }
}
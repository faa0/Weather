package com.fara.weather;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.resources.TextAppearance;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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

    private TextView tvSave;

    private ImageView ivWeather;
    private ImageView ivHumidity;
    private ImageView ivPressure;
    private ImageView ivWind;
    private EditText etSearch;
    private View background;

    SharedPreferences sPref;
    final String SAVED_TEXT = "";

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
        background = findViewById(R.id.view);

        tvSave = findViewById(R.id.tvSave);

        setTime();
        loadText();

        tvSave.setVisibility(View.VISIBLE);

    }

    public void onClickShowWeather(View view) {
        String city = etSearch.getText().toString().trim();
        if (!city.isEmpty()) {
            DownloadWeatherTask task = new DownloadWeatherTask();
            String url = String.format(WEATHER_URL, city);
            task.execute(url);

            hideKeyboard(this);

            ivHumidity.setVisibility(View.VISIBLE);
            ivPressure.setVisibility(View.VISIBLE);
            ivWind.setVisibility(View.VISIBLE);

            saveText();
            tvSave.setText(sPref.getString(SAVED_TEXT, ""));
            tvSave.setVisibility(View.INVISIBLE);
        }
    }

    private void saveText() {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(SAVED_TEXT, etSearch.getText().toString());
        ed.commit();
    }

    private void loadText() {
        sPref = getPreferences(MODE_PRIVATE);
        String savedText = sPref.getString(SAVED_TEXT, "");
        tvSave.setText(savedText);
    }

    public void onClickHistory(View view) {
        etSearch.setText(sPref.getString(SAVED_TEXT, ""));
        tvSave.setTextColor(R.style.TextAppearance_AppCompat_Body1);
    }

    public void onClickVisibility(View view) {
        tvSave.setVisibility(View.VISIBLE);
        tvSave.setTextColor(getResources().getColor(R.color.black));
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d01d, null));
                            background.setBackgroundResource(R.drawable.cloud);
                            break;
                        case "01n":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d11d, null));
                            background.setBackgroundResource(R.drawable.cloud);
                            break;
                        case "02d":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d02d, null));
                            background.setBackgroundResource(R.drawable.cloud);
                            break;
                        case "02n":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d11d, null));
                            background.setBackgroundResource(R.drawable.d02n);
                            break;
                        case "03d":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d03d, null));
                            background.setBackgroundResource(R.drawable.cloud);
                            break;
                        case "03n":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d03d, null));
                            background.setBackgroundResource(R.drawable.d0304n);
                            break;
                        case "04d":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d04d, null));
                            background.setBackgroundResource(R.drawable.cloud);
                            break;
                        case "04n":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d04d, null));
                            background.setBackgroundResource(R.drawable.d0304n);
                            break;
                        case "09d":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d09d, null));
                            background.setBackgroundResource(R.drawable.cloud);
                            break;
                        case "09n":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d09d, null));
                            background.setBackgroundResource(R.drawable.d0910n);
                            break;
                        case "10d":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d10d, null));
                            background.setBackgroundResource(R.drawable.cloud);
                            break;
                        case "10n":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d11d, null));
                            background.setBackgroundResource(R.drawable.d0910n);
                            break;
                        case "11d":
                            background.setBackgroundResource(R.drawable.cloud);
                        case "11n":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d11d, null));
                            background.setBackgroundResource(R.drawable.cloud);
                            break;
                        case "13d":
                            background.setBackgroundResource(R.drawable.cloud);
                        case "13n":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d13d, null));
                            background.setBackgroundResource(R.drawable.cloud);
                            break;
                        case "50d":
                            background.setBackgroundResource(R.drawable.cloud);
                        case "50n":
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.d13d, null));
                            background.setBackgroundResource(R.drawable.cloud);
                            break;
                    }
                }
            });
        }
    }

    private void setTime() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay >= 20 && timeOfDay < 5) {
            background.setBackgroundResource(R.drawable.cloud);

        } else if (timeOfDay >= 5 && timeOfDay < 20) {
            background.setBackgroundResource(R.drawable.sunny);
        }
    }
}
package com.fara.weather;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    private ImageView ivHumidity;
    private ImageView ivPressure;
    private ImageView ivWind;

    private FloatingActionButton fabSearch;
    private EditText etSearch;
    private View background;

    SharedPreferences sPref;
    final String SAVED_TEXT = "";

    @SuppressLint("ClickableViewAccessibility")
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
        etSearch = findViewById(R.id.etSearch);
        ivHumidity = findViewById(R.id.ivHumidity);
        ivPressure = findViewById(R.id.ivPress);
        ivWind = findViewById(R.id.ivWindSpeed);
        background = findViewById(R.id.view);

        fabSearch = findViewById(R.id.fabSearch);

        setTime();

//        sPref = getPreferences(MODE_PRIVATE);
//        SharedPreferences.Editor ed = sPref.edit();
//        boolean firstRun = sPref.getBoolean("firstRun", true);
//        if (firstRun) {
//            ed.putBoolean("firstRun", false);
//            ed.commit();
//        } else {
//            loadText();
//        }
    }

    public void onClickShowWeather(View view) {
        String city = etSearch.getText().toString().trim();

        if (!city.isEmpty()) {
            DownloadWeatherTask task = new DownloadWeatherTask();
            String url = String.format(WEATHER_URL, city);
            task.execute(url);

            hideKeyboard(this);
        }
    }

    public void onClickAddFavorite(View view) {
        String city = etSearch.getText().toString().trim();
        if (!city.isEmpty()) {
            Intent intent = new Intent(this, FavoriteActivity.class);
            intent.putExtra("city", city);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        } else {
            Toast.makeText(this, "Пустое поле ввода ", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickFavorite(View view) {
        Intent intent = new Intent(this, FavoriteActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
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
                setImage(background, icon);
                ivHumidity.setVisibility(View.VISIBLE);
                ivPressure.setVisibility(View.VISIBLE);
                ivWind.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setTime() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay >= 20 && timeOfDay < 5) {
            background.setBackgroundResource(R.drawable.d01n);
            fabSearch.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        } else if (timeOfDay >= 5 && timeOfDay < 20) {
            background.setBackgroundResource(R.drawable.d01d);
            fabSearch.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
            getWindow().setStatusBarColor(getResources().getColor(R.color.blue));
        }
    }

    private void setImage(final View background, final String value) {
        switch (value) {
            case "01d":
                background.setBackgroundResource(R.drawable.d01d);
                break;
            case "01n":
                background.setBackgroundResource(R.drawable.d01n);
                break;
            case "02d":
                background.setBackgroundResource(R.drawable.d02d);
                break;
            case "02n":
                background.setBackgroundResource(R.drawable.d02n);
                break;
            case "03d":
                background.setBackgroundResource(R.drawable.d03d);
                break;
            case "03n":
                background.setBackgroundResource(R.drawable.d03n);
                break;
            case "04d":
                background.setBackgroundResource(R.drawable.d04d);
                break;
            case "04n":
                background.setBackgroundResource(R.drawable.d04n);
                break;
            case "09d":
                background.setBackgroundResource(R.drawable.d09d);
                break;
            case "09n":
                background.setBackgroundResource(R.drawable.d09n);
                break;
            case "10d":
                background.setBackgroundResource(R.drawable.d10d);
                break;
            case "10n":
                background.setBackgroundResource(R.drawable.d10n);
                break;
            case "11d":
                background.setBackgroundResource(R.drawable.d11d);
            case "11n":
                background.setBackgroundResource(R.drawable.d11n);
                break;
            case "13d":
                background.setBackgroundResource(R.drawable.d13d);
            case "13n":
                background.setBackgroundResource(R.drawable.d13n);
                break;
            case "50d":
                background.setBackgroundResource(R.drawable.d01d);
            case "50n":
                background.setBackgroundResource(R.drawable.d01n);
                break;
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
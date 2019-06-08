package com.kwiqbalance.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateFormat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.kwiqbalance.models.BalanceModel;

/**
 * Created by Ankit Chouhan on 29-12-2017.
 */

public class AppUtils {


    public static ArrayList<BalanceModel> getBalanceData(Context context) {
        String parseString = loadJSONFromAsset(context);
        ArrayList<BalanceModel> arrayList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(parseString);
            JSONArray countryArray = jsonObject.getJSONArray("response_data");
            for (int i = 0; i < countryArray.length(); i++) {
                JSONObject countryObj = countryArray.getJSONObject(i);
                BalanceModel country = new BalanceModel();
                country.setCarrierName(countryObj.getString("carrier_name"));
                country.setMessage(countryObj.getString("message"));
                country.setNumber(countryObj.getString("number"));
                arrayList.add(country);
            }
        } catch (Exception ignored) {

        }
        return arrayList;
    }

    /***
     * read country json from text file** @return
     */
    private static String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("Country.txt");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static boolean isInternetAvailable(Context ctx) {
        // using received context (typically activity) to get SystemService causes memory link as this holds strong reference to that activity.
        // use application level context instead, which is available until the app dies.
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        // if network is NOT available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }

        return false;
    }

    public static String getDateCurrentTimeZone(long timestamp) {
        try {
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp);
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
            Date currenTimeZone = calendar.getTime();
            return sdf.format(currenTimeZone);
        } catch (Exception e) {
        }
        return "";
    }

    public static String getDate(long timestamp) {

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        return DateFormat.format("dd-MM-yyyy hh:mm a", cal).toString();
    }
}

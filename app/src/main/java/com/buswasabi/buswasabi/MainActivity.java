package com.buswasabi.buswasabi;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.util.Log;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Random;

import android.graphics.Color;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import java.util.Hashtable;


public class MainActivity extends Activity  {

    /** Local variables **/
    GoogleMap googleMap;
    BusMarkerRenderer busMarkerRenderer;
    Hashtable<String, String> tripsHash;
    Hashtable<String, String> routesHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        busMarkerRenderer = new BusMarkerRenderer(this);
        routesHash = new Hashtable<String, String>();
        tripsHash = new Hashtable<String, String>();
        createMapView();
        // addMarker();

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            public boolean onMarkerClick(Marker marker) {
                System.out.println("CLICKED!");
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        new HttpRequestTask().execute();
    }

    /**
     * Initialises the mapview
     */
    private void createMapView(){
        /**
         * Catch the null pointer exception that
         * may be thrown when initialising the map
         */
        try {
            if(null == googleMap){
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();

                /**
                 * If the map is still null after attempted initialisation,
                 * show an error to the user
                 */
                if(null == googleMap) {
                    Toast.makeText(getApplicationContext(),
                            "Error creating map",Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception){
            Log.e("mapApp", exception.toString());
        }
    }

    /**
     * Adds a marker to the map
     */
    private void addMarker(){

        /** Make sure that the map has been initialised **/
        if(null != googleMap){
            googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(0, 0))
                            .title("Marker")
                            .draggable(true)
            );
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString().substring(2, sb.length() - 1);
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    private void addIcon(BusMarkerRenderer busMarkerRenderer, String text, LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(busMarkerRenderer.getBitmap(text))).
                position(position).
                anchor(0, 0);

        googleMap.addMarker(markerOptions);
    }


    private void renderLine() {

    }

    private class HttpRequestTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                JSONObject json = readJsonFromUrl("http://api.onebusaway.org/api/where/vehicles-for-agency/1.json?key=20db9014-d735-4e1f-bace-90f3e6651fc0&callback=?");
                return json;
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            JSONArray activeVehicles;
            try {
                Random randy = new Random();
                JSONObject data = (JSONObject) json.get("data");
                JSONObject references = (JSONObject) data.get("references");
                JSONArray routes = (JSONArray) references.get("routes");
                JSONArray trips = (JSONArray) references.get("trips");
                for(int i = 0; i < routes.length(); i++) {
                    routesHash.put((String) ((JSONObject) routes.get(i)).get("id"), (String) ((JSONObject) routes.get(i)).get("shortName"));
                }
                for(int i = 0; i < trips.length(); i++) {
                    tripsHash.put((String) ((JSONObject) trips.get(i)).get("id"), (String) ((JSONObject) trips.get(i)).get("routeId"));
                }
                activeVehicles = (JSONArray) data.get("list");
                for(int i = 0; i < activeVehicles.length(); i++) {
                    JSONObject currentVehicle = activeVehicles.getJSONObject(i);
                    if (!currentVehicle.isNull("tripStatus"))
                        addIcon(busMarkerRenderer, routesHash.get(tripsHash.get(currentVehicle.get("tripId"))),
                                new LatLng((Double) ((JSONObject) currentVehicle.get("location")).get("lat"),
                                        (Double) ((JSONObject) currentVehicle.get("location")).get("lon"))
                        );
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println(json);
        }
    }

    /*
    private class HttpRequestTaskPolyline extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                JSONObject json = readJsonFromUrl("http://api.onebusaway.org/api/where/trip-details/" + tripId + ".json?key=20db9014-d735-4e1f-bace-90f3e6651fc0&version=2&callback=?");
                return json;
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            JSONArray activeVehicles;
            try {
                Random randy = new Random();
                JSONObject data = (JSONObject) json.get("data");
                JSONObject references = (JSONObject) data.get("references");
                JSONArray routes = (JSONArray) references.get("routes");
                JSONArray trips = (JSONArray) references.get("trips");
                for(int i = 0; i < routes.length(); i++) {
                    routesHash.put((String) ((JSONObject) routes.get(i)).get("id"), (String) ((JSONObject) routes.get(i)).get("shortName"));
                }
                for(int i = 0; i < trips.length(); i++) {
                    tripsHash.put((String) ((JSONObject) trips.get(i)).get("id"), (String) ((JSONObject) trips.get(i)).get("routeId"));
                }
                activeVehicles = (JSONArray) data.get("list");
                for(int i = 0; i < activeVehicles.length(); i++) {
                    JSONObject currentVehicle = activeVehicles.getJSONObject(i);
                    if (!currentVehicle.isNull("tripStatus"))
                        addIcon(busMarkerRenderer, routesHash.get(tripsHash.get(currentVehicle.get("tripId"))),
                                new LatLng((Double) ((JSONObject) currentVehicle.get("location")).get("lat"),
                                        (Double) ((JSONObject) currentVehicle.get("location")).get("lon"))
                        );
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println(json);
        }
    }
    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
}

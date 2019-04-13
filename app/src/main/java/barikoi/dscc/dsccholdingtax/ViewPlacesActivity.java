package barikoi.dscc.dsccholdingtax;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import barikoi.barikoilocation.RequestQueueSingleton;

public class ViewPlacesActivity extends AppCompatActivity implements OnMapReadyCallback,  PlaceListAdapter.OnPlaceItemSelectListener  {
    private ArrayList<Place> items ;

    private PlaceListAdapter adapter;
    private RequestQueue queue;

    private String token;
    private MapView mapView;
    private MapboxMap mMap;
    protected  String TOKEN="token", android_id;
    private HashMap<Marker, Integer> mHashMap = new HashMap<>();
    private SeekBar seekbar;
   /* private PlaceLoadTask load;*/
    IconFactory iconFactory;
    Icon icon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_places);
        items = new ArrayList<>();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        token = prefs.getString(TOKEN,"");
        android_id= "1";
        queue = RequestQueueSingleton.getInstance(getApplicationContext()).getRequestQueue();
        RecyclerView listViewOwn = findViewById(R.id.listviewPlace);
        adapter = new PlaceListAdapter(items,this);
        listViewOwn.setAdapter(adapter);
        generateListFromSQLite();
        //generatelist(limits[seekbar.getProgress()]+"");

    }

    private void generateListFromSQLite() {
        PlaceDbAdapter placeDbAdapter=new PlaceDbAdapter(this);
        placeDbAdapter.open();
        ArrayList<Place> newitems=new ArrayList<>();
        newitems=placeDbAdapter.getSavedPlaces();
        items.clear();
        items.addAll(newitems);
        adapter.notifyDataSetChanged();
        placeDbAdapter.close();
    }

    public void onResume(){
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_places_menu, menu);
        MenuItem searchbar=menu.findItem(R.id.app_bar_search);
        SearchView searchview= (SearchView) searchbar.getActionView();
        searchbar.expandActionView();
        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText)){
                    adapter.getFilter().filter(newText);
                }
                return false;
            }
        });
        searchview.requestFocus();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

       /* if(id==R.id.action_map){
            if(mapcontainer.getVisibility()== View.GONE){
                mapcontainer.setVisibility(View.VISIBLE);
                item.setIcon(R.drawable.ic_listview);

            }
            else {
                mMap.clear();
                mapcontainer.setVisibility(View.GONE);
                item.setIcon(android.R.drawable.ic_menu_mapmode);
            }
        }*/


        return super.onOptionsItemSelected(item);
    }

/*
    public void generatelist(final String s){
        if(load!=null) load.cancel(true);
        StringRequest request = new StringRequest(Request.Method.GET,
                Api.url_byuid+android_id+"?limit="+s,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            final JSONArray placearray = new JSONArray(response.toString());

                            load = new PlaceLoadTask(new PlaceLoadListener() {

                                @Override
                                public void onplaceLoaded(ArrayList<Place> newitems) {
                                    items.clear();
                                    items.addAll(newitems);
                                    adapter.notifyDataSetChanged();
                                }
                            });
                            load.execute(placearray);
                        }catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ViewPlacesActivity.this, "problem or change in server, please wait and try again.", Toast.LENGTH_LONG).show();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ViewPlacesActivity.this, "problem with network or server, please wait and try again.", Toast.LENGTH_LONG).show();
                    }
                }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                if(!token.equals("")){
                    params.put("Authorization", "bearer "+token);
                }
                return params;
            }
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("limit", s);
                return params;
            }

        };
        queue.add(request);
    }*/

   /* public void deletePlace(final String ucode){

        RequestQueue queue = RequestQueueSingleton.getInstance(this).getRequestQueue();

        StringRequest request = new StringRequest(Request.Method.GET,
                deleteurl+ucode,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(ViewPlacesActivity.this,response,Toast.LENGTH_SHORT).show();
                        generatelist(limits[seekbar.getProgress()]+"");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ViewPlacesActivity.this, "Network error "+error, Toast.LENGTH_LONG).show();
                    }
                }) {

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                if(!token.equals("")){
                    params.put("Authorization", "bearer "+token);
                }
                return params;
            }
        };
        queue.add(request);
    }*/

    /*private void plotOnMap(ArrayList<Place> places){
        //Toast.makeText(this,"okay",Toast.LENGTH_LONG).show();
        mMap.clear();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if(places.size()>0 && mMap!=null){
            for(int i = 0;i<places.size();i++){
                double latitude = Double.parseDouble(places.get(i).getLat());
                double longitude = Double.parseDouble(places.get(i).getLon());
                Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude))
                        .icon(iconFactory.fromResource(R.drawable.marker_map))
                        .title(places.get(i).getAddress().split(",")[0]));
                builder.include(marker.getPosition());
                mHashMap.put(marker, i);
            }
            LatLngBounds bounds = builder.build();
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 2));
        }
    }
*/
    @Override
    public void onPlaceItemSelected(final Place mItem, int position) {

    }

    @Override
    public void onPlacedeleted(Place mItem) {
        if(!ViewPlacesActivity.this.isFinishing()){
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to delete this place?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                          /*  deletePlace(mItem.getCode());*/
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        }

    }

    @Override
    public void onPlaceEdited(Place mItem) {
      /*  Intent i= new Intent(this,PlaceEditActivity.class);
        i.putExtra("place_details",  mItem);
        startActivityForResult(i,50);*/
    }

    @Override
    public void onPlaceCorrect(Place mItem) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
/*
        if(load!=null) load.cancel(true);
*/
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        mMap=mapboxMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //generatelist();
        //plotOnMap(items);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==50) {
           /* if(resultCode==RESULT_UPDATED){
                Place place= (Place)data.getSerializableExtra("place_edit");
                Log.d("TaufiqPlaceEdit",place.getAddress());
               *//* items.set(adapter.findposition(place.getCode()),place);
                adapter.notifyDataSetChanged();*//*
                placeEdited(place.getCode());
            }*/
        }
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }

    }

    public void placeEdited(final String uCode){
       /* if(load!=null) load.cancel(true);
        StringRequest request = new StringRequest(Request.Method.GET,
                Api.url_PlaceAfterEditByUCode+uCode,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            final JSONObject jsonObject=new JSONObject(response);
                            Place place=NetworkcallUtils.getPlace(jsonObject);
                            items.set(adapter.findposition(place.getCode()),place);
                            adapter.notifyDataSetChanged();
                        }catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ViewPlacesActivity.this, "problem or change in server, please wait and try again.", Toast.LENGTH_LONG).show();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ViewPlacesActivity.this, "problem with network or server, please wait and try again.", Toast.LENGTH_LONG).show();
                    }
                }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                if(!token.equals("")){
                    params.put("Authorization", "bearer "+token);
                }
                return params;
            }
        };
        queue.add(request);*/
    }

}

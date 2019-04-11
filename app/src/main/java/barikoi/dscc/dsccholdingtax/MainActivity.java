package barikoi.dscc.dsccholdingtax;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerOptions;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.mapboxsdk.plugins.offline.ui.OfflineActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import barikoi.barikoilocation.BarikoiAPI;
import barikoi.barikoilocation.NearbyPlace.NearbyPlaceAPI;
import barikoi.barikoilocation.NearbyPlace.NearbyPlaceListener;
import barikoi.barikoilocation.PlaceModels.NearbyPlace;
import barikoi.barikoilocation.RequestQueueSingleton;

import static barikoi.dscc.dsccholdingtax.Api.logouturl;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener, PermissionsListener, NavigationView.OnNavigationItemSelectedListener {
    String[] permissions={
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET
    };
    private RequestQueue queue;
    private SharedPreferences prefs;
    private String token;
    protected  String USER_ID="user_id",PASSWORD="password",TOKEN="token",LANG="language",EMAIL="email",NAME="name",POINTS="points",PHONE="phone";
    ArrayList<NearbyPlace> nearbyPlaces;
    private final int REQUEST_CHECK_SETTINGS = 1;
    private PermissionsManager permissionsManager;
    private MapView mapView;
    private MapboxMap map;
    public final static int MULTIPLE_PERMISSIONS=5;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationPlugin;
    private Location originLocation;
    private Button selectplace,submit;
    private BottomSheetBehavior /*bottomSheetBehavior,suggestSheetBehavior,*/inputPlaceBottomSheet;
    private LinearLayout /*viewBottomSheet,*/ inputBottomSheet;
  /*  private View viewSuggestionSheet;
    private RecyclerView suggView;
    private PlaceAddressAdapter placeArrayAdapter;
    Spinner buildingType,buildingCategory,roadPlacement;
    EditText floorNumbers,sftperFloor;
    TextView price;
    String[] builType={"পাকা","সেমি-পাকা","কাঁচা"};
    String[] builcategory={"আবাসিক","বানিজ্যিক","শিল্প-কারখানা"};
    String[] roadside={"মেইন রাস্তার পাশে","রাস্তার ৩০০ ফিটের মধ্যে"};
    TextView priceRateShow;*/
    private LocationTask getloc;
    private Double lon, lat;
    private String holdingNo,holdingDes,road,block,section,sector,subArea,area,postcode,ward,zone;
    private EditText holdingText,holdingDesText,roadText,blockText,sectorText,sectionText,subAreaText,areaText,postcodeText,wardText,zoneText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Mapbox.getInstance(this,getString(R.string.access_token));
        BarikoiAPI.getINSTANCE(this,"MTExMTpKQkZZMzNIQk45");
        mapView=findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        queue = RequestQueueSingleton.getInstance(getApplicationContext()).getRequestQueue();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        token = prefs.getString(TOKEN,"");
        init();
    }
    public void init(){

        holdingText=findViewById(R.id.editTextHoldinNo);
        holdingDesText=findViewById(R.id.editTextHoldingDesc);
        roadText=findViewById(R.id.editTextRoad);
        blockText=findViewById(R.id.editTextBlock);
        sectorText=findViewById(R.id.editTextSector);
        sectionText=findViewById(R.id.editTextSection);
        subAreaText=findViewById(R.id.editTextSubArea);
        areaText=findViewById(R.id.autoCompleteTextViewArea);
        postcodeText=findViewById(R.id.editTextPostal) ;
        wardText=findViewById(R.id.editTextWard);
        zoneText=findViewById(R.id.editTextZone);
       /* priceRateShow=findViewById(R.id.price_rate_show);
        floorNumbers=findViewById(R.id.building_floors);
        sftperFloor=findViewById(R.id.squarefeetperfloor);
        price=findViewById(R.id.price_rate_show);
        viewBottomSheet = findViewById(R.id.price_bottom_sheet) ;
        bottomSheetBehavior = BottomSheetBehavior.from (viewBottomSheet) ;
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setSkipCollapsed(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        viewSuggestionSheet= findViewById(R.id.suggestlistsheet);
        suggestSheetBehavior= BottomSheetBehavior.from(viewSuggestionSheet);
        suggestSheetBehavior.setHideable(true);
        suggestSheetBehavior.setSkipCollapsed(true);
        suggestSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);*/

        inputBottomSheet=findViewById(R.id.design_bottom_sheet_input);
        inputPlaceBottomSheet= BottomSheetBehavior.from(inputBottomSheet);
        inputPlaceBottomSheet.setHideable(true);
        inputPlaceBottomSheet.setSkipCollapsed(false);
        inputPlaceBottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

     /*   buildingType=findViewById(R.id.spinnerbuildingType);
        buildingCategory=findViewById(R.id.spinnerbuilding_usage_type);
        roadPlacement=findViewById(R.id.spinnerbuilding_road_side_type);*/

      /*  ArrayAdapter<String> dataAdaptertype = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, builType);
        ArrayAdapter<String> dataAdapterCategory = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, builcategory);
        ArrayAdapter<String> dataAdapterRoad = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, roadside);
        nearbyPlaces=new ArrayList<>();*/
      /*  buildingType.setAdapter(dataAdaptertype);
        buildingCategory.setAdapter(dataAdapterCategory);
        roadPlacement.setAdapter(dataAdapterRoad);
        suggView=findViewById(R.id.suggestlist);*/

     /*   placeArrayAdapter=new PlaceAddressAdapter(nearbyPlaces, new PlaceAddressAdapter.OnPlaceItemSelectListener() {
            @Override
            public void onPlaceItemSelected(NearbyPlace mItem, int position) {
                suggestSheetBehavior.setHideable(true);
                suggestSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        suggView.setAdapter(placeArrayAdapter);
        buildingType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                buildingType.setSelection(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        buildingCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                buildingCategory.setSelection(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        roadPlacement.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                roadPlacement.setSelection(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/
        selectplace = this.findViewById(R.id.buttonselectplace);
        selectplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*  suggestSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                LatLng target=map.getCameraPosition().target;
                double lat=target.getLatitude();
                double lon=target.getLongitude();
                    NearbyPlaceAPI.builder(MainActivity.this)
                            .setLatLng(lat,lon)
                            .setLimit(10)
                            .setDistance(.5)
                            .build()
                            .generateNearbyPlaceList(new NearbyPlaceListener() {
                                @Override
                                public void onPlaceListReceived(ArrayList<NearbyPlace> places) {
                                    Log.d("TaufiqMainActivity","Recieved");
                                    nearbyPlaces.clear();
                                    nearbyPlaces.addAll(places);
                                    placeArrayAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onFailure(String message) {
                                    if(message.equals("not found")){
                                        suggestSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                                        inputPlaceBottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                                        Log.d("TaufiqMainActivity","not found");
                                        nearbyPlaces.clear();
                                        placeArrayAdapter.notifyDataSetChanged();

                                    }

                                }
                            });*/
/*

                    //initsuggestionList(lat,lon);
                  */
                //Toast.makeText(AddPlaceActivity.this,lat+" "+lon, Toast.LENGTH_LONG).show();

                /*if (mMap.getCameraPosition().zoom >= 17.0f) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    init();
                }
                else {
                    Toast.makeText(AddPlaceActivity.this, getString(R.string.need_more_zoom), Toast.LENGTH_LONG).show();
                }*/
            }

        });
        submit=findViewById(R.id.buttonSubmitInput);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                boolean inputOk = true;
                subArea=subAreaText.getText().toString().trim().replaceAll(" +", " ");
                area=areaText.getText().toString().trim().replaceAll(" +", " ");
                holdingNo=holdingText.getText().toString().trim().replaceAll(" +", " ");
                road=roadText.getText().toString().trim().replaceAll(" +", " ");
                block=blockText.getText().toString().trim().replaceAll(" +", " ");
                sector=sectorText.getText().toString().trim().replaceAll(" +", " ");
                section=sectionText.getText().toString().trim().replaceAll(" +", " ");
                holdingDes = holdingDesText.getText().toString().trim().replaceAll(" +", " ");
                postcode = postcodeText.getText().toString().trim().replaceAll(" +", " ");
                ward=wardText.getText().toString().trim().replaceAll(" +", " ");
                zone=zoneText.getText().toString().trim().replaceAll(" +", " ");

                if (holdingNo.equals("")) {
                    inputOk = false;
                    holdingText.setError(getString(R.string.this_field_is_required));
                    hideProgress();
                }
                if (area.equals("")) {
                    inputOk = false;
                    areaText.setError(getString(R.string.this_field_is_required));
                    hideProgress();
                }
                if (ward.equals("")) {
                    inputOk = false;
                    wardText.setError(getString(R.string.this_field_is_required));
                    hideProgress();
                }
                if (zone.equals("")) {
                    inputOk = false;
                    zoneText.setError(getString(R.string.this_field_is_required));
                    hideProgress();
                }
                if (postcode.equals("")) {
                    inputOk = false;
                    postcodeText.setError(getString(R.string.this_field_is_required));
                    hideProgress();
                }
                if(inputOk) {
                    submit.setEnabled(false);
                    addPlace();
                }
            }
        });
        findViewById(R.id.buttonsubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputPlaceBottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
       /* submit=findViewById(R.id.buttonbook);
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int floorNo=Integer.parseInt(floorNumbers.getText().toString());
                int squarefeet=Integer.parseInt(sftperFloor.getText().toString());
                int totalSqft=floorNo*squarefeet;
                String type=buildingType.getSelectedItem().toString();
                String category=buildingCategory.getSelectedItem().toString();
                String placement=roadPlacement.getSelectedItem().toString();
                double price=calculate(type,category,placement);
                double result=totalSqft*price*1.2;
                priceRateShow.setText(String.valueOf(result));
                Intent intent=new Intent(MainActivity.this,AssessmentActivity.class);
                intent.putExtra("floorNo",floorNo);
                intent.putExtra("squareFeet",squarefeet);
                intent.putExtra("totalSQT",totalSqft);
                intent.putExtra("buildingType",type);
                intent.putExtra("buildingCategory",category);
                intent.putExtra("buildingPlacement",placement);
                intent.putExtra("ratepersqt",price);
                intent.putExtra("totalTax",result);
                startActivity(intent);
                finish();
            }
        });*/
        //DrawerLayout and Navigation Drawer Initialize
        AppBarLayout appbar= findViewById(R.id.appBarLayout);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        appbar.bringToFront();
        setSupportActionBar(toolbar);
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
    private boolean isNotEmpty(EditText etText) {
        return etText.getText().toString().trim().length() > 0;
    }
    public void addPlace(){
        savePlaceInDB(holdingNo, holdingDes,  lon+"",  lat+"",  road,  block,  sector,  section,  subArea,  area,  postcode,  ward,  zone);
        Toast.makeText(MainActivity.this,"Place saved offline",Toast.LENGTH_LONG).show();
        hideProgress();
        holdingText.setText("");
        //final long mRequestStartTime = System.currentTimeMillis();
       /* StringRequest request = new StringRequest(Request.Method.POST,
                Api.insertUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        submit.setEnabled(true);
                        hideProgress();
                        //Toast.makeText(addPlaceFragment.this,(long)(System.currentTimeMillis() - mRequestStartTime)+"",Toast.LENGTH_LONG).show();
                        if(response!=null) {
                            try {
                                JSONObject data = new JSONObject(response);
                                String ucode=data.getString("uCode");
                                Place place =new Place(holdingNo,holdingDes,lon+"",lat+"",road,block,sector,section,subArea,area,postcode,ward,zone);
                                ViewDialog congrats = new ViewDialog();
                                //im.removeAllPhotoes();
                                holdingText.setText("");
                                if(!MainActivity.this.isFinishing()){
                                    congrats.showmessageDialog(MainActivity.this, "Done", "Place Added Successfully.");
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                       *//*     map.addMarker(new MarkerOptions().position(new LatLng(lat,lon))
                                    .icon(iconFactory.fromResource(R.drawable.marker_plot1))
                                    .title("undefined, "+address));*//*
                            inputPlaceBottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                            Toast.makeText(MainActivity.this,"Server error,place saved offline",Toast.LENGTH_LONG).show();
                            savePlaceInDB(holdingNo, holdingDes,  lon+"",  lat+"",  road,  block,  sector,  section,  subArea,  area,  postcode,  ward,  zone);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        submit.setEnabled(true);
                        hideProgress();
                        if(error!=null){

                            if(error instanceof TimeoutError || error instanceof NoConnectionError){
                                Toast.makeText(getApplicationContext(), "Network error,place saved offline" , Toast.LENGTH_LONG).show();
                            }
                            else Toast.makeText(getApplicationContext(), "Sorry, could not add place right now,place saved offline " , Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(MainActivity.this, "problem connecting to server,place saved offline", Toast.LENGTH_LONG).show();
                        }
                        inputPlaceBottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                      *//*  map.addMarker(new MarkerOptions().position(new LatLng(lat,lon))
                                .icon(iconFactory.fromResource(R.drawable.marker_plot1))
                                .title("undefined, "+address));*//*

                        savePlaceInDB(holdingNo, holdingDes,  lon+"",  lat+"",  road,  block,  sector,  section,  subArea,  area,  postcode,  ward,  zone);
                    }
                }) {
            @Override

            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                //parameters.put("id", id.getText().toString());
                parameters.put("longitude", lon+"");
                parameters.put("latitude", lat+"");
                parameters.put("Address", holdingNo);
                parameters.put("area", area);
                parameters.put("postCode", postcode);
                parameters.put("subType", ward);
                parameters.put("subType", zone);


                if(sectionText.getText().toString().trim().length()>0)
                    parameters.put("holding_no",sectionText.getText().toString().trim());
                if(roadText.getText().toString().trim().length()>0)
                    parameters.put("road_name_number",roadText.getText().toString().trim());
                if(blockText.getText().toString().trim().length()>0)
                    parameters.put("road_name_number",blockText.getText().toString().trim());
                if(sectorText.getText().toString().trim().length()>0)
                    parameters.put("name",sectorText.getText().toString().trim());
                if(subAreaText.getText().toString().length()>0)
                    parameters.put("contact_person_name",subAreaText.getText().toString());
                if(holdingDesText.getText().toString().length()>0)
                    parameters.put("contact_person_phone",holdingDesText.getText().toString());
                return parameters;
            }
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<>();
                //parameters.put("id", id.getText().toString());
                if(!token.equals("")) {
                    parameters.put("Authorization", "bearer " + token);
                }
                return parameters;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(40 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
        showProgress();*/
    }
    private void showProgress(){
        findViewById(R.id.progressBar2).setVisibility(View.VISIBLE);
    }
    private void hideProgress(){
        findViewById(R.id.progressBar2).setVisibility(View.GONE);
    }
    private void savePlaceInDB(String holdingNo, String holdingDes, String lon, String lat, String road, String block, String sector, String section, String subarea, String area, String postalcode, String ward, String zone){
        PlaceDbAdapter db=new PlaceDbAdapter(this);
        db.open();
        db.addPlace( holdingNo, holdingDes,  lon,  lat,  road,  block,  sector,  section,  subarea,  area,  postalcode,  ward,  zone);
        db.close();
    }

    public double calculate(String type,String category,String placement ){
        double rate=1.0;
     switch (placement){
         case "মেইন রাস্তার পাশে":
             switch (category){
                 case "আবাসিক":
                     switch (type){
                         case "পাকা":
                             rate=7;
                             break;
                         case "সেমি-পাকা":
                             rate=6;
                             break;
                         case "কাঁচা":
                             rate=5;
                             break;
                     }
             break;
                 case "বানিজ্যিক":
                     switch (type){
                         case "পাকা":
                             rate=10;
                             break;
                         case "সেমি-পাকা":
                             rate=8;
                             break;
                         case "কাঁচা":
                             rate=7;
                             break;
                     }
                 break;
                 case "শিল্প-কারখানা":
                     switch (type){
                         case "পাকা":
                             rate=8;
                             break;
                         case "সেমি-পাকা":
                             rate=7;
                             break;
                         case "কাঁচা":
                             rate=6;
                             break;
                     }
                  break;
             }
            break;
         case "রাস্তার ৩০০ ফিটের মধ্যে":
             switch (category){
                 case "আবাসিক":
                     switch (type){
                         case "পাকা":
                             rate=6.5;
                             break;
                         case "সেমি-পাকা":
                             rate=5;
                             break;
                         case "কাঁচা":
                             rate=4;
                             break;
                     }
                     break;
                 case "বানিজ্যিক":
                     switch (type){
                         case "পাকা":
                             rate=8.1;
                             break;
                         case "সেমি-পাকা":
                             rate=6;
                             break;
                         case "কাঁচা":
                             rate=5;
                             break;
                     }
                     break;
                 case "শিল্প-কারখানা":
                     switch (type){
                         case "পাকা":
                             rate=7;
                             break;
                         case "সেমি-পাকা":
                             rate=6;
                             break;
                         case "কাঁচা":
                             rate=5;
                             break;
                     }
                     break;
             }
             break;
     }
     return rate;
 }
   /* private void initsuggestionList(double lat,double lon){
        suggestSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        suggestSheetBehavior.setHideable(false);
        StringRequest request = new StringRequest(Request.Method.GET,
                Api.addplacesugg+"?longitude="+lon+"&latitude="+lat,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            loading.setVisibility(View.GONE);
                            JSONArray placearray = new JSONArray(response.toString());
                            ArrayList<com.barikoi.barikoi.CustomModels.Place> newplaces=JsonUtils.getPlaces(placearray);
                            IconFactory iconFactory = IconFactory.getInstance(AddPlaceActivity.this);
                            Icon icon = iconFactory.fromResource(R.drawable.mapmarkerforshowplaces);
                            for(com.barikoi.barikoi.CustomModels.Place p : newplaces){
                                LatLng point=new LatLng(Double.parseDouble(p.getLat()),Double.parseDouble(p.getLon()));
                                map.addMarker(new com.mapbox.mapboxsdk.annotations.MarkerOptions().position(point).icon(icon));
                            }
                            if(newplaces.size()>0) {
                                placeArrayAdapter=new PlaceAddressAdapter(newplaces, (mItem, position) -> initInputFromPlace(mItem));
                                suggView=findViewById(R.id.suggestlist);
                                suggView.setAdapter(placeArrayAdapter);
                                areaText.setText(newplaces.get(0).getArea());
                                postalText.setText(newplaces.get(0).getPostalcode());
                                cityText.setText(newplaces.get(0).getCity());
                            }else{
                                //Toast.makeText(AddPlaceActivity.this,lat+" "+lon, Toast.LENGTH_LONG).show();

                                suggestSheetBehavior.setHideable(true);
                                suggestSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.setVisibility(View.GONE);
                        //Toast.makeText(getActivity(),"Network error",Toast.LENGTH_SHORT).show();
                    }
                }) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                if(!token.equals("")){
                    params.put("Authorization", "bearer "+token);
                }
                return params;
            }
        };
        queue.add(request);
        loading.setVisibility(View.VISIBLE);
        findViewById(R.id.buttonskip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearInputData();
                suggestSheetBehavior.setHideable(true);
                suggestSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            }
        });
    }*/
    private  boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
            return false;
        }
        return true;
    }
    /**
     * This function checks for location permission, if granted initializes location plugin
     */
    private void enableLocation(){
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Create an instance of LOST location engine
            initializeLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }
    /**
     * It sets the location Engine and gets the last known location
     */
    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine() {
        Log.d("PermissionsDSCC","Initialize");
        LocationEngineProvider locationEngineProvider = new LocationEngineProvider(this);
        locationEngine = locationEngineProvider.obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();
        LocationLayerOptions llopt= LocationLayerOptions.builder(this).maxZoom(25).build();
        locationPlugin = new LocationLayerPlugin(mapView, map, locationEngine,llopt);
        locationPlugin.setRenderMode(RenderMode.COMPASS);
        // currentLocation.displayLocation();
        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            originLocation = lastLocation;
            setCameraPosition(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }
    private void setCameraPosition(LatLng location){
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 13));
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        // Check which request we're responding to
       /* if (requestCode == REQUEST_CHECK_SETTINGS||requestCode==2) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                getloc.displayLocation();
                Log.d("PermissionsDSCC","OnActivity Result OK");
                enableLocation();
            }
        }*/
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /*switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getloc.checkForLocationsettings();
                    Log.d("PermissionsDSCC","OnRequest IF");
                } else {
                    Log.d("PermissionsDSCC","OnRequest ELSE");
                }
                break;
            }
            default:
                break;
        }*/
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map=mapboxMap;
        enableLocation();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected() {
        Log.d("PermissionsDSCC","OnConnected");
       locationEngine.requestLocationUpdates();
        FloatingActionButton fab=findViewById(R.id.myLocationButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                @SuppressLint("MissingPermission")
                Location lastLocation = locationEngine.getLastLocation();
                if (lastLocation!=null) {
                    originLocation = lastLocation;
                    Double lat = lastLocation.getLatitude();
                    Double lon = lastLocation.getLongitude();
                    LatLng latLng = new LatLng(lat, lon);
                    map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                }
                else{
                    locationEngine.requestLocationUpdates();
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLocationChanged(Location location) {
        Log.d("PermissionsDSCC","OnLocChanged");
        if(location!=null){
            originLocation = location;
            Double lat = location.getLatitude();
            Double lon = location.getLongitude();
            LatLng latLng = new LatLng(lat, lon);
            map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            /*  generatelist(lat,lon);*/

            locationEngine.removeLocationEngineListener(this);
        }
        else{
            locationEngine.requestLocationUpdates();
        }
    }
    @SuppressLint("MissingPermission")
    protected void onStart() {
        super.onStart();
        if (locationEngine != null) {
            locationEngine.requestLocationUpdates();
        }
        if (locationPlugin != null) {
            locationPlugin.onStart();
        }
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationPlugin != null) {
            locationPlugin.onStop();
        }
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if(granted){
            Log.d("PermissionsDSCC","OnPermission");
            enableLocation();
        }
    }
    private void logout(){
        SharedPreferences.Editor editor=prefs.edit();
        editor.remove(TOKEN);
        editor.remove(NAME);
        editor.remove(USER_ID);
        editor.remove(PHONE);
        editor.remove(POINTS);
        editor.commit();
        //clearCache();

        RequestQueue queue= RequestQueueSingleton.getInstance(getApplicationContext()).getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.DELETE,
                logouturl,
                response -> {
                    Intent home =new Intent(MainActivity.this,LoginActivity.class);
                    finish();
                    startActivity(home);
                },
                error -> {
                    Intent home =new Intent(MainActivity.this,LoginActivity.class);
                    finish();
                    startActivity(home);

                }) {
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                if(!token.equals("")){
                    params.put("Authorization", "bearer "+token);
                }
                return params;
            }
        };
        queue.add(request);
    }
  /*  private void clearCache(){
        PlaceDbAdapter db=new PlaceDbAdapter(this);
        db.open();
        db.deleteAllPlaces();
        db.close();
    }*/
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.viewplace) {
            Intent i=new Intent(this,ViewPlacesActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

        }
        else if (id == R.id.offline) {
            Intent i=new Intent(this, Offline_Activity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        }
        else if(id ==R.id.logout){
            new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog)
                    .setTitle(R.string.log_out)
                    .setMessage(R.string.sure_log_out)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> logout())
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {
                        // do nothing
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

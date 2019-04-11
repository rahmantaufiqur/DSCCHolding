package barikoi.dscc.dsccholdingtax;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import barikoi.barikoilocation.RequestQueueSingleton;

public class Offline_Activity extends AppCompatActivity implements PlaceListAdapter.OnPlaceItemSelectListener {
    private PlaceListAdapter adapter;
    private RequestQueue queue;
    private RecyclerView listViewOffline;

    private String token;
    private Handler handler;
    private SharedPreferences prefs;
    private RelativeLayout mapcontainer;
    private HashMap<Marker, Integer> mHashMap = new HashMap<Marker, Integer>();
    private ArrayList<Place> items;
    private ProgressDialog pd;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_);

        items = new ArrayList<Place>();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        token = prefs.getString(Api.TOKEN, "");

        queue = RequestQueueSingleton.getInstance(getApplicationContext()).getRequestQueue();
        listViewOffline = (RecyclerView) findViewById(R.id.listviewPlace);

        adapter = new PlaceListAdapter(items, this);
        listViewOffline.setAdapter(adapter);
        generatelist();
    }

    private void generatelist() {
        PlaceDbAdapter db = new PlaceDbAdapter(this);
        db.open();
        items.clear();
        ArrayList<Place> places = db.getSavedPlaces();
        items.addAll(places);
        db.close();
        /*for (Place p : places) {
            Toast.makeText(this, p.toString(), Toast.LENGTH_LONG).show();
        }*/
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPlaceItemSelected(Place mItem, int position) {

    }

    @Override
    public void onPlacedeleted(Place mItem) {
        deleteFromDB(mItem);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPlaceEdited(Place mItem) {
        Toast.makeText(this,"edit not possible",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPlaceCorrect(Place mItem) {

    }

    private void deleteFromDB(Place mItem) {
        PlaceDbAdapter db = new PlaceDbAdapter(this);
        db.open();
        db.deletePlace(mItem.getHoldingNo());
        db.close();
        items.remove(mItem);
        adapter.notifyDataSetChanged();
    }

    private void addFromDB(final Place mItem) {
        /*StringRequest request = new StringRequest(Request.Method.POST,
                insertUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //Toast.makeText(addPlaceFragment.this,(long)(System.currentTimeMillis() - mRequestStartTime)+"",Toast.LENGTH_LONG).show();
                        if (response != null) {

                            try {
                                JSONObject data = new JSONObject(response);
                                deleteFromDB(mItem);
                                refreshData();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(Offline_Activity.this, "Server error", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(), "Sorry, could not add place right now, please try again later. ", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override

            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                //parameters.put("id", id.getText().toString());
                parameters.put("longitude", mItem.getLon()+"");
                parameters.put("latitude", mItem.getLat()+"");
                parameters.put("Address", mItem.getHoldingNo());
                parameters.put("area", mItem.getArea());
                parameters.put("postCode", mItem.getPostalcode());
                parameters.put("subType", mItem.getWard());
                parameters.put("subType", mItem.getZone());


                if(mItem.getSection().length()>0)
                    parameters.put("holding_no",mItem.getSection().trim());
                if(mItem.getRoad().length()>0)
                    parameters.put("road_name_number",mItem.getRoad().trim());
                if(mItem.getBlock().trim().length()>0)
                    parameters.put("road_name_number",mItem.getBlock().trim());
                if(mItem.getSector().trim().length()>0)
                    parameters.put("name",(mItem.getSector().trim()));
                if(mItem.getSubarea().length()>0)
                    parameters.put("contact_person_name",mItem.getSubarea());
                if(mItem.getHoldingDesc().length()>0)
                    parameters.put("contact_person_phone",mItem.getHoldingDesc());
                return parameters;
            }

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                //parameters.put("id", id.getText().toString());
                if (!token.equals("")) {
                    parameters.put("Authorization", "bearer " + token);
                }
                return parameters;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(40 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);*/
    }

    private void refreshData() {
        if (items.size() > 0) {
            addFromDB(items.get(0));
        } else {
            pd.dismiss();
            new AlertDialog.Builder(this)
                    .setMessage("all places added")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create()
                    .show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.offline_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.refresh) {
            pd = new ProgressDialog(this);
            pd.setMessage("uploading..");
            refreshData();
        }
        return super.onOptionsItemSelected(item);
    }

}

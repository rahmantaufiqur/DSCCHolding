package barikoi.dscc.dsccholdingtax;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by Sakib on 6/25/2017.
 */

public class LocationTask {
        public int REQUEST_CHECK_SETTINGS = 0x2;
        public int REQUEST_CHECK_PERMISSION = 0x1;
        private final LocationRequest mLocationRequest;

        private final LocationCallback mLocationCallback;
        private LocationTaskListener listener;
        Context context;

        private FusedLocationProviderClient mFusedLocationClient;
        private static final String TAG="LocationTask";
        private Handler handler;

        public LocationTask(Context context) {
                this.context = context;


                mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(5000);
                mLocationRequest.setFastestInterval(3000);
                mLocationRequest.setSmallestDisplacement(5);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

                mLocationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                                for (Location location : locationResult.getLocations()) {
                                        // Update UI with location data
                                        onLocationChanged(location);
                                }
                        };
                };
        }

        public LocationTask(Context context, LocationTaskListener listener){
                this.context=context;
                this.listener=listener;
        /*GoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();*/
                mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(5000);
                mLocationRequest.setFastestInterval(3000);
                mLocationRequest.setSmallestDisplacement(5);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

                mLocationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                                for (Location location : locationResult.getLocations()) {
                                        // Update UI with location data
                                        onLocationChanged(location);
                                }
                        };
                };
        }


        public synchronized void buildGoogleApiClient() {
       /* if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting())
            mGoogleApiClient.connect();*/
                displayLocation();

        }


        public void displayLocation() {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CHECK_PERMISSION);

                } else {
                        checkForLocationsettings();
                }
        }

        public void checkForLocationsettings() {
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(mLocationRequest);

                LocationServices.getSettingsClient(context).checkLocationSettings(builder.build())
                        .addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                                @Override
                                public void onComplete(Task<LocationSettingsResponse> task) {
                                        try {
                                                LocationSettingsResponse response = task.getResult(ApiException.class);
                                                // All location settings are satisfied. The client can initialize location
                                                getLastLocation();

                                        } catch (ApiException exception) {
                                                switch (exception.getStatusCode()) {
                                                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                                                // Location settings are not satisfied. But could be fixed by showing the
                                                                // user a dialog.
                                                                try {
                                                                        // Cast to a resolvable exception.
                                                                        ResolvableApiException resolvable = (ResolvableApiException) exception;
                                                                        // Show the dialog by calling startResolutionForResult(),
                                                                        // and check the result in onActivityResult().
                                                                        resolvable.startResolutionForResult(
                                                                                (Activity) context,
                                                                                REQUEST_CHECK_SETTINGS);
                                                                } catch (IntentSender.SendIntentException e) {
                                                                        // Ignore the error.
                                                                } catch (ClassCastException e) {
                                                                        // Ignore, should be an impossible error.
                                                                }
                                                                break;
                                                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                                                // Location settings are not satisfied. However, we have no way to fix the
                                                                // settings so we won't show the dialog.
                                                                //mGoogleApiClient.disconnect();
                                                                break;
                                                }
                                        }
                                }
                        });

        }

        private void getLastLocation() {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                }

                mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                        mLocationCallback,
                        null /* Looper */);
                handler= new Handler();
                handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                                stoptask();
                        }
                }, 10000);
        }

        public void stoptask(){
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                //mGoogleApiClient.disconnect();
                handler.removeCallbacksAndMessages(null);
        }

        public void onLocationChanged(Location location) {
                stoptask();
                /*getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);*/
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                //mGoogleApiClient.disconnect();
                if(listener!=null)
                        listener.OnLocationChanged(location);
        }
}
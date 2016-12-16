package app.gotcha;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by macbook on 3/17/16.
 */
public class GotchaService extends Service implements LocationListener {

    protected LocationManager locationManager;
    Location location;
    Context context;
    private static final long MIN_DISTANCE_FOR_UPDATE = 10;
    private static final long MIN_TIME_FOR_UPDATE = 1000;


    public GotchaService() {
        context = AppController.getInstance();
        locationManager = (LocationManager) context
                .getSystemService(LOCATION_SERVICE);
    }

    public GotchaService(Context context) {
        this.context = context;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        return START_STICKY;
    }

    public Location getLocation(String provider) {
        if (locationManager.isProviderEnabled(provider)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            locationManager.requestLocationUpdates(provider, MIN_TIME_FOR_UPDATE, MIN_DISTANCE_FOR_UPDATE, this);
            if (locationManager != null) {
                location = locationManager.getLastKnownLocation(provider);

                return location;
            }
        }
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            PreferenceManager
                    .getDefaultSharedPreferences(AppController.getInstance())
                    .edit()
                    .putString("lat", location.getLatitude() + "").apply();

            PreferenceManager
                    .getDefaultSharedPreferences(AppController.getInstance())
                    .edit()
                    .putString("lon", location.getLongitude() + "").apply();

        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


}

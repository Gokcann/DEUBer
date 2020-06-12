package com.deubercomp.deuber;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

import static com.deubercomp.deuber.R.id.otostopcu;
import static com.deubercomp.deuber.R.id.surucu;

@SuppressWarnings("deprecation")
public class SurucuHaritaActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;

    //API Client icin degisken olusturuyoruz
    GoogleApiClient vGoogleApiClient;
    Location vSonKonum;
    LocationRequest vKonumAl;

    private Button vCikis;
    private String otostopcuId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surucu_harita);
        //burada google maps cagiriliyor (map activity de default geldi)
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //app üzerinde map calistiriliyor
        mapFragment.getMapAsync(this);

        vCikis = (Button) findViewById(R.id.cikis);
        vCikis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(SurucuHaritaActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        otostopcuAtama();
    }

    private void otostopcuAtama(){
        String surucuId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference otostopcuAtamaRef= FirebaseDatabase.getInstance().getReference().child("Kullanici").child("Surucu").child(surucuId).child("otostopcuId");
        otostopcuAtamaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                        otostopcuId = dataSnapshot.getValue().toString();
                        otostopcuLokasyonAlma();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void otostopcuLokasyonAlma(){
        //otostopcuAtama metodunun aynisi

        DatabaseReference otostopcuLokasyonAlmaRef = FirebaseDatabase.getInstance().getReference().child("otostopcuIstek").child(otostopcuId).child("l");
        otostopcuLokasyonAlmaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> harita = (List<Object>) dataSnapshot.getValue();
                    double lokasyonLat= 0;
                    double lokasyonLng= 0;
                    if(harita.get(0) != null) {
                        lokasyonLat = Double.parseDouble(harita.get(0).toString());
                    }
                    if(harita.get(1) != null){
                        lokasyonLng = Double.parseDouble(harita.get(1).toString());
                    }
                    LatLng surucuLatLng = new LatLng(lokasyonLat,lokasyonLng);
                    mMap.addMarker(new MarkerOptions().position(surucuLatLng).title("Lokasyon alındı."));
                    }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

       /*
        LatLng sydney = new LatLng(38.368203, 27.203631);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Dokuz Eylul Universitesi"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient() {
        vGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        vGoogleApiClient.connect();
    }

    //asagidaki metodlarin tamami GoogleApiClient.ConnectionCallbacks,
//  GoogleApiClient.OnConnectionFailedListener, LocationListener bunlar eklendikten sonra geldi
    @Override
    public void onLocationChanged(Location location) {
        if (getApplicationContext() != null) {

            vSonKonum = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

            //lokasyon degistiginde anlık database'i guncellemek icin
            String kullaniciId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference refKontrol = FirebaseDatabase.getInstance().getReference("surucuKontrol");
            DatabaseReference refAktif = FirebaseDatabase.getInstance().getReference("surucuAktif");
            GeoFire geoFireKontrol = new GeoFire(refKontrol);
            GeoFire geoFireAktif = new GeoFire(refAktif);

        switch (otostopcuId) {
            case "":
                //burasi arastirilacak
                geoFireAktif.removeLocation(kullaniciId);
                geoFireKontrol.setLocation(kullaniciId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                break;

            default:
                geoFireKontrol.removeLocation(kullaniciId);
                geoFireAktif.setLocation(kullaniciId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                break;
        }






        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        //LocationRequest nesnesi konum güncellemeleri için kullanılır
        vKonumAl = new LocationRequest();
        //1000 milisaniye tekrarla -> setInterval
        //1000 milisaniyede bir tekrarla -> setFastestInterval
        vKonumAl.setInterval(1000);
        vKonumAl.setFastestInterval(1000);
        //arastirilacak !!!
        vKonumAl.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //com.google.android.gms.location.LocationListener  nesnesi import edildikten
        // sonra aktif oldu + add permisson check dedikten sonra yukaridai kodlar otomatik eklendi
        LocationServices.FusedLocationApi.requestLocationUpdates(vGoogleApiClient, vKonumAl, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    protected void onStop() {
        super.onStop();
        //activity durdurulduğunda anlık databaseden lokasyonu silme islemi
        String kullaniciId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("surucuKontrol");
        //burasi arastirilacak
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(kullaniciId);
    }
}
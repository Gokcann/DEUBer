package com.deubercomp.deuber;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import static android.R.attr.radius;

public class OtostopcuHaritaActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;

    //API Client icin degisken olusturuyoruz
    GoogleApiClient vGoogleApiClient;
    Location vSonKonum;
    LocationRequest vKonumAl;

    private Button vCikis,vOtostop;
    private LatLng lokasyonAl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otostopcu_harita);
        //burada google maps cagiriliyor (map activity de default geldi)
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //app üzerinde map calistiriliyor
        mapFragment.getMapAsync(this);

        vCikis = (Button) findViewById(R.id.cikis);
        vOtostop = (Button) findViewById(R.id.otostop);

        vCikis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(OtostopcuHaritaActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        //otostop cek butonuna basildiginda
        vOtostop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String kullaniciId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("otostopcuIstek");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(kullaniciId, new GeoLocation(vSonKonum.getLatitude(), vSonKonum.getLongitude() ));

                lokasyonAl = new LatLng(vSonKonum.getLatitude(), vSonKonum.getLongitude());
                mMap.addMarker(new MarkerOptions().position(lokasyonAl).title("Burada !"));
                vOtostop.setText("Seni almaya geliyor ! :) ");


            }
        });
    }
    private int yaricap = 1;
    private Boolean surucuBul = false;
    private String surucuBulId;
    private void enYakinSurucu() {
        DatabaseReference surucuLokasyon = FirebaseDatabase.getInstance().getReference().child("surucuKontrol");
        GeoFire geoFire = new GeoFire(surucuLokasyon);

        //Geo Queries -- 1 mil yaricap icerisindeki en yakin surucuyu getirir
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(lokasyonAl.latitude, lokasyonAl.longitude), yaricap);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!surucuBul){
                    surucuBul = true;
                    surucuBulId = key;
                    //surucu ile otostopcu arasinda veri alisverisi
                    DatabaseReference surucuref= FirebaseDatabase.getInstance().getReference().child("Kullanici").child("Surucu").child(surucuBulId);
                    String otostopcuId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap hHarita = new HashMap();
                    hHarita.put("otostopcuId", otostopcuId);
                    surucuref.updateChildren(hHarita);

                    surucuLokasyonGonder();
                    vOtostop.setText("Otostop çekiyor !");
                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!surucuBul)
                {
                    yaricap++;
                    enYakinSurucu();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
//burasi arastirilacak
    Marker vSurucuMarker;
    private  void surucuLokasyonGonder() {
        DatabaseReference surucuLokasyonRef = FirebaseDatabase.getInstance().getReference().child("surucuAktif").child(surucuBulId).child("l");
        surucuLokasyonRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    List<Object> harita = (List<Object>) dataSnapshot.getValue();
                    double lokasyonLat= 0;
                    double lokasyonLng= 0;
                    vOtostop.setText("Surucu bulundu !");
                    if(harita.get(0) != null) {
                        lokasyonLat = Double.parseDouble(harita.get(0).toString());
                    }
                    if(harita.get(1) != null){
                        lokasyonLng = Double.parseDouble(harita.get(1).toString());
                    }
                    LatLng surucuLatLng = new LatLng(lokasyonLat,lokasyonLng);
                    if(vSurucuMarker != null){
                        vSurucuMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(lokasyonAl.latitude);
                    loc1.setLongitude(lokasyonAl.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(surucuLatLng.latitude);
                    loc2.setLongitude(surucuLatLng.longitude);

                    float mesafe = loc1.distanceTo(loc2);

                    vOtostop.setText("Surucu bulundu" + String.valueOf(mesafe));
                    vSurucuMarker = mMap.addMarker(new MarkerOptions().position(surucuLatLng).title("Senin surucun !"));
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

        vSonKonum = location;

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));



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
    }
}
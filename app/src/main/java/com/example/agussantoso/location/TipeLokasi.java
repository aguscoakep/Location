package com.example.agussantoso.location;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class TipeLokasi extends AppCompatActivity implements DapatkanAlamatTask.onTaskSelesai{

    // btn location
    public Button btnLoc;
    public Button btnPilihLoc;

    // variable dg tipe Location
    private Location mLastLocation;

    // obj location callback
    private LocationCallback mLocationCallback;

    // var dg tipe FusedLocationProviderClient
    private FusedLocationProviderClient mFusedLocationClient;
    public TextView mLocationTextView;

    // Constant digunakan untuk mmengidentifikasi req permission
    // dari method onRequestPermissionResult()
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    // Constant untuk mendapatkan hasil
    private static final int REQUEST_PICK_PLACE = 1;

    private ImageView mAndroidImageView;
    private AnimatorSet mRotateAnim;

    private boolean mTrackingLocation;

    //    places class
    private PlaceDetectionClient mPlaceDetectionClient;
    private String mLastPlaceName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipe_lokasi);

        // untuk mendapat informasi lokasi device
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationTextView = (TextView) findViewById(R.id.textview_location);
        btnLoc = (Button) findViewById(R.id.button_location);
        btnPilihLoc = (Button) findViewById(R.id.button_pilih);
        mAndroidImageView = (ImageView) findViewById(R.id.imageview_android);

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
//                super.onLocationResult(locationResult);

                // jika tracking aktif, proses reverse geocode manjadi data alamat
                if(mTrackingLocation){
                    new DapatkanAlamatTask(TipeLokasi.this, TipeLokasi.this)
                            .execute(locationResult.getLastLocation());
                }
            }
        };

        // Animasi
        mRotateAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.rotate );
        mRotateAnim.setTarget(mAndroidImageView);

        btnLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getLocation();
//                mulaiTrackingLokasi();
                if (!mTrackingLocation){
                    mulaiTrackingLokasi();
                } else {
                    stopTrackingLokasi();
                }
            }
        });


        btnPilihLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//              click handler untuk mengeksekusi placepicker
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try{
                    startActivityForResult(builder.build(TipeLokasi.this), REQUEST_PICK_PLACE );
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e){
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            // mendapatkan object dr placepicker
            Place place = PlacePicker.getPlace(this,data );

            setTipeLokasi(place);
            mLocationTextView.setText(
                    getString(R.string.alamat_detail,
                            place.getName(),
                            place.getAddress(),
                            System.currentTimeMillis())
            );

        } else{
            mLocationTextView.setText("belum pilih lokasi bebs");

        }
    }

    //    private void getLocation(){
//        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION} ,
//                    REQUEST_LOCATION_PERMISSION );
//        } else {
////            Log.d("GETPERMISSION", "getLocation : permission granted");
//            mFusedLocationClient.getLastLocation().addOnSuccessListener(
//                    new OnSuccessListener<Location>() {
//                        @Override
//                        public void onSuccess(Location location) {
//                            if(location != null){
////                                get lang long
//
////                                mLastLocation = location;
////                                mLocationTextView. setText(
////                                        getString(R.string.location_text,
////                                                mLastLocation.getLatitude(),
////                                                mLastLocation.getLongitude(),
////                                                mLastLocation.getTime())
////                                );
//                                new DapatkanAlamatTask(MainActivity.this, MainActivity.this).execute(location);
//                            } else {
//                                mLocationTextView.setText("Lokasi tidak tersedia");
//                            }
//                        }
//                    }
//            );
//        }
//        mLocationTextView.setText(getString(R.string.alamat_text, "sedang mencari alamat",
//                System.currentTimeMillis()));
//
//    }




    private void mulaiTrackingLokasi(){
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(TipeLokasi.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION} ,
                    REQUEST_LOCATION_PERMISSION );
        } else {
//            Log.d("GETPERMISSION", "getLocation : permission granted");

            mFusedLocationClient.requestLocationUpdates(getLocationRequest(), mLocationCallback,null );


            mLocationTextView.setText(getString(R.string.alamat_detail, "sedang mencari nama tempat",
                    "sedang mencari alamat",
                    System.currentTimeMillis()));
            mTrackingLocation = true;
            btnLoc.setText("Stop Tracking Lokasi");
            mRotateAnim.start();
        }
    }




    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case REQUEST_LOCATION_PERMISSION:
                // jika permission diijinkan, getLocation()
                // jika tidak, tampilkan toast
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                    getLocation();
                    mulaiTrackingLokasi();
                } else {
                    Toast.makeText(this, "tidak dapat permission", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onTaskCompleted(final String result) throws SecurityException {

//        untuk mengecek mTrackingLocatin aktif atau tidak
        if(mTrackingLocation){

            Task<PlaceLikelihoodBufferResponse> placeResult=
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener(
                    new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            // mengecek apakah task berhasil atau tidak
                            if(task.isSuccessful()){
                                // ini diisi jika berhasil
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                                float maxLikelihood = 0;
                                Place currentPlace = null;

                                // cek tempat yg dihasilkan adalah tempat yg paling mendekati(likehood)
                                for (PlaceLikelihood placeLikelihood : likelyPlaces){
                                    if(maxLikelihood < placeLikelihood.getLikelihood()){
                                        maxLikelihood = placeLikelihood.getLikelihood();
                                        currentPlace = placeLikelihood.getPlace();
                                    }


                                }

                                // tampilan di UI
                                if(currentPlace !=null){
                                    mLocationTextView.setText(
                                            getString(R.string.alamat_detail,
                                                    currentPlace.getName(),
                                                    result,
                                                    System.currentTimeMillis())
                                    );
                                    // ubah icon berdasar tipe lokasi
                                    setTipeLokasi(currentPlace);
                                }

                                likelyPlaces.release();
                            } else {
                                // ini juga diisi
                                mLocationTextView.setText(
                                        getString(R.string.alamat_detail,
                                                "nama lokasi tidak ditemukan",
                                                result,
                                                System.currentTimeMillis())
                                );
                            }
                        }
                    }
            );

            // menampilkan semua tempat
            mPlaceDetectionClient.getCurrentPlace(null);

            // menampilkan alamat
            mLocationTextView.setText(getString(R.string.alamat_detail, result, System.currentTimeMillis()));
        }
    }

    private void stopTrackingLokasi(){
        if(mTrackingLocation){
            mTrackingLocation = false;

            // menghapus request update lokasi
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);

            btnLoc.setText("Mulai Tracking Lokasi");
            mLocationTextView.setText("Tracking sedang dihentikan");
            mRotateAnim.end();
        }

    }

    // digunakan untuk menentukan frekuensi req dan tingkat akurasi dari update lokasi
    private LocationRequest getLocationRequest(){
        LocationRequest locationRequest = new LocationRequest();

        // digunakan untuk seberapa sering update lokasi yg diinginkan
        locationRequest.setInterval(10000);

        // adalah seberapa sering update lokasi dari app lain yg meminta req lokasi
        locationRequest.setFastestInterval(5000);

        // parameter untuk memilih akurasi dan akurasi tinggi menggunakan GPS
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    // menampilkan lokasi
    private void setTipeLokasi(Place currentPlace){
        int drawableID = -1;
        for (Integer placeType : currentPlace.getPlaceTypes()){
            switch (placeType){
                case Place.TYPE_SCHOOL:
                    drawableID = R.drawable.toko;
                    break;
                case Place.TYPE_BAKERY:
                    drawableID = R.drawable.toko;
                    break;
                case Place.TYPE_CAFE:
                    drawableID = R.drawable.warkop;
                    break;
                case Place.TYPE_CITY_HALL:
                    drawableID = R.drawable.bioskop;
                    break;
            }
        }

        if(drawableID<0){
            drawableID = R.drawable.unknown;
        }
        mAndroidImageView.setImageResource(drawableID);
    }

}

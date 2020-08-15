package com.islamozcelik.stafftrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.islamozcelik.stafftrack.entities.Constants;
import com.islamozcelik.stafftrack.maps.UserLocation;
import com.islamozcelik.stafftrack.pages.StaffPage;
import com.islamozcelik.stafftrack.services.LocationService;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    TextView locationTextView;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        locationTextView = findViewById(R.id.locationTextView);

        Dexter.withContext(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        starLocationService();



                        registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                locationTextView.setText("Lokasyonun şu: "+intent.getDoubleExtra("latitude",0f)+" - "+intent.getDoubleExtra("longitude",0f) );
                                Calendar calendar = Calendar.getInstance();
                                SimpleDateFormat mdformat = new SimpleDateFormat("dd/MM/yyyy");
                                final String datetime = mdformat.format(calendar.getTime());


                                Map<String,Object> data = new HashMap<>();
                                data.put("currentLocation",new GeoPoint(intent.getDoubleExtra("latitude",0f),intent.getDoubleExtra("longitude",0f)));
                                final double lat = intent.getDoubleExtra("latitude",0f);
                                final double lon = intent.getDoubleExtra("longitude",0f);
                                firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).update(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //Toast.makeText(MainActivity.this,"eklenme başarılı",Toast.LENGTH_LONG).show();
                                        sendPostRequest(lat,lon,Constants.URL_TO_ADD);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }
                        }, new IntentFilter(Constants.RECEIVERS_TRIGGER));


                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(MainActivity.this,"Lütfen Uygulamaya izin Veriniz",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();

    }
    private boolean isLocationServiceRunning(){
        ActivityManager activityManager  = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        if (activityManager != null){
            for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)){
                if (LocationService.class.getName().equals(serviceInfo.service.getClassName())){
                    if (serviceInfo.foreground){
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private void starLocationService(){
        if (!isLocationServiceRunning()){
            Intent intent = new Intent(getApplicationContext(),LocationService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);

        }
    }
    private void stopLocationService(){
        if (isLocationServiceRunning()){
            Intent intent = new Intent(getApplicationContext(),LocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
        }
    }

    public void showstaff(View view) {
        startActivity(new Intent(this, StaffPage.class));


    }
    public void adduser(View view) {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.add_user_dialog);


        final Button button = dialog.findViewById(R.id.add_user_btn);
        final EditText editText = dialog.findViewById(R.id.add_user_edit);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText()!=null)
                    findUser(editText.getText().toString());
                    dialog.dismiss();
            }
        });

        dialog.show();

    }

    public void sendPostRequest(final double latitude, final double longitude,String url){
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,url , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Response From Server",response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Hata var ",error.getLocalizedMessage().toString());
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> datas = new HashMap<>();
                datas.put("userid",firebaseAuth.getCurrentUser().getUid());
                datas.put("latitude",String.valueOf(latitude));
                datas.put("longitude",String.valueOf(longitude));
                return datas;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String,String> params = new HashMap<>();
                params.put("content-type","application/x-www-form-urlencoded");
                //return super.getHeaders();
                return params;
            }
        };
        queue.getCache().clear();
        queue.add(stringRequest);
    }
    public void findUser(final String email){
        CollectionReference refforuser = firebaseFirestore.collection("Users");


        refforuser.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null){
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
                        if(snapshot.getData().get("email").equals(email)){
                            System.out.println("aranan kullanıcı: "+snapshot.getData().get("uid"));
                            addtoUser((String) snapshot.getData().get("uid"));

                            break;
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
    public void addtoUser(final String uid){
        CollectionReference colref = firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).collection("following");

        Map<String,String> data = new HashMap<>();
        data.put("userid",uid);
        colref.add(data).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(MainActivity.this,"kullanıcı eklenmiştir",Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
            }
        });
    }
}

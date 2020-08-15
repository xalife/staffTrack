package com.islamozcelik.stafftrack.maps;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.islamozcelik.stafftrack.MainActivity;
import com.islamozcelik.stafftrack.R;
import com.islamozcelik.stafftrack.adapter.DaysAdapter;
import com.islamozcelik.stafftrack.entities.Constants;
import com.islamozcelik.stafftrack.model.DayModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserLocation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore firebaseFirestore;
    private GeoPoint usersLocation;
    private String userid;
    private ArrayList<DayModel> gunler;
    private RecyclerView recyclerView;
    private ArrayList<LatLng> latLngs;

    DocumentReference documentReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_location);
        firebaseFirestore = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.userslocrecyclerview);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        userid = getIntent().getExtras().getString("userid");
        gunler = new ArrayList<>();
        latLngs = new ArrayList<>();

        followUser(userid);
        getData(userid, Constants.URL_FOR_DATAS);
    }
    public DocumentReference followUser(String userid){

        documentReference = firebaseFirestore.collection("Users").document(userid);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null){
                    if (value.get("currentLocation") != null){
                        usersLocation = (GeoPoint) value.get("currentLocation");
                        mMap.clear();

                        mMap.addMarker(new MarkerOptions().position(new LatLng(usersLocation.getLatitude(),usersLocation.getLongitude()))
                                .title("Marker in Where You are"))
                                .setIcon(bitmapDescriptor(getApplicationContext(),R.drawable.ic_dot_on_map));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(usersLocation.getLatitude(),usersLocation.getLongitude()),15f));
                    }
                }
            }
        });
        return documentReference;
    }
    private BitmapDescriptor bitmapDescriptor(Context context,int vec){
        Drawable vectordrawable = ContextCompat.getDrawable(context,vec);
        vectordrawable.setBounds(0,0,vectordrawable.getIntrinsicWidth(),vectordrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectordrawable.getIntrinsicWidth(),vectordrawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectordrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();
        LatLng sydney = new LatLng(35,35);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Where You are"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


    }

    private void getData(final String userid, String url){


        RequestQueue queue = Volley.newRequestQueue(UserLocation.this);
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                System.out.println(response);

                try {
                    JSONArray j= new JSONArray(response);
                    for (int i =0;i<j.length();i++) {

                        JSONObject object = j.getJSONObject(i);
                        DayModel dayModel = new DayModel();
                        dayModel.setDate(object.getString("day"));
                        gunler.add(dayModel);
                        System.out.println("gelen günler: " + object.getString("day"));


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                latLngs.add(new LatLng(38,38));
                latLngs.add(new LatLng(38,30));
                recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(),3, GridLayoutManager.HORIZONTAL,false));
                recyclerView.setAdapter(new DaysAdapter(getApplicationContext(),gunler,latLngs,mMap));

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> datas = new HashMap<>();
                datas.put("userid",userid);
                return datas;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String,String> params = new HashMap<>();
                params.put("content-type","application/x-www-form-urlencoded");

                return params;
            }
        };



        queue.getCache().clear();
        queue.add(request);



    }

}

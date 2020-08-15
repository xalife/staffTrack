package com.islamozcelik.stafftrack.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonObject;
import com.islamozcelik.stafftrack.MainActivity;
import com.islamozcelik.stafftrack.R;
import com.islamozcelik.stafftrack.entities.Constants;
import com.islamozcelik.stafftrack.maps.UserLocation;
import com.islamozcelik.stafftrack.model.DayModel;
import com.islamozcelik.stafftrack.model.LocationModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DaysAdapter extends RecyclerView.Adapter<DaysAdapter.DayViewHolder> {

    ArrayList<DayModel> daysList;
    GoogleMap googleMap;
    ArrayList<LatLng> latLngs;
    Polyline polyline;
    Context context;


    public DaysAdapter(Context context, ArrayList<DayModel> daysList,ArrayList<LatLng> latLngs,GoogleMap googleMap){
        this.context = context;
        this.googleMap = googleMap;
        this.latLngs = latLngs;
        this.daysList = daysList;
        polyline = null;
    }

    @NonNull
    @Override
    public DaysAdapter.DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_row_item,parent,false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DaysAdapter.DayViewHolder holder, final int position) {
        holder.textView.setText(daysList.get(position).getDate());
        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(daysList.get(position).getDate());
                getLocationsforLine(daysList.get(position).getDate(),Constants.URL_FOR_DATE);

            }
        });

    }

    @Override
    public int getItemCount() {
        return daysList.size();
    }

    class DayViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        ConstraintLayout constraintLayout;
        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.day_edit_two);
            constraintLayout = itemView.findViewById(R.id.singledayitem);
        }
    }
    private void drawLine(ArrayList<LatLng> latLngs){
        if (polyline != null) polyline.remove();

        PolylineOptions polygonOptions = new PolylineOptions()
                .addAll(latLngs)
                .clickable(true);
        polyline = googleMap.addPolyline(polygonOptions);

    }
    private void getLocationsforLine(final String date,String url){

        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST,url , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //System.out.println("serverdan gelen günler"+response);

                try {
                    JSONArray j= new JSONArray(response);
                    for (int i =0;i<j.length();i++) {

                        JSONObject object = j.getJSONObject(i);
                        LocationModel locationModel = new LocationModel();
                        locationModel.setLatitude(object.getDouble("latitude"));
                        locationModel.setLongitude(object.getDouble("longitude"));
                        locationModel.setTime(object.getString("time"));
                        latLngs.add(new LatLng(locationModel.getLatitude(),locationModel.getLongitude()));



                    }
                    drawLine(latLngs);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                datas.put("date",date);
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




}

package com.islamozcelik.stafftrack.pages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.islamozcelik.stafftrack.R;
import com.islamozcelik.stafftrack.maps.UserLocation;
import com.islamozcelik.stafftrack.model.Staff;

public class StaffPage extends AppCompatActivity {
    RecyclerView recyclerView;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    FirestoreRecyclerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_staff_page);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerviewStaffPage);

        Query query = firebaseFirestore.collection("Users").document(firebaseAuth.getCurrentUser().getUid()).collection("following");
        FirestoreRecyclerOptions<Staff> options = new FirestoreRecyclerOptions.Builder<Staff>()
                .setQuery(query,Staff.class)
                .build();

         adapter = new FirestoreRecyclerAdapter<Staff,StaffViewHolder>(options){

            @NonNull
            @Override
            public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.staff_item,parent,false);

                System.out.println("recylerview oncreateviewholder çalıştı");
                return new StaffViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull StaffViewHolder holder, int position, @NonNull final Staff model) {
                holder.textView.setText(model.getUserid());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(StaffPage.this, UserLocation.class).putExtra("userid",model.getUserid()));
                    }
                });

                System.out.println("recylerview onbind çalıştı");
            }
        };

         recyclerView.setLayoutManager(new LinearLayoutManager(this));
         recyclerView.setAdapter(adapter);
    }

    private class StaffViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            System.out.println("viewholder constructor çalıştı");
            textView = itemView.findViewById(R.id.staffitemtextview);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}

package com.jimmypiedrahita.chat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
public class HomeActivity extends AppCompatActivity {
    private FirebaseUser user;
    private RecyclerView recyclerChats;
    private ControllerAdapterChat adapterChat;
    private final List<DataSnapshot> listChat = new ArrayList<>();
    private ConstraintLayout panel_add;
    private EditText txt_add_email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.quintoColor));
        TextView txt_name_user = findViewById(R.id.txt_name_user);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        txt_add_email = findViewById(R.id.txt_add);
        user = auth.getCurrentUser();
        recyclerChats = findViewById(R.id.recyclerChats);
        panel_add = findViewById(R.id.panel_add);
        if(user == null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
        txt_name_user.setText(user.getEmail());
        showChats();
    }
    public void showChats(){
        getChats();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(HomeActivity.this);
        recyclerChats.setLayoutManager(linearLayoutManager);
        recyclerChats.setAdapter(adapterChat);
    }
    public void getChats(){
        adapterChat = new ControllerAdapterChat(HomeActivity.this, listChat);
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Chats");
        database.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String user1 = snapshot.child("user1").getValue(String.class);
                String user2 = snapshot.child("user2").getValue(String.class);
                String userMail = user.getEmail();
                assert userMail != null;
                if(userMail.equalsIgnoreCase(user1) || userMail.equalsIgnoreCase(user2)){
                    listChat.add(snapshot);
                    adapterChat.notifyDataSetChanged();
                    recyclerChats.smoothScrollToPosition(listChat.size() - 1);
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                for (java.util.Iterator<DataSnapshot> iterator = listChat.iterator(); iterator.hasNext();) {
                    DataSnapshot chat = iterator.next();
                    if (Objects.equals(chat.getKey(), snapshot.getKey())) {
                        iterator.remove();
                        adapterChat.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    public void closeSession(View view){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }
    public void showAddPanel(View view){
        panel_add.setVisibility(View.VISIBLE);
    }
    public void closePanel(View view){
        panel_add.setVisibility(View.GONE);
    }
    public void addEmailChat(View view) {
        String email_add = txt_add_email.getText().toString().trim().toLowerCase();
        if (email_add.isEmpty()) {
            Toast.makeText(this, "Enter an email address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Objects.equals(user.getEmail(), email_add)) {
            Toast.makeText(this, "Cannot add to itself", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean emailFound = false;
                for (DataSnapshot user : snapshot.getChildren()) {
                    String userEmail = user.child("email").getValue(String.class);
                    if (userEmail != null && userEmail.equalsIgnoreCase(email_add)) {
                        emailFound = true;
                        break;
                    }
                }
                if (emailFound) {
                    DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("Chats");
                    Query query1 = chatsRef.orderByChild("user1").equalTo(user.getEmail());
                    Query query2 = chatsRef.orderByChild("user2").equalTo(user.getEmail());
                    Task<DataSnapshot> task1 = query1.get();
                    Task<DataSnapshot> task2 = query2.get();
                    Tasks.whenAllSuccess(task1, task2)
                            .addOnSuccessListener(results -> {
                                boolean chatExists = false;
                                for (Object result : results) {
                                    DataSnapshot snapshot1 = (DataSnapshot) result;
                                    for (DataSnapshot chatSnapshot : snapshot1.getChildren()) {
                                        String user1 = chatSnapshot.child("user1").getValue(String.class);
                                        String user2 = chatSnapshot.child("user2").getValue(String.class);
                                        if((user1 != null && user2 != null) && ((user1.equals(user.getEmail()) && user2.equals(email_add)) || user1.equals(email_add) && user2.equals(user.getEmail()))){
                                            chatExists = true;
                                            break;
                                        }
                                    }
                                }
                                if (chatExists) {
                                    Toast.makeText(getApplicationContext(), "You have already added this user", Toast.LENGTH_SHORT).show();
                                } else {
                                    DatabaseReference chatReference = chatsRef.push();
                                    Chat chat = new Chat(user.getEmail(), email_add, chatReference.getKey());
                                    chatReference.setValue(chat);
                                    panel_add.setVisibility(View.GONE);
                                }
                            })
                            .addOnFailureListener(Throwable::printStackTrace);
                } else {
                    Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Error retrieving user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
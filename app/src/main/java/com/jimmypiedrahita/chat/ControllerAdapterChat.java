package com.jimmypiedrahita.chat;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
public class ControllerAdapterChat extends RecyclerView.Adapter<MyViewHolderChat>{
    private final List<DataSnapshot> dataList;
    private final List<User> userList;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    public ControllerAdapterChat(Context context, List<DataSnapshot> dataList) {
        this.dataList = dataList;
        this.userList = new ArrayList<>();
        loadUserList();
    }
    private void loadUserList() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String user_name = userSnapshot.child("name").getValue(String.class);
                    String user_email = userSnapshot.child("email").getValue(String.class);
                    String user_password = userSnapshot.child("password").getValue(String.class);
                    String user_picture = userSnapshot.child("urlPicture").getValue(String.class);
                    userList.add(new User(user_picture, user_name, user_email, user_password));
                }
                notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println(error.getMessage());
            }
        });
    }
    @NonNull
    @Override
    public MyViewHolderChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View viewTransmitter = layoutInflater.inflate(R.layout.view_chat_single, parent, false);
        return new MyViewHolderChat(viewTransmitter);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolderChat holder, int position) {
        String user1 = dataList.get(position).child("user1").getValue(String.class);
        String user2 = dataList.get(position).child("user2").getValue(String.class);
        String chat_id = dataList.get(position).child("id").getValue(String.class);
        String userLocal = Objects.requireNonNull(auth.getCurrentUser()).getEmail();
        assert userLocal != null;
        User user = getUserByEmail(userLocal,user1, user2);
        if(user != null){
            Glide.with(holder.itemView.getContext())
                    .load(user.getUrlPicture())
                    .apply(new RequestOptions())
                    .centerCrop()
                    .into(holder.image_profile);
            holder.name_chat.setText(user.getName());
        }
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, MainActivity.class);
            assert user != null;
            intent.putExtra("CHAT_ID", chat_id);
            intent.putExtra("CHAT_NAME", user.getName());
            context.startActivity(intent);
        });
    }
    @Override
    public int getItemCount() {
        if(dataList.isEmpty())return 0;
        return dataList.size();
    }
    private User getUserByEmail(String e_local, String e_user1, String e_user2) {
        String e_chat = null;
        if(e_local.equals(e_user1)){
            e_chat = e_user2;
        }
        if(e_local.equals(e_user2)){
            e_chat = e_user1;
        }
        for (User user : userList) {
            assert e_chat != null;
            if(e_chat.equals(user.getEmail())){
                return user;
            }
        }
        return null;
    }
}
class MyViewHolderChat extends RecyclerView.ViewHolder{
    ImageView image_profile;
    TextView name_chat;
    public MyViewHolderChat(@NonNull View itemView) {
        super(itemView);
        image_profile = itemView.findViewById(R.id.imageProfile);
        name_chat = itemView.findViewById(R.id.txt_name_chat);
    }
}
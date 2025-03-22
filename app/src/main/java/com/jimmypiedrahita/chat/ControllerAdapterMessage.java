package com.jimmypiedrahita.chat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import java.util.List;
import java.util.Objects;

public class ControllerAdapterMessage extends RecyclerView.Adapter<MyViewHolder>{
    private final List<DataSnapshot> dataList;
    private static final int VIEW_TYPE_TRANSMITTER = 1;
    private static final int VIEW_TYPE_RECEIVER = 2;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public ControllerAdapterMessage(List<DataSnapshot> dataList) {
        this.dataList = dataList;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_TRANSMITTER) {
            View viewTransmitter = layoutInflater.inflate(R.layout.view_chat_transmitter, parent, false);
            return new MyViewHolder(viewTransmitter);
        } else {
            View viewReceiver = layoutInflater.inflate(R.layout.view_chat_receiver, parent, false);
            return new MyViewHolder(viewReceiver);
        }
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String urlImage = dataList.get(position).child("urlMessage").getValue(String.class);
        Glide.with(holder.itemView.getContext())
                .load(urlImage)
                .apply(new RequestOptions())
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.imageView);
    }
    @Override
    public int getItemViewType(int position) {
        String sender = dataList.get(position).child("senderMessage").getValue(String.class);
        assert sender != null;
        boolean isSender = sender.equals(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail());
        return isSender ? VIEW_TYPE_TRANSMITTER : VIEW_TYPE_RECEIVER;
    }
    @Override
    public int getItemCount() {
        if(dataList.isEmpty())return 0;
        return dataList.size();
    }
}
class MyViewHolder extends RecyclerView.ViewHolder{
    ImageView imageView;
    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imageChat);
    }
}
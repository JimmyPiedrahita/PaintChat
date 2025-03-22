package com.jimmypiedrahita.chat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import yuku.ambilwarna.AmbilWarnaDialog;
public class MainActivity extends AppCompatActivity {
    DrawCanvas drawCanvas;
    final List<DataSnapshot> listMessage = new ArrayList<>();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    RecyclerView recyclerView;
    ControllerAdapterMessage adapterMessage;
    boolean isPanelVisible = true;
    boolean isVisiblePanelPencil = false;
    boolean isVisiblePanelBackground = false;
    ConstraintLayout canvasDraw;
    public int defaultColor, defaultBackground;
    ConstraintLayout toolsPanel;
    TextView lbl_name_chat;
    SeekBar seekBar;
    ConstraintLayout sizeView, panelPencil, panelBackground;
    Button btn_eraser;
    String chat_id;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.quintoColor));
        defaultColor = ContextCompat.getColor(MainActivity.this,R.color.black);
        defaultBackground = ContextCompat.getColor(MainActivity.this,R.color.white);
        recyclerView = findViewById(R.id.recyclerMessages);
        canvasDraw = findViewById(R.id.canvasDrawPanel);
        drawCanvas = findViewById(R.id.canvas);
        toolsPanel = findViewById(R.id.toolsPanel);
        seekBar = findViewById(R.id.seekbar_stroke);
        sizeView = findViewById(R.id.size_view);
        panelPencil = findViewById(R.id.panelPencil);
        panelBackground = findViewById(R.id.panelBackground);
        sizeView.setMinHeight(50);
        sizeView.setMinWidth(50);
        btn_eraser = findViewById(R.id.btn_eraser);
        lbl_name_chat = findViewById(R.id.lbl_name_chat);
        mAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        if (intent != null) {
            chat_id = intent.getStringExtra("CHAT_ID");
            String chat_name  = intent.getStringExtra("CHAT_NAME");
            if (chat_id != null) {
                lbl_name_chat.setText(chat_name);
            }
        }
        showMessages();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changeStrokePen(progress);
                System.out.println(progress);
                int size = progress+10;
                sizeView.setMaxWidth(size);
                sizeView.setMaxHeight(size);
                sizeView.setMinHeight(size);
                sizeView.setMinWidth(size);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    public void sendMessage(View view){
        if(isPanelVisible){
            Uri uriMessage = drawCanvas.captureDraw(this);
            if(uriMessage != null){
                saveMessage(Objects.requireNonNull(mAuth.getCurrentUser()),uriMessage);
            }else{
                Toast.makeText(this, "Drawing could not be obtained", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void saveMessage(FirebaseUser user, Uri uriMessage){
        DatabaseReference messages = FirebaseDatabase.getInstance().getReference("Messages");
        messages.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long quantityMessage = snapshot.getChildrenCount();
                StorageReference fileRef = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(uriMessage));
                fileRef.putFile(uriMessage).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uriFile -> {
                    Message message = new Message(uriFile.toString(),user.getEmail(), chat_id);
                    messages.child((quantityMessage+1)+"_message").setValue(message);
                    drawCanvas.clearCanvas();
                }).addOnFailureListener(Throwable::fillInStackTrace)).addOnFailureListener(Throwable::fillInStackTrace);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    public String getFileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }
    public void showMessages(){
        getMessages();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapterMessage);
    }
    public void getMessages(){
        adapterMessage = new ControllerAdapterMessage(listMessage);
        DatabaseReference messages = FirebaseDatabase.getInstance().getReference("Messages");
        messages.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String idChat = snapshot.child("idChat").getValue(String.class);
                assert idChat != null;
                if(idChat.equals(chat_id)){
                    listMessage.add(snapshot);
                    adapterMessage.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(listMessage.size() - 1);
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                for (java.util.Iterator<DataSnapshot> iterator = listMessage.iterator(); iterator.hasNext();) {
                    DataSnapshot chat = iterator.next();
                    if (Objects.equals(chat.getKey(), snapshot.getKey())) {
                        iterator.remove();
                        adapterMessage.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    public void hidePanel(View view) {
        if(isVisiblePanelBackground && isVisiblePanelPencil){
            panelBackground.setVisibility(View.GONE);
            panelPencil.setVisibility(View.GONE);
            isVisiblePanelPencil = false;
            isVisiblePanelBackground = false;
        }else{
            if(isPanelVisible){
                canvasDraw.setMaxHeight(0);
                isPanelVisible = false;
            }
        }
    }
    public void showPanel(View view){
        if(isPanelVisible){
            panelBackground.setVisibility(View.VISIBLE);
            panelPencil.setVisibility(View.VISIBLE);
            isVisiblePanelBackground = true;
            isVisiblePanelPencil = true;
        }else{
            int maxHeightInDp = 300;
            float scale = getResources().getDisplayMetrics().density;
            int maxHeightInPixels = (int) (maxHeightInDp * scale + 0.5f);
            canvasDraw.setMaxHeight(maxHeightInPixels);
            isPanelVisible = true;
        }
    }
    public void changeColorPen(View view){
        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {}
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                    defaultColor = color;
                    drawCanvas.changeColorPen(Color.red(color),Color.green(color),Color.blue(color));
            }
        });
        ambilWarnaDialog.show();
    }
    public void changeBackground(View view){
        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(this, defaultBackground, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {}
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                defaultBackground = color;
                drawCanvas.setBackgroundColor(color);
            }
        });
        ambilWarnaDialog.show();
    }
    public void changeStrokePen(int size){
        drawCanvas.changeSizePen(size);
    }
    public void eraserDraw(View view){
        if(drawCanvas.isEraserMode()){
            drawCanvas.setEraserMode(false);
            drawCanvas.changeColorPen(Color.red(defaultColor),Color.green(defaultColor),Color.blue(defaultColor));
            btn_eraser.setBackgroundColor(ContextCompat.getColor(this, R.color.quintoColor));
        }else{
            drawCanvas.setEraserMode(true);
            btn_eraser.setBackgroundColor(ContextCompat.getColor(this, R.color.tertiaryColor));
            drawCanvas.colorEraser = defaultBackground;
        }
    }
}
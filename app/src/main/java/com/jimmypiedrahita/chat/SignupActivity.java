package com.jimmypiedrahita.chat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import de.hdodenhof.circleimageview.CircleImageView;
public class SignupActivity extends AppCompatActivity {
    private EditText txt_name;
    private EditText txt_email;
    private EditText txt_password;
    private EditText txt_re_password;
    private CircleImageView picker_photo;
    private Uri imageURI;
    private StorageReference storageReference;
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseAuth mAuth;
    private String profileUrl;
private final ActivityResultLauncher<String> launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri ->{
    if (uri != null){
        imageURI = uri;
        picker_photo.setImageURI(imageURI);
    }
});
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        txt_name = findViewById(R.id.txt_name);
        txt_email = findViewById(R.id.txt_email);
        txt_password = findViewById(R.id.txt_password);
        txt_re_password = findViewById(R.id.txt_re_password);
        picker_photo = findViewById(R.id.pickerPhoto);
        mAuth = FirebaseAuth.getInstance();
        picker_photo.setOnClickListener(v -> launcher.launch("image/*"));
    }
    public void signUp(View view) {
        String name = txt_name.getText().toString().trim();
        String email = txt_email.getText().toString().trim().toLowerCase();
        String password = txt_password.getText().toString().trim();
        String re_password = txt_re_password.getText().toString().trim();
        if (!name.isEmpty() || !email.isEmpty() || !password.isEmpty() || !re_password.isEmpty()) {
            if (password.equals(re_password)) {
                if(password.getBytes().length >= 6) {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Account successfully created", Toast.LENGTH_SHORT).show();
                            if (imageURI != null) {
                                storageReference = storage.getReference("UserPictureProfile");
                                StorageReference fileReference = storageReference.child(email + "." + getFileExtension(imageURI));
                                fileReference.putFile(imageURI).addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> createUser(uri.toString(), name, email, password)).addOnFailureListener(Throwable::fillInStackTrace)).addOnFailureListener(Throwable::printStackTrace);
                            } else {
                                profileUrl = "https://firebasestorage.googleapis.com/v0/b/chat-b8110.appspot.com/o/UserPictureProfile%2Fdefault.png?alt=media&token=f767c68d-1817-42c9-a20b-e5e21868bfbc";
                                createUser(profileUrl, name, email, password);
                            }
                        } else {
                            Toast.makeText(SignupActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Password does not match", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Please fill out all the required fields", Toast.LENGTH_LONG).show();
        }
    }
    public void createUser(String url, String name, String email, String password){
        DatabaseReference root = FirebaseDatabase.getInstance().getReference("Users");
        root.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = new User(url, name, email, password);
                root.child(email.replace(".","")).setValue(user);
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri));
    }
    public void toLogin(View view){
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
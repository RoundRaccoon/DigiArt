package a10m3.cruciada;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class PostActivity extends AppCompatActivity {

    //private static final int GALLERY_REQUEST=2;
    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;
    private Uri uri=null;
    private ImageButton imageButton;//compressedImage,actualImage;
    private EditText editName,editDesc;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseLucrari;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;
    private Button GataButton;
    ProgressDialog mPostDialog;
    boolean photo=false;
    private String comp_key;
    private DatabaseReference mCurrentComp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        editName = (EditText) findViewById(R.id.editName);
        editDesc = (EditText) findViewById(R.id.editDesc);
        imageButton = (ImageButton) findViewById(R.id.imageButton);
        GataButton = (Button) findViewById(R.id.gata);

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Comps");
        mAuth = FirebaseAuth.getInstance();
        mCurrentComp = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("CurrentComp");
        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        mPostDialog = new ProgressDialog(this);


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //LOGIC FOR PROFILE PICTURE
                profilePicSelection();
            }
        });

        GataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //LOGIC FOR PROFILE PICTURE
                submitButtonClicked();
            }
        });
    }
     /*public void imageButtonClicked(View view)
     {
     Intent galleryintent= new Intent(Intent.ACTION_GET_CONTENT);
     galleryintent.setType("Image/*");
     startActivityForResult(galleryintent,GALLERY_REQUEST);
     }*/

    @Override
    public void onBackPressed() {

        Intent a = new Intent(PostActivity.this, CruciadaHome.class);
        startActivity(a);
        super.onBackPressed();
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*if( requestCode == REQUEST_CAMERA && resultCode == RESULT_OK)
        {
            uri = data.getData();
            imageButton = (ImageButton) findViewById(R.id.imageButton);
            Picasso
                    .with(getApplicationContext())
                    .load(uri)
                    .fit()
                    .centerInside()
                    .into(imageButton);
            photo = true;
        }else*/ if(requestCode == SELECT_FILE && resultCode == RESULT_OK)
        {
            uri = data.getData();
            imageButton = (ImageButton) findViewById(R.id.imageButton);
            Picasso
                    .with(getApplicationContext())
                    .load(uri)
                    .fit()
                    .centerInside()
                    .into(imageButton);
            photo = true;
        }
    }

    public void submitButtonClicked()
    {
        final String titlevValue = editName.getText().toString().trim();
        final String titleDesc = editDesc.getText().toString().trim();

        if(!TextUtils.isEmpty(titlevValue) && photo)
        {
            mPostDialog.setTitle("Se încarcă poza");
            mPostDialog.setMessage("Vă rugăm așteptați");
            mPostDialog.show();
            mPostDialog.setCancelable(false);
            mPostDialog.setCanceledOnTouchOutside(false);

            StorageReference filePath = storageReference.child("PostImage").child(uri.getLastPathSegment());
            //StorageReference filePath = storageReference.child("PostImage").child(String.valueOf(uri));
            filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    final Uri downloadurl = taskSnapshot.getDownloadUrl();
                    Toast.makeText(PostActivity.this,"Încărcare finalizată",Toast.LENGTH_LONG).show();
                    mCurrentComp.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            comp_key = dataSnapshot.getValue().toString().trim();
                            final DatabaseReference newPost = databaseReference.child(comp_key).child("Lucrari").push();

                            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    newPost.child("like").setValue(0);
                                    newPost.child("title").setValue(titlevValue);
                                    newPost.child("desc").setValue(titleDesc);
                                    newPost.child("image").setValue(downloadurl.toString());
                                    newPost.child("uid").setValue(mCurrentUser.getUid());
                                    newPost.child("username").setValue(dataSnapshot.child("Name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Intent moveToHome = new Intent(PostActivity.this, CruciadaHome.class);
                                                startActivity(moveToHome);
                                            }
                                        }
                                    });
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });
        }
        else {
            if(photo)Toast.makeText(PostActivity.this, "Postarea trebuie să aibe un titlu", Toast.LENGTH_LONG).show();
            else Toast.makeText(PostActivity.this, "Selectați o imagine", Toast.LENGTH_LONG).show();
        }
    }
    //USER IMAGEVIEW ONCLICK LISTENER
    //Functia pentru extragerea pozei
    private void profilePicSelection() {

        final CharSequence[] items = {/*"Fă o poză",*/ "Alege din Galerie", "Anulează"};
        AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
        builder.setTitle("Adaugă o fotografie");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                /*if (items[item].equals("Fă o poză")) {
                    cameraIntent();
                } else*/ if (items[item].equals("Alege din Galerie")) {
                    galleryIntent();
                } else if (items[item].equals("Anulează")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    private void cameraIntent() {

        Log.d("gola", "entered here");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }
    private void galleryIntent() {

        Log.d("gola", "entered here");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_FILE);
    }
}
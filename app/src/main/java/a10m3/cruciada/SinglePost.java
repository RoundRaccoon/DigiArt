package a10m3.cruciada;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher;

public class SinglePost extends AppCompatActivity {

    DatabaseReference mDatabase;
    DatabaseReference mCurrentComp;
    ImageView zoomImage;
    String key;
    ActionBar actionBar;
    PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post);

        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));
        //actionBar.hide();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Comps");
        mCurrentComp = FirebaseDatabase.getInstance().getReference().child("Users").child("CurrentComp");
        key = getIntent().getExtras().getString("id");
        zoomImage = (ImageView) findViewById(R.id.zoomImage);

        mCurrentComp.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String code = dataSnapshot.getValue().toString().trim();

                mDatabase.child(code).child("Lucrari").child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String postImage = dataSnapshot.child(code).child("Lucrari").child(key).child("image").getValue(String.class).trim();
                        Picasso
                                .with(getApplicationContext())
                                .load(postImage)
                                //.fit()
                                //.centerInside()
                                .into(zoomImage);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mAttacher = new PhotoViewAttacher(zoomImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(SinglePost.this,CruciadaHome.class));
        super.onBackPressed();
    }

}

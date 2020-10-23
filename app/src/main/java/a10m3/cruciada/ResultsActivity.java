package a10m3.cruciada;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ResultsActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private TextView numeComp,loc1,loc2,loc3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        numeComp = (TextView) findViewById(R.id.competitie);
        loc1 = (TextView) findViewById(R.id.winner1);
        loc2 = (TextView) findViewById(R.id.winner2);
        loc3 = (TextView) findViewById(R.id.winner3);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Winners");

    }

    @Override
    protected void onStart() {
        super.onStart();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                numeComp.setText(dataSnapshot.child("Competiție").getValue().toString().trim());
                loc1.setText(dataSnapshot.child("Locul 1").getValue().toString().trim());
                loc2.setText(dataSnapshot.child("Locul 2").getValue().toString().trim());
                loc3.setText(dataSnapshot.child("Locul 3").getValue().toString().trim());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}

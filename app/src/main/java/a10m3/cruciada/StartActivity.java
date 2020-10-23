package a10m3.cruciada;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class StartActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseComps;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseCurrentUser;
    private DatabaseReference mDatabaseCod;
    private FirebaseUser mCurrentUser;

    private RecyclerView mCompList;
    private Query mQueryCrt;

    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabaseCod = FirebaseDatabase.getInstance().getReference().child("Comps");
        mDatabaseComps = mDatabase.child("Comps");
        mDatabaseUsers = mDatabase.child("Users").child(mCurrentUser.getUid());
        mDatabaseCurrentUser = FirebaseDatabase.getInstance().getReference().child("Comps");
        mQueryCrt = mDatabaseCurrentUser.orderByChild(mAuth.getCurrentUser().getUid()).equalTo("Acces");

        mDatabaseUsers.child("Name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userName = dataSnapshot.getValue().toString().trim();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mCompList = (RecyclerView) findViewById(R.id.comp_list);
        mCompList.setHasFixedSize(true);
        mCompList.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(StartActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_create,null);

                final EditText mName = (EditText) mView.findViewById(R.id.CompName);
                final EditText mDesc = (EditText) mView.findViewById(R.id.CompDescription);
                TextView createBtn = (TextView) mView.findViewById(R.id.CreateCompButton);

                createBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String name = mName.getText().toString().trim();
                        final String desc = mDesc.getText().toString().trim();

                        if(!TextUtils.isEmpty(name))
                        {
                            Toast.makeText(StartActivity.this,"Se creeaza concursul",Toast.LENGTH_SHORT).show();
                            final DatabaseReference newComp = mDatabaseComps.push();
                            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(final DataSnapshot dataSnapshot) {
                                    newComp.child("Name").setValue(name);
                                    newComp.child("Desc").setValue(desc);
                                    newComp.child(mAuth.getCurrentUser().getUid()).setValue("Acces");
                                    newComp.child("Admin").child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getUid());
                                    mDatabaseUsers.child("CurrentComp").setValue(newComp.getKey());
                                    newComp.child("Creator").setValue(dataSnapshot.child("Name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                startActivity(new Intent(StartActivity.this,CruciadaHome.class));
                                            }

                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                        else
                        {
                            Toast.makeText(StartActivity.this,"Completeaza toate spatiile obligatorii",Toast.LENGTH_SHORT).show();
                        }

                    }
                });


                mBuilder.setView(mView);
                AlertDialog dialog = mBuilder. create();
                dialog.show();
            }
        });



    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseRecyclerAdapter<Comp,CompViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comp, CompViewHolder>(
                Comp.class,
                R.layout.comp_row,
                CompViewHolder.class,
                mQueryCrt
        ) {
            @Override
            protected void populateViewHolder(CompViewHolder viewHolder, Comp model, int position) {


                final String comp_key = getRef(position).getKey();
                viewHolder.setTitle(model.getName());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        mDatabase.child("Comps").child(comp_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.child("Comps").child(comp_key).child("Vizitator").hasChild(mAuth.getCurrentUser().getUid()))
                                {
                                    mDatabaseUsers.child("CurrentStatus").setValue("Vizitator");
                                    mDatabaseUsers.child("CurrentComp").setValue(comp_key);
                                    Intent a = new Intent(StartActivity.this,CruciadaHome.class);
                                    startActivity(a);
                                }
                                else if(dataSnapshot.child("Comps").child(comp_key).child("Concurent").hasChild(mAuth.getCurrentUser().getUid()))
                                {
                                    mDatabaseUsers.child("CurrentStatus").setValue("Concurent");
                                    mDatabaseUsers.child("CurrentComp").setValue(comp_key);
                                    Intent a = new Intent(StartActivity.this,CruciadaHome.class);
                                    startActivity(a);
                                }
                                else if(dataSnapshot.child("Comps").child(comp_key).child("Jurat").hasChild(mAuth.getCurrentUser().getUid()))
                                {
                                    mDatabaseUsers.child("CurrentStatus").setValue("Jurat");
                                    mDatabaseUsers.child("CurrentComp").setValue(comp_key);
                                    Intent a = new Intent(StartActivity.this,CruciadaHome.class);
                                    startActivity(a);
                                }
                                else
                                {
                                    mDatabaseUsers.child("CurrentStatus").setValue("Admin");
                                    mDatabaseUsers.child("CurrentComp").setValue(comp_key);
                                    Intent a = new Intent(StartActivity.this,CruciadaHome.class);
                                    startActivity(a);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });

            }
        };

        mCompList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class CompViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public CompViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setTitle(String title){

            TextView post_title = (TextView) mView.findViewById(R.id.compTitle);
            post_title.setText(title);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.help) {
            Intent intent3 = new Intent(StartActivity.this, GetHelp.class);
            startActivity(intent3);
        }
        else if(id == R.id.logout){
            mAuth.signOut();
            Intent intent2 = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent2);
        }
        else if(id == R.id.join)
        {
            final AlertDialog.Builder mBuilde = new AlertDialog.Builder(StartActivity.this);
            View mView = getLayoutInflater().inflate(R.layout.cod_concurs,null);

            final EditText mCod = (EditText) mView.findViewById(R.id.CompCod);
            TextView addBtn = (TextView) mView.findViewById(R.id.AddCompButton);

            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final String cod = mCod.getText().toString().trim();

                    if(!TextUtils.isEmpty(cod)) {

                        mDatabaseCod.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild(cod))
                                {
                                    if(!dataSnapshot.child(cod).hasChild(mAuth.getCurrentUser().getUid()))
                                    {
                                        mDatabaseCod.child(cod).child(mAuth.getCurrentUser().getUid()).child("Acces");
                                        mDatabaseCod.child(cod).child("Vizitator").child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getUid());
                                        mDatabaseCod.child(cod).child("Members").child(mAuth.getCurrentUser().getUid()).child("Name").setValue(userName);
                                        Toast.makeText(StartActivity.this,"Acum aveti acces la acest concurs",Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        Toast.makeText(StartActivity.this,"Aveti deja acces la acest concurs",Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else
                                {
                                    Toast.makeText(StartActivity.this,"Concurs inexistent",Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    else
                    {
                        Toast.makeText(StartActivity.this,"Nu ai scris nimic ",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mBuilde.setView(mView);
            AlertDialog dialog = mBuilde. create();
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }
}

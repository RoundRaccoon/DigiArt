package a10m3.cruciada;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class CruciadaHome extends AppCompatActivity {

    private RecyclerView mCruceaList;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseLike;
    private DatabaseReference mDatabaseLucrari;
    private DatabaseReference mCompKeyRef;
    private DatabaseReference databaseReference;

    private boolean mProcessLike = false;

    private FirebaseAuth mAuth;

    private DatabaseReference mCodConcursCurent;
    private int value2,value3;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference memberType;
    private String comp_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cruciada_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mCodConcursCurent = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("CurrentComp");
        mCruceaList = (RecyclerView) findViewById(R.id.crucea_list);
        mCruceaList.setHasFixedSize(true);
        mCruceaList.setLayoutManager(new LinearLayoutManager(this));
        mCompKeyRef = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("CurrentComp");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Comps");
        //mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        //mAuth.addAuthStateListener(mAuthListener);

        mCompKeyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                comp_key = dataSnapshot.getValue().toString().trim();
                mDatabaseLucrari = mDatabase.child(comp_key).child("Lucrari");

                FirebaseRecyclerAdapter<Crucea,CruceaViewHolder> FBRA = new FirebaseRecyclerAdapter<Crucea, CruceaViewHolder>(
                        Crucea.class,
                        R.layout.crucea_row,
                        CruceaViewHolder.class,
                        mDatabaseLucrari
                ) {
                    @Override
                    protected void populateViewHolder(final CruceaViewHolder viewHolder, Crucea model, int position) {

                        final String post_key = getRef(position).getKey();
                        viewHolder.setTitle(model.getTitle());
                        viewHolder.setDesc(model.getDesc());
                        viewHolder.setUserName(model.getUsername());
                        viewHolder.setImage(getApplicationContext(),model.getImage());
                        viewHolder.setButonLike(post_key);
                        //viewHolder.setLike(model.getLike());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent a = new Intent(CruciadaHome.this,SinglePost.class);
                                a.putExtra("id",post_key);
                                startActivity(a);
                            }
                        });

                        viewHolder.butonLike.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                mProcessLike = true;

                                mCompKeyRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        comp_key = dataSnapshot.getValue().toString().trim();
                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                if(mProcessLike)
                                                {

                                                    int likecount = 0;
                                                    int userlikes = 0;

                                                    if(dataSnapshot.child("Likes").hasChild(mAuth.getCurrentUser().getUid())){

                                                        userlikes = dataSnapshot.child("Likes").child(mAuth.getCurrentUser().getUid()).child("vote").getValue(int.class);
                                                        likecount = dataSnapshot.child("like").getValue(int.class);

                                                        if(userlikes==1)
                                                        {
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).removeValue();
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("like").setValue(likecount-1);
                                                        }
                                                        else
                                                        {

                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("vote").setValue(1);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("1star").setValue(1);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("2star").removeValue();
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("3star").removeValue();
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("4star").removeValue();
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("5star").removeValue();
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("like").setValue(likecount+1-userlikes);

                                                        }

                                                        mProcessLike = false;

                                                    } else {

                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("vote").setValue(1);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("1star").setValue(1);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("2star").removeValue();
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("3star").removeValue();
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("4star").removeValue();
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("5star").removeValue();
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("like").setValue(likecount+1);
                                                        mProcessLike = false;

                                                    }
                                                }
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

                        viewHolder.butonLike2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                mProcessLike = true;

                                mCompKeyRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        comp_key = dataSnapshot.getValue().toString().trim();
                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                if(mProcessLike)
                                                {
                                                    int likecount = 0;
                                                    int userlikes = 0;

                                                    if(dataSnapshot.child("Likes").hasChild(mAuth.getCurrentUser().getUid())){

                                                        userlikes = dataSnapshot.child("Likes").child(mAuth.getCurrentUser().getUid()).child("vote").getValue(int.class);
                                                        likecount = dataSnapshot.child("like").getValue(int.class);

                                                        if(userlikes==2)
                                                        {
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).removeValue();
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("like").setValue(likecount-2);
                                                        }
                                                        else
                                                        {

                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("vote").setValue(2);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("1star").setValue(1);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("2star").setValue(1);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("3star").removeValue();
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("4star").removeValue();
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("5star").removeValue();
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("like").setValue(likecount+2-userlikes);

                                                        }

                                                        mProcessLike = false;

                                                    } else {

                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("vote").setValue(2);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("1star").setValue(1);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("2star").setValue(1);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("3star").removeValue();
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("4star").removeValue();
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("5star").removeValue();
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("like").setValue(likecount+2);
                                                        mProcessLike = false;

                                                    }
                                                }
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

                        viewHolder.butonLike3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                mProcessLike = true;

                                mCompKeyRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        comp_key = dataSnapshot.getValue().toString().trim();
                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                if(mProcessLike)
                                                {

                                                    int likecount = 0;
                                                    int userlikes = 0;

                                                    if(dataSnapshot.child("Likes").hasChild(mAuth.getCurrentUser().getUid())){

                                                        userlikes = dataSnapshot.child("Likes").child(mAuth.getCurrentUser().getUid()).child("vote").getValue(int.class);
                                                        likecount = dataSnapshot.child("like").getValue(int.class);

                                                        if(userlikes==3)
                                                        {
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).removeValue();
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("like").setValue(likecount-3);
                                                        }
                                                        else
                                                        {

                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("vote").setValue(3);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("1star").setValue(1);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("2star").setValue(1);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("3star").setValue(1);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("4star").removeValue();
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("5star").removeValue();
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("like").setValue(likecount+3-userlikes);

                                                        }

                                                        mProcessLike = false;

                                                    } else {

                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("vote").setValue(3);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("1star").setValue(1);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("2star").setValue(1);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("3star").setValue(1);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("4star").removeValue();
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("5star").removeValue();
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("like").setValue(likecount+3);
                                                        mProcessLike = false;

                                                    }

                                                }

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

                        viewHolder.butonLike4.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                mProcessLike = true;

                                mCompKeyRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        comp_key = dataSnapshot.getValue().toString().trim();
                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                if(mProcessLike)
                                                {

                                                    int likecount = 0;
                                                    int userlikes = 0;

                                                    if(dataSnapshot.child("Likes").hasChild(mAuth.getCurrentUser().getUid())){

                                                        userlikes = dataSnapshot.child("Likes").child(mAuth.getCurrentUser().getUid()).child("vote").getValue(int.class);
                                                        likecount = dataSnapshot.child("like").getValue(int.class);

                                                        if(userlikes==4)
                                                        {
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).removeValue();
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("like").setValue(likecount-4);
                                                        }
                                                        else
                                                        {

                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("vote").setValue(4);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("1star").setValue(1);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("2star").setValue(1);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("3star").setValue(1);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("4star").setValue(1);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("5star").removeValue();
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("like").setValue(likecount+4-userlikes);

                                                        }

                                                        mProcessLike = false;

                                                    } else {

                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("vote").setValue(4);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("1star").setValue(1);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("2star").setValue(1);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("3star").setValue(1);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("4star").setValue(1);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("5star").removeValue();
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("like").setValue(likecount+4);
                                                        mProcessLike = false;

                                                    }

                                                }

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

                        viewHolder.butonLike5.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                mProcessLike = true;

                                mCompKeyRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        comp_key = dataSnapshot.getValue().toString().trim();
                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                if(mProcessLike)
                                                {

                                                    int likecount = 0;
                                                    int userlikes = 0;

                                                    if(dataSnapshot.child("Likes").hasChild(mAuth.getCurrentUser().getUid())){

                                                        userlikes = dataSnapshot.child("Likes").child(mAuth.getCurrentUser().getUid()).child("vote").getValue(int.class);
                                                        likecount = dataSnapshot.child("like").getValue(int.class);

                                                        if(userlikes==5)
                                                        {
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).removeValue();
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("like").setValue(likecount-5);
                                                        }
                                                        else
                                                        {

                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("vote").setValue(5);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("1star").setValue(1);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("2star").setValue(1);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("3star").setValue(1);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("4star").setValue(1);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("5star").setValue(1);
                                                            mDatabase.child(comp_key).child("Lucrari").child(post_key).child("like").setValue(likecount+5-userlikes);

                                                        }

                                                        mProcessLike = false;

                                                    } else {

                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("vote").setValue(5);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("1star").setValue(1);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("2star").setValue(1);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("3star").setValue(1);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("4star").setValue(1);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("Likes").child(mAuth.getCurrentUser().getUid()).child("5star").setValue(1);
                                                        mDatabase.child(comp_key).child("Lucrari").child(post_key).child("like").setValue(likecount+5);
                                                        mProcessLike = false;

                                                    }

                                                }

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
                };
                mCruceaList.setAdapter(FBRA);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String type = dataSnapshot.child("CurrentStatus").getValue().toString().trim();
                if(type == "Concurent")
                {
                    getMenuInflater().inflate(R.menu.menu_main_participant, menu);
                }
                else if(type == "Vizitator")
                {
                    getMenuInflater().inflate(R.menu.menu_main_vizitator, menu);
                }
                else if(type == "Jurat")
                {
                    getMenuInflater().inflate(R.menu.menu_main_jurat, menu);
                }
                else
                    getMenuInflater().inflate(R.menu.menu_main_admin, menu);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return true;
    }

    public static class CruceaViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        ImageView butonLike,butonLike2,butonLike3,butonLike4,butonLike5;

        DatabaseReference mDatabase,mDatabaseLike;
        FirebaseAuth mAuth;

        public CruceaViewHolder(View itemView)
        {
            super(itemView);
            mView= itemView;

            butonLike = (ImageView) mView.findViewById(R.id.likeButton);
            butonLike2 = (ImageView) mView.findViewById(R.id.likeButton2);
            butonLike3 = (ImageView) mView.findViewById(R.id.likeButton3);
            butonLike4 = (ImageView) mView.findViewById(R.id.likeButton4);
            butonLike5 = (ImageView) mView.findViewById(R.id.likeButton5);

            mDatabase = FirebaseDatabase.getInstance().getReference();
            mAuth = FirebaseAuth.getInstance();
            mDatabase.keepSynced(true);
        }


        public void setButonLike(final String post_key)
        {
            FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("CurrentComp").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String comp_key = dataSnapshot.getValue().toString().trim();
                    mDatabaseLike = mDatabase.child("Comps").child(comp_key).child("Lucrari").child(post_key).child("Likes");
                    mDatabaseLike.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(mAuth.getCurrentUser().getUid()))
                            {
                                if(dataSnapshot.child(mAuth.getCurrentUser().getUid()).hasChild("1star"))
                                {
                                    butonLike.setImageResource(R.drawable.full_star);
                                }
                                else
                                {
                                    butonLike.setImageResource(R.drawable.empty_star);
                                }
                                if(dataSnapshot.child(mAuth.getCurrentUser().getUid()).hasChild("2star"))
                                {
                                    butonLike2.setImageResource(R.drawable.full_star);
                                }
                                else
                                {
                                    butonLike2.setImageResource(R.drawable.empty_star);
                                }
                                if(dataSnapshot.child(mAuth.getCurrentUser().getUid()).hasChild("3star"))
                                {
                                    butonLike3.setImageResource(R.drawable.full_star);
                                }
                                else
                                {
                                    butonLike3.setImageResource(R.drawable.empty_star);
                                }
                                if(dataSnapshot.child(mAuth.getCurrentUser().getUid()).hasChild("4star"))
                                {
                                    butonLike4.setImageResource(R.drawable.full_star);
                                }
                                else
                                {
                                    butonLike4.setImageResource(R.drawable.empty_star);
                                }
                                if(dataSnapshot.child(mAuth.getCurrentUser().getUid()).hasChild("5star"))
                                {
                                    butonLike5.setImageResource(R.drawable.full_star);
                                }
                                else
                                {
                                    butonLike5.setImageResource(R.drawable.empty_star);
                                }
                            }
                            else
                            {
                                butonLike.setImageResource(R.drawable.empty_star);
                                butonLike2.setImageResource(R.drawable.empty_star);
                                butonLike3.setImageResource(R.drawable.empty_star);
                                butonLike4.setImageResource(R.drawable.empty_star);
                                butonLike5.setImageResource(R.drawable.empty_star);
                            }
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

        public void setTitle(String title)
        {
            TextView post_title=(TextView) mView.findViewById(R.id.textTitle);
            post_title.setText(title);
        }
        public void setDesc(String desc)
        {
            TextView post_desc=(TextView) mView.findViewById(R.id.textDescription);
            post_desc.setText(desc);
        }

        public void setImage(Context ctx, String image)
        {
            ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);
            Picasso
                    .with(ctx)
                    .load(image)
                    .fit()
                    .centerInside()
                    .into(post_image);
        }

        public void setUserName( String userName)
        {
            TextView post_user=(TextView) mView.findViewById(R.id.usernameET);
            post_user.setText(userName);
        }

        /*public void setLike(String like)
        {
            TextView numar = (TextView) mView.findViewById(numarlike);
            numar.setText(like);
        }*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.addIcon) {

            Intent intent = new Intent(CruciadaHome.this, PostActivity.class);
            startActivity(intent);

        }
        else if(id == R.id.rezultate){
            Intent intent3 = new Intent(CruciadaHome.this, ResultsActivity.class);
            startActivity(intent3);
        }
        else if(id == R.id.getId)
        {
            final AlertDialog.Builder mBuilder = new AlertDialog.Builder(CruciadaHome.this);
            View mView = getLayoutInflater().inflate(R.layout.get_currentcomp,null);

            final TextView mCod  = (TextView) mView.findViewById(R.id.codCurent);
            final TextView mCopy = (TextView) mView.findViewById(R.id.codCopy);

            mCodConcursCurent.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    final String cod = dataSnapshot.getValue().toString().trim();
                    mCod.setText(cod);



                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mCodConcursCurent.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("label",dataSnapshot.getValue().toString().trim());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(CruciadaHome.this,"Codul a fost copiat in telefon",Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });

            mBuilder.setView(mView);
            AlertDialog dialog = mBuilder. create();
            dialog.show();

        }
        else if(id == R.id.manage)
        {
            final Intent a = new Intent(CruciadaHome.this,MembersActivity.class);
            mCodConcursCurent.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    a.putExtra("id",dataSnapshot.getValue().toString());
                    startActivity(a);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(CruciadaHome.this,StartActivity.class));
        super.onBackPressed();
    }

}
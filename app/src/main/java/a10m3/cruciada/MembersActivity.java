package a10m3.cruciada;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MembersActivity extends AppCompatActivity {

    private RecyclerView mPersonList;
    private DatabaseReference mDatabaseNames;
    private String comp_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);
        mPersonList = (RecyclerView) findViewById(R.id.person_list);
        mPersonList.setHasFixedSize(true);
        mPersonList.setLayoutManager(new LinearLayoutManager(this));
        comp_key = getIntent().getExtras().getString("id");
        mDatabaseNames = FirebaseDatabase.getInstance().getReference().child("Comps").child(comp_key).child("Members");

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerAdapter<person,PersonViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<person, PersonViewHolder>(
                person.class,
                R.layout.person_row,
                PersonViewHolder.class,
                mDatabaseNames
        ) {
            @Override
            protected void populateViewHolder(PersonViewHolder viewHolder, person model, int position) {

                final String person_key = getRef(position).getKey();
                viewHolder.setTitle(model.getName());

            }
        };

        mPersonList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class PersonViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public PersonViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setTitle(String title){

            TextView post_title = (TextView) mView.findViewById(R.id.personName);
            post_title.setText(title);

        }

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(MembersActivity.this,CruciadaHome.class));
        super.onBackPressed();
    }


}

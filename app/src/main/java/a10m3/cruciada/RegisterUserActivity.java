package a10m3.cruciada;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegisterUserActivity extends AppCompatActivity {

    EditText emailRegister, passRegister, username, passConfirm;
    TextView CreateAccount;

    ProgressDialog mProgressDialog;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        emailRegister = (EditText) findViewById(R.id.emailRegister);
        passRegister = (EditText) findViewById(R.id.passRegister);
        username = (EditText) findViewById(R.id.usernameET);
        passConfirm = (EditText) findViewById(R.id.passRegisterConfirm);

        CreateAccount = (TextView) findViewById(R.id.createAccount);

        mProgressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuthListener =  new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user= firebaseAuth.getCurrentUser();
                if(user!=null)
                {
                    Intent moveToHome = new Intent(RegisterUserActivity.this, CruciadaHome.class);
                    moveToHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(moveToHome);
                }
            }
        };

        mAuth.addAuthStateListener(mAuthListener);

        CreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgressDialog.setTitle("Se creeaza contul");
                mProgressDialog.setMessage("Va rugam așteptați...");
                mProgressDialog.show();
                mProgressDialog.setCancelable(false);
                mProgressDialog.setCanceledOnTouchOutside(false);
                try {
                    createUserAccount();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(RegisterUserActivity.this,MainActivity.class));
        super.onBackPressed();
    }

    public static String hashPassword(String password)throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.update(password.getBytes());
        byte[] b = md.digest();
        StringBuffer sb = new StringBuffer();
        for(byte b1 : b){
            sb.append(Integer.toHexString(b1 & 0xff).toString());
        }
        return sb.toString();
    }


    //Funcția care se apelează când îți creezi un cont
    private void createUserAccount() throws NoSuchAlgorithmException {

        //Se preiau datele utilizatorului
        final String emailUser, passUser, user, passConf, salt, hashPassUser;
        emailUser = emailRegister.getText().toString().trim();
        passUser = passRegister.getText().toString().trim();
        user = username.getText().toString().trim();
        passConf = passConfirm.getText().toString().trim();
        salt = "#*(I3";
        hashPassUser = hashPassword(passUser+salt);


        //Verificăm dacă toate spațiile sunt completate și parola este optimă (are peste 6 cararctere și este scrisă la fel și în caseta de ”Confirmare parolă”)
        if( !TextUtils.isEmpty(emailUser) && !TextUtils.isEmpty(passUser) && !TextUtils.isEmpty(user) && passConf.compareTo(passUser)==0 && passConf.length()>=6)
        {
            //Dacă totul este corect, accesăm baza de date și stocăm email-ul, parola și numele de utilizator
            mAuth.createUserWithEmailAndPassword(emailUser, hashPassUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if( task.isSuccessful())
                    {
                        String userid = mAuth.getCurrentUser().getUid();
                        DatabaseReference current_user_db = databaseReference.child(userid);
                        current_user_db.child("Name").setValue(user);
                        //După ce contul este creat, pe ecran apare textul "Contul a fost creat cu succes", apoi se deschide pagina principală
                        Toast.makeText(RegisterUserActivity.this, "Contul a fost creat cu succes", Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();
                        startActivity(new Intent(RegisterUserActivity.this, StartActivity.class));
                    }
                    else
                    {
                        Toast.makeText(RegisterUserActivity.this, "Email invalid", Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();
                    }
                }
            });
        }
        else
        {
            if(TextUtils.isEmpty(emailUser)||TextUtils.isEmpty(passUser)||TextUtils.isEmpty(user)||TextUtils.isEmpty(passConf))
            {
                Toast.makeText(RegisterUserActivity.this, "Completează toate spațiile", Toast.LENGTH_SHORT).show();
            }
            else if(passConf.compareTo(passUser)!=0)
            {
                Toast.makeText(RegisterUserActivity.this, "Cele doua parole sunt diferite", Toast.LENGTH_SHORT).show();
            }
            else if(passConf.compareTo(passUser)==0 && passConf.length()<6)
            {
                Toast.makeText(RegisterUserActivity.this, "Parola trebuie să conțină cel puțin 6 caractere", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(RegisterUserActivity.this, "Contul nu a putut fi creat", Toast.LENGTH_SHORT).show();
            }
            mProgressDialog.dismiss();
        }
    }
}

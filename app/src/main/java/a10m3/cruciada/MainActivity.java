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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    EditText userEmailEdit, userPassEdit;
    TextView mainLoginBtn, mainCreateBtn;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userEmailEdit = (EditText) findViewById(R.id.mainEmailET);
        userPassEdit = (EditText) findViewById(R.id.mainPassET);

        mainLoginBtn = (TextView) findViewById(R.id.mainLoginBtn);
        mainCreateBtn = (TextView) findViewById(R.id.mainCreateBtn);

        mAuth = FirebaseAuth.getInstance();

        mProgressDialog = new ProgressDialog(this);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {


                FirebaseUser user = firebaseAuth.getCurrentUser();

                if( user!=null )
                {

                    Intent moveToHome = new Intent(MainActivity.this, StartActivity.class);
                    moveToHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(moveToHome);

                }
            }
        };

        mAuth.addAuthStateListener(mAuthListener);


        mainCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterUserActivity.class));
            }
        });

        mainLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mProgressDialog.setTitle("Autentificare în curs");
                mProgressDialog.setMessage("Vă rugăm așteptați...");
                mProgressDialog.show();
                mProgressDialog.setCancelable(false);
                mProgressDialog.setCanceledOnTouchOutside(false);
                try {
                    loginUser();
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


    //Funcția care se apelează când utilizatorul apasă butoul de ”AUTENTIFICARE”
    private void loginUser() throws NoSuchAlgorithmException {

        //Preluăm datele (email și parolă) scrise de utilizator
        String userEmail, userPass, salt, hashUserPass;
        salt = "#*(I3";
        userEmail = userEmailEdit.getText().toString().trim();
        userPass = userPassEdit.getText().toString().trim();
        hashUserPass = hashPassword(userPass+salt);
        //Se verifică dacă s-au introdus un email și o parolă
        if( !TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(userPass))
        {
            //Verificăm dacă în baza de date există o combinație email-parolă ca cea introdusă
            mAuth.signInWithEmailAndPassword(userEmail, hashUserPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    //Dacă input-ul este corect, se deschide pagina principală a aplicației
                    if(task.isSuccessful())
                    {
                        mProgressDialog.dismiss();
                        Intent moveToHome = new Intent(MainActivity.this, StartActivity.class);
                        startActivity(moveToHome);
                    }
                    else   //Altfel, o casetă de dialog apare cu mesajul de mai jos
                    {
                        Toast.makeText(MainActivity.this, "Date de conectare invalide", Toast.LENGTH_LONG).show();
                        mProgressDialog.dismiss();
                    }
                }
            });
        }else //Dacă nu au fost introduse un email/parolă, apare mesajul următor
        {
            Toast.makeText(MainActivity.this, "Introdu email si parola", Toast.LENGTH_LONG).show();
            mProgressDialog.dismiss();
        }
    }
}
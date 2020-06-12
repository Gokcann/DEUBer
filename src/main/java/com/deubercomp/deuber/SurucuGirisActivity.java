package com.deubercomp.deuber;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SurucuGirisActivity extends AppCompatActivity {

    //.xml dosyasinda tasarladigimiz edit textleri ve butonlari baglamak icin burada olusturuyoruz
    private EditText vEmail, vSifre;
    private Button vGiris, vKayit;

    //FireBase baglantisi icin gereken kutuphaneyi (core) ekledikten sonra devam ediyoruz
    private FirebaseAuth vAuth;
    //kimlik dogrulama esnasinda cagirilan metod, eklemek icin add silmek icin remove eklenir basina
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surucu_giris);
        //firebase classında bulunan istegi dondurur
        vAuth = FirebaseAuth.getInstance();
        //kimlik dogrulama metodumuzu olusturduk
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser kullanici = FirebaseAuth.getInstance().getCurrentUser();
                //getCurrrentUser() metodu oturum acan kullaniciyi dondurur eger yoksa null dondurur
                if(kullanici!=null){
                    Intent intent = new Intent(SurucuGirisActivity.this, SurucuHaritaActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };


        //olusturdugumuz widget lari burada bagliyoruz
        vEmail = (EditText) findViewById(R.id.email);
        vSifre = (EditText) findViewById(R.id.sifre);
        vGiris = (Button) findViewById(R.id.giris);
        vKayit = (Button) findViewById(R.id.kayit);

        //!!ONEMLI; bu asamadan sonra FireBase kutuphanesini eklememiz gerekli cunku
        //! artik giris icin gerekli islemlere baslayacagiz
        //! bunun icinde FireBase baglantisi gerekiyor
        //FireBase guide -> get started -> android -> add the SDK

        vKayit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = vEmail.getText().toString();
                final String sifre = vSifre.getText().toString();

                vAuth.createUserWithEmailAndPassword(email,sifre).addOnCompleteListener(SurucuGirisActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(SurucuGirisActivity.this, "KAYIT HATASI", Toast.LENGTH_SHORT).show();
                        }//eger islem tamamlanirsa aktif_kullanici_db adinda bir kok olusturuyor
                        else {
                            String kullanici_id = vAuth.getCurrentUser().getUid();
                            DatabaseReference aktif_kullanici_db = FirebaseDatabase.getInstance().getReference().child("Kullanici").child("Surucu").child(kullanici_id);
                            aktif_kullanici_db.setValue(true);
                        }
                    }
                });
            }
        });
        //kayit icin benzer islemler giris icinde tekrar ediyor
        vGiris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = vEmail.getText().toString();
                final String sifre = vSifre.getText().toString();
                vAuth.signInWithEmailAndPassword(email,sifre).addOnCompleteListener(SurucuGirisActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(SurucuGirisActivity.this, "GIRIS HATASI", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
    //SurucuGirisActivity ekranindan cikis yapildiginda firebaseAuthListener metodunu sonlandiriyoruz.

    @Override
    protected void onStart() {
        super.onStart();
        vAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        vAuth.removeAuthStateListener(firebaseAuthListener);
    }
}

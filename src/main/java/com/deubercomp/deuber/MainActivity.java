package com.deubercomp.deuber;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    //.xml dosyasinda tasarladigimiz butonları baglamak icin burada 2 adet buton olusturuyoruz
    private Button vSurucu, vOtostopcu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //olusturdugumuz butonlari burada .xml dosyasindakilerle bagliyoruz
        vSurucu = (Button) findViewById(R.id.surucu);
        vOtostopcu = (Button) findViewById(R.id.otostopcu);

        //onClick metodunu olusturma
        vSurucu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //burada baska bir activity'e baglanmak icin intent olusturuyoruz (runtime binding)
                Intent intent = new Intent(MainActivity.this, SurucuGirisActivity.class);
                startActivity(intent);
                finish();
                return;
                //bu asamadan sonra intent icinde belirttigimiz SurucuGirisActivity'i olusturuyoruz
                //bunun icin izlenen yol; com.deubercomp.deuber -> new -> activity -> empty activity
            }
        });

        vOtostopcu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //burada baska bir activity'e baglanmak icin intent olusturuyoruz (runtime binding)
                Intent intent = new Intent(MainActivity.this, OtostopcuGirisActivity.class);
                startActivity(intent);
                finish();
                return;
                //bu asamadan sonra intent icinde belirttigimiz SurucuGirisActivity'i olusturuyoruz
                //bunun icin izlenen yol; com.deubercomp.deuber -> new -> activity -> empty activity
            }
        });

        vSurucu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SurucuGirisActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        vOtostopcu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OtostopcuGirisActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

    }
}

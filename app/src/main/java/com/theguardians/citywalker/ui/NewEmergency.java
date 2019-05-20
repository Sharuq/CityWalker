package com.theguardians.citywalker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.theguardians.citywalker.R;

public class NewEmergency extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newemergency);
        Toolbar toolbar = findViewById (R.id.toolbar);
        toolbar.setTitle ("Emergency Support");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        setSupportActionBar (toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewEmergency.this, MainActivity.class);
                startActivity(intent);
            }
        });



    }
}

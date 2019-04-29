package com.theguardians.citywalker.ui;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.theguardians.citywalker.Model.ContactHandler;
import com.theguardians.citywalker.Model.UserContact;
import com.theguardians.citywalker.R;


public class UpdateContactActivity extends AppCompatActivity {


    private ContactHandler handler;
    private String name;
    private String phone;

    private EditText et_name;
    private EditText et_phone;
    private Button updateContactBtn;
    SQLiteDatabase db;
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_contact);
        Toolbar toolbar = findViewById (R.id.toolbar);
        toolbar.setTitle ("Update Emergency Contact");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        setSupportActionBar (toolbar);
        extras = getIntent().getExtras();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UpdateContactActivity.this, ContactEmergencyActivity.class);
                startActivity(intent);
            }
        });

        handler = new ContactHandler(getApplicationContext());

        updateContactBtn =findViewById (R.id.updateContactButton);
        et_name = (EditText) findViewById(R.id.userName);
        et_phone = (EditText) findViewById(R.id.phoneNumber);


        db = handler.getReadableDatabase ();
        updateContactBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                name = et_name.getText().toString();
                phone = et_phone.getText().toString();
                UserContact contact = new UserContact();
                contact.setId(extras.getInt("id"));
                contact.setName(name);
                contact.setPhoneNumber(phone);
               // db.execSQL("DROP TABLE IF EXISTS contacts");
               // handler.onCreate (db);
                Boolean updated = handler.editContact (contact);

                if(updated){
                    Intent intent = new Intent(UpdateContactActivity.this, ContactEmergencyActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "Contact data not updated. Please try again", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
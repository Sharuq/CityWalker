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


public class AddContactActivity extends AppCompatActivity {


    private ContactHandler handler;
    private String name;
    private String phone;

    private EditText et_name;
    private EditText et_phone;
    private Button addContactBtn;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact);
        Toolbar toolbar = findViewById (R.id.toolbar);
        toolbar.setTitle ("Add Emergency Contact");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        setSupportActionBar (toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddContactActivity.this, EmergencyActivity.class);
                startActivity(intent);
            }
        });

        handler = new ContactHandler(getApplicationContext());

        addContactBtn =findViewById (R.id.addContactButton);
        et_name = (EditText) findViewById(R.id.userName);
        et_phone = (EditText) findViewById(R.id.phoneNumber);


        db = handler.getReadableDatabase ();
        addContactBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                name = et_name.getText().toString();
                phone = et_phone.getText().toString();
                UserContact contact = new UserContact();
                contact.setName(name);
                contact.setPhoneNumber(phone);
                db.execSQL("DROP TABLE IF EXISTS contacts");
                handler.onCreate (db);
                if(name.length()==0){
                    et_name.requestFocus();
                    et_name.setError("Field cannot be empty");
                }else if (!name.matches("[a-zA-Z]+")){
                    et_name.requestFocus();
                    et_name.setError("Enter only alphabetical characters");
                }else if (phone.length()==0){
                    et_phone.requestFocus();
                    et_phone.setError("Field cannot be empty");
                }else if (!phone.matches("^[0][0-9]{9}$")){
                    et_phone.requestFocus();
                    et_phone.setError("Please enter a Australia 10 digits phone number start from 0");
                }
                else {
                    Boolean added = handler.addContactDetails (contact);
                    if (added) {
                        Intent intent = new Intent (AddContactActivity.this, ContactEmergencyActivity.class);
                        startActivity (intent);
                        System.out.println ("Added");
                    } else {
                        Toast.makeText (getApplicationContext (), "Contact data not added. Please try again", Toast.LENGTH_LONG).show ();
                    }
                }
            }
        });
    }

}
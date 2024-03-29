package com.theguardians.citywalker.ui;
/**
 * This class is utilised for updating emergency contact
 * @Author Sharuq
 * @Version 2.1
 */

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

import java.util.List;


public class UpdateContactActivity extends AppCompatActivity {


    private ContactHandler handler;
    private String name;
    private String phone;

    private EditText et_name;
    private EditText et_phone;
    private Button updateContactBtn;
    SQLiteDatabase db;
    //Bundle extras =new Bundle ();
    private int Id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_contact);
        Toolbar toolbar = findViewById (R.id.toolbar);
        toolbar.setTitle ("Emergency Support");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        setSupportActionBar (toolbar);
        Intent mIntent = getIntent();
        Id = mIntent.getIntExtra("Id", 0);
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

        // Reading all contacts
        List<UserContact> contacts = handler.readAllContacts();

        et_name.setText (contacts.get (0).getName ());
        et_phone.setText (contacts.get (0).getPhoneNumber ());


        db = handler.getReadableDatabase ();
        updateContactBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                name = et_name.getText().toString();
                phone = et_phone.getText().toString();
                UserContact contact = new UserContact();
                contact.setId(Id);
                contact.setName(name);
                contact.setPhoneNumber(phone);
                if(name.length()==0){
                    et_name.requestFocus();
                    et_name.setError("Field cannot be empty");
                }else if (!name.matches("[a-zA-Z]+")){
                    et_name.requestFocus();
                    et_name.setError("Enter only alphabetical characters");
                }else if (phone.length()==0){
                    et_phone.requestFocus();
                    et_phone.setError("Field cannot be empty");
                }else if (!phone.matches("^[0][2-8][0-9]{8}$")){
                    et_phone.requestFocus();
                    et_phone.setError("Please enter a Australia 10 digits phone number start from 0");
                }else {
                    // db.execSQL("DROP TABLE IF EXISTS contacts");
                    // handler.onCreate (db);
                    Boolean updated = handler.editContact (contact);

                    if (updated) {
                        Intent intent = new Intent (UpdateContactActivity.this, ContactEmergencyActivity.class);
                        startActivity (intent);
                    } else {
                        Toast.makeText (getApplicationContext (), "Contact data not updated. Please try again", Toast.LENGTH_LONG).show ();
                    }
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(UpdateContactActivity.this, ContactEmergencyActivity.class);
        startActivity(intent);
    }

}
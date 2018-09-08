package com.kapp.TrustFall;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class EmergencyContacts extends AppCompatActivity {

    private static final String TAG = "EMERGENCY_CONTACTS";

    static public SharedPreferences prefs;

    private EditText ContactNumber1;
    private EditText ContactNumber2;
    private EditText ContactNumber3;
    private EditText ContactNumber4;

    private EditText ContactName1;
    private EditText ContactName2;
    private EditText ContactName3;
    private EditText ContactName4;

    private String SavedContactNumber1 = "com.kapp.app.savedcontactnumber1";
    private String SavedContactNumber2 = "com.kapp.app.savedcontactnumber2";
    private String SavedContactNumber3 = "com.kapp.app.savedcontactnumber3";
    private String SavedContactNumber4 = "com.kapp.app.savedcontactnumber4";

    private String SavedContactName1 = "com.kapp.app.savedcontactname1";
    private String SavedContactName2 = "com.kapp.app.savedcontactname2";
    private String SavedContactName3 = "com.kapp.app.savedcontactname3";;
    private String SavedContactName4 = "com.kapp.app.savedcontactname4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        ContactNumber1 = (EditText) findViewById(R.id.contactNumber1);
        ContactNumber2 = (EditText) findViewById(R.id.contactNumber2);
        ContactNumber3 = (EditText) findViewById(R.id.contactNumber3);
        ContactNumber4 = (EditText) findViewById(R.id.contactNumber4);

        ContactName1 = (EditText) findViewById(R.id.contactName1);
        ContactName2 = (EditText) findViewById(R.id.contactName2);
        ContactName3 = (EditText) findViewById(R.id.contactName3);
        ContactName4 = (EditText) findViewById(R.id.contactName4);

        prefs = this.getSharedPreferences("com.kapp.app", Context.MODE_PRIVATE);

        SetContactText();
    }

    public void SaveContacts(View view)
    {
        SaveContactValuesToPrefs();
        Utils.ShowToast(getApplicationContext(),"Saved");
    }

    private void SetContactText()
    {
        ContactNumber1.setText(prefs.getString(SavedContactNumber1, "-"));
        ContactNumber2.setText(prefs.getString(SavedContactNumber2, "-"));
        ContactNumber3.setText(prefs.getString(SavedContactNumber3, "-"));
        ContactNumber4.setText(prefs.getString(SavedContactNumber4, "-"));

        ContactName1.setText(prefs.getString(SavedContactName1, "Contact Name #1"));
        ContactName2.setText(prefs.getString(SavedContactName2, "Contact Name #2"));
        ContactName3.setText(prefs.getString(SavedContactName3, "Contact Name #3"));
        ContactName4.setText(prefs.getString(SavedContactName4, "Contact Name #4"));
    }

    private void SaveContactValuesToPrefs()
    {
        String SaveContactNumber1 = ContactNumber1.getText().toString();
        String SaveContactNumber2 = ContactNumber2.getText().toString();
        String SaveContactNumber3 = ContactNumber3.getText().toString();
        String SaveContactNumber4 = ContactNumber4.getText().toString();

        prefs.edit().putString(SavedContactNumber1,SaveContactNumber1).apply();
        prefs.edit().putString(SavedContactNumber2,SaveContactNumber2).apply();
        prefs.edit().putString(SavedContactNumber3,SaveContactNumber3).apply();
        prefs.edit().putString(SavedContactNumber4,SaveContactNumber4).apply();

        String SaveContactName1 = ContactName1.getText().toString();
        String SaveContactName2 = ContactName2.getText().toString();
        String SaveContactName3 = ContactName3.getText().toString();
        String SaveContactName4 = ContactName4.getText().toString();

        prefs.edit().putString(SavedContactName1,SaveContactName1).apply();
        prefs.edit().putString(SavedContactName2,SaveContactName2).apply();
        prefs.edit().putString(SavedContactName3,SaveContactName3).apply();
        prefs.edit().putString(SavedContactName4,SaveContactName4).apply();
    }
}


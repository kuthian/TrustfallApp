package com.kapp.TrustFall;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class AlertSettings extends AppCompatActivity {

    private static final String TAG = "ALERT_SETTINGS";

    SharedPreferences prefs;
    private TextView SeekBarValue;
    private Switch OnOrOffSwitch;
    private String SavedSeekBarValue = "com.kapp.app.savedseekbarvalue";;
    private String SavedOnOrOFf = "com.kapp.app.savedonoroff";;
    private int SeekBarInterval = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_settings);

        prefs = this.getSharedPreferences("com.kapp.app", Context.MODE_PRIVATE);

        OnOrOffSwitch = (Switch) findViewById(R.id.OnOrOffSwitch);
        SeekBarValue = (TextView) findViewById(R.id.SeekBarValue);

        final SeekBar CountdownSeekBar = (SeekBar) findViewById(R.id.CountdownSeekBar);
        final Button SaveSettingsButton = (Button) findViewById(R.id.SaveSettingsButton);

        SeekBarValue.setText(prefs.getString(SavedSeekBarValue, "-"));
        OnOrOffSwitch.setChecked(prefs.getBoolean(SavedOnOrOFf, true));
        CountdownSeekBar.setProgress(Integer.parseInt(prefs.getString(SavedSeekBarValue, "30"))/ SeekBarInterval);

        CountdownSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar CountdownSeekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar CountdownSeekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar CountdownSeekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                SeekBarValue.setText(String.valueOf(progress* SeekBarInterval));
            }

        });

        SaveSettingsButton.setOnClickListener(new View.OnClickListener()
        {

            public void onClick(View v)
            {
                String SaveSeekBarValue = SeekBarValue.getText().toString();
                Boolean SaveOnOrOffSwitch = OnOrOffSwitch.isChecked();

                prefs.edit().putString(SavedSeekBarValue, SaveSeekBarValue).apply();
                prefs.edit().putBoolean(SavedOnOrOFf, SaveOnOrOffSwitch).apply();

                Utils.ShowToast(getApplicationContext(),"Saved");
            }
        });
    }
}

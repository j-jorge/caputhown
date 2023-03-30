package de.digisocken.stop_o_moto;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class PreferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager()
            .beginTransaction()
            .replace(android.R.id.content, new PreferenceFragment())
            .commit();
    }
}

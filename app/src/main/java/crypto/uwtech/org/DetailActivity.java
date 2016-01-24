package crypto.uwtech.org;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import crypto.uwtech.org.crypto.R;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_detail);

        TextView keyModulus = (TextView) findViewById(R.id.keyModulus);
        TextView keyPrivate = (TextView) findViewById(R.id.keyPrivate);
        TextView keyPublic = (TextView) findViewById(R.id.keyPublic);

        if(MainActivity.KEY != null) {
            keyModulus.setText(MainActivity.KEY.getModulus().toString());
            keyPrivate.setText(MainActivity.KEY.getPrivateKey().toString());
            keyPublic.setText(MainActivity.KEY.getPublicKey().toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }
}

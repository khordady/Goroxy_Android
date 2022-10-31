package arteh.world.goroxy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sh;
    EditText server, port, username, password;
    Spinner encryption;
    CheckBox authentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        server = findViewById(R.id.server);
        port = findViewById(R.id.port);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        encryption = findViewById(R.id.encryption);
        authentication = findViewById(R.id.authentication);

        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.encryption, android.R.layout.simple_spinner_dropdown_item);
        encryption.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sh = getSharedPreferences("date", MODE_PRIVATE);
        if (!sh.getString("server", " ").equals(" ")) {
            server.setText(sh.getString("server", ""));
            port.setText(sh.getString("port", ""));
            username.setText(sh.getString("username", ""));
            password.setText(sh.getString("password", ""));
            encryption.setSelection(sh.getInt("encryption", 0));
            authentication.setChecked(sh.getBoolean("authentication", false));
        }
    }

    public void connect(View view) {
        SharedPreferences.Editor editor=sh.edit();
        editor.putString("server", server.getText().toString());
        editor.putString("port", port.getText().toString());
        editor.putString("username", username.getText().toString());
        editor.putString("password", password.getText().toString());
        editor.putInt("encryption", encryption.getSelectedItemPosition());
        editor.putBoolean("authentication", authentication.isChecked());
        editor.apply();


    }
}
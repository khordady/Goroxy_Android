package arteh.world.goroxy;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {

    SharedPreferences sh;
    EditText server, port, username, password, encryption_key, encryption_iv;
    Spinner encryption;
    CheckBox authentication, write_server;
    Button connectbtn;
    ListenSocket listenSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        server = findViewById(R.id.server);
        port = findViewById(R.id.port);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        encryption = findViewById(R.id.encryption);
        encryption_key = findViewById(R.id.encryption_key);
        encryption_iv = findViewById(R.id.encryption_iv);
        authentication = findViewById(R.id.authentication);
        write_server = findViewById(R.id.write_server);

        connectbtn = findViewById(R.id.connectbtn);

        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.encryption, android.R.layout.simple_spinner_dropdown_item);
        encryption.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        encryption.setSelection(1,false);

        sh = getSharedPreferences("date", MODE_PRIVATE);
        if (!sh.getString("server", " ").equals(" ")) {
            server.setText(sh.getString("server", ""));
            port.setText(sh.getInt("port", 0) + "");
            username.setText(sh.getString("username", ""));
            password.setText(sh.getString("password", ""));
            encryption_key.setText(sh.getString("encryption_key", ""));
            encryption_iv.setText(sh.getString("encryption_iv", ""));
            encryption.setSelection(sh.getInt("encryption", 0));
            authentication.setChecked(sh.getBoolean("authentication", true));
            write_server.setChecked(sh.getBoolean("write_server", true));
        }
    }

    public void connect(View view) {
        if (view.getTag().toString().equals("0")) {
            connectbtn.setText("Connecting");
            SharedPreferences.Editor editor = sh.edit();
            editor.putString("server", server.getText().toString());
            editor.putInt("port", Integer.parseInt(port.getText().toString()));
            editor.putString("username", username.getText().toString());
            editor.putString("password", password.getText().toString());
            editor.putString("encryption_key", encryption_key.getText().toString());
            editor.putString("encryption_iv", encryption_iv.getText().toString());
            editor.putInt("encryption", encryption.getSelectedItemPosition());
            editor.putBoolean("authentication", authentication.isChecked());
            editor.putBoolean("write_server", write_server.isChecked());
            editor.apply();

            Config.server = server.getText().toString();
            Config.port = Integer.parseInt(port.getText().toString());
            Config.username = username.getText().toString();
            Config.password = password.getText().toString();
            Config.encryption_key = encryption_key.getText().toString();
            Config.encryption_iv = encryption_iv.getText().toString();
            Config.encryption = encryption.getSelectedItemPosition();
            Config.authentication = authentication.isChecked();
            Config.write_server = write_server.isChecked();

            Encryptor.initialize();

            listenSocket = new ListenSocket(new Listener() {
                @Override
                public void Connected() {
                    runOnUiThread(() -> {
                        view.setBackgroundResource(R.drawable.circ_connected);
                        view.setTag("1");
                        connectbtn.setText("Connected");
                    });
                }

                @Override
                public void failed(String message) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show());
                }
            });
            listenSocket.start();
        } else {
            view.setBackgroundResource(R.drawable.circ_connect);
            view.setTag("0");
            connectbtn.setText("Connect");
            listenSocket.close();
        }
    }
}
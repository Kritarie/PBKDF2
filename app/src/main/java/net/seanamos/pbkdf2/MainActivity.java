package net.seanamos.pbkdf2;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity {

    private PasswordPrefs cripshin;

    private EditText password;
    private EditText key;
    private EditText secret;
    private Button encrypt;
    private Button decrypt;
    private Button clear;
    private TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cripshin = new PasswordPrefs(this);
        password = (EditText)findViewById(R.id.password);
        key = (EditText)findViewById(R.id.key);
        secret = (EditText)findViewById(R.id.secret);
        encrypt = (Button)findViewById(R.id.encrypt);
        decrypt = (Button)findViewById(R.id.decrypt);
        clear = (Button)findViewById(R.id.clear);
        result = (TextView)findViewById(R.id.result);

        encrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String passwordString = password.getText().toString();
                String keystring = key.getText().toString();
                String secretString = secret.getText().toString();
                try {
                    cripshin.encrypt(keystring, secretString, passwordString);
                    result.setText("ok");
                    result.setTextColor(Color.GREEN);
                } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                    result.setText("nah");
                    result.setTextColor(Color.RED);
                }
            }
        });

        decrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String passwordString = password.getText().toString();
                String keystring = key.getText().toString();
                try {
                    String secret = cripshin.decrypt(keystring, passwordString);
                    result.setText(secret);
                    result.setTextColor(Color.GREEN);
                } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                    result.setText("nah");
                    result.setTextColor(Color.RED);
                }
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cripshin.clear();
            }
        });
    }
}

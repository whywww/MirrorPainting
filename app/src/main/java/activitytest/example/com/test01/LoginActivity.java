package activitytest.example.com.test01;

import android.app.DownloadManager;
import android.app.VoiceInteractor;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText accountEdit;
    private EditText passwordEdit;
    private TextView forgetPassword;
    private TextView signUpNav;
    private Button login;
    TextView responseText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        accountEdit = (EditText) findViewById(R.id.usrName);
        passwordEdit = (EditText) findViewById(R.id.password);
        forgetPassword = (TextView) findViewById(R.id.forget);
        signUpNav = (TextView) findViewById(R.id.signUpNav);
        login = (Button) findViewById(R.id.login);
        signUpNav.setOnClickListener(this);
        forgetPassword.setOnClickListener(this);
        responseText=(TextView)findViewById(R.id.response_text);
        login.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.login) {
           sendRequestWithHttpURLConnection();
        }
        if (v.getId() == R.id.signUpNav) {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        }
        if (v.getId() == R.id.forget) {
            Toast.makeText(LoginActivity.this, "找回密码", Toast.LENGTH_SHORT).show();
        }
    }


    private void sendRequestWithHttpURLConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection=null;
                BufferedReader reader=null;
                try{

                    URL url=new URL("http://139.199.162.139:5000/login");
                    connection=(HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(80000);
                    connection.setReadTimeout(80000);

                   DataOutputStream out=new DataOutputStream(connection.getOutputStream());
                   out.writeBytes("userName=admin&password=123456");
                   InputStream in=connection.getInputStream();

                    //对输入流读取
                    reader=new BufferedReader(new InputStreamReader(in));
                    StringBuilder response=new StringBuilder();
                    String line;
                     while ((line=reader.readLine())!=null) {
                        response.append(line);
                     }
                     showResponse(response.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if (reader!=null){
                        try{
                            reader.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    if (connection!=null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
    private void showResponse(final String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                  //  responseText.setText(response);
               System.out.println(response);
            }
        });
    }
}

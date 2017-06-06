package activitytest.example.com.test01;

import android.app.DownloadManager;
import android.app.VoiceInteractor;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.ConnectException;
import java.util.StringTokenizer;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText username;
    private EditText password;
    private TextView forgetPassword;
    private TextView signUpNav;
    private Button login;
    TextView responseText;
    String PassWord = "";
    String UserName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (EditText) findViewById(R.id.usrname_edit);
        password = (EditText) findViewById(R.id.password_edit);
        forgetPassword = (TextView) findViewById(R.id.forget);
        signUpNav = (TextView) findViewById(R.id.signUpNav);
        login = (Button) findViewById(R.id.login);
        responseText=(TextView)findViewById(R.id.response_text);

        signUpNav.setOnClickListener(this);
        forgetPassword.setOnClickListener(this);
        login.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.login) {
           //sendRequestWithHttpURLConnection();
            socketConnection();
           // Intent intent = new Intent(LoginActivity.this, MainActivity.class);
           // startActivity(intent);
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

                    JSONObject  obj = new JSONObject();
                    obj.put("username", "admin");
                    obj.put("password", "123456");

                    DataOutputStream out=new DataOutputStream(connection.getOutputStream());
                    out.writeBytes(obj.toString());
                    //  out.writeBytes("userName=admin&password=123456");
                    //   System.out.println(obj.toString());
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
                responseText.setText(response);
               //System.out.println(response);
            }
        });
    }

    public void socketConnection() {
        UserName = username.getText().toString();
        PassWord = password.getText().toString();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                            detectDiskWrites().detectNetwork().penaltyLog().build());
                    Socket socket = new Socket();
                    SocketAddress address = new InetSocketAddress("localhost", 3306);
                    try {
                        socket.setKeepAlive(true);
                        SocketAddress remoteAddr=new InetSocketAddress("192.168.56.1",6100);
                        socket.connect(remoteAddr, 100);
                    } catch ( IOException e) {
                        Toast.makeText(LoginActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));//接收信息
                    PrintWriter os = new PrintWriter(socket.getOutputStream());

                    JSONObject obj = new JSONObject();
                    obj.put("username", UserName);
                    obj.put("password", PassWord);
                    obj.put("request", "login");

                    //建立通道， print writer用来向服务器发信息
                    os.println(obj.toString());
                    os.flush();
                    String state = is.readLine();
                    //is用来收信息  存在问题，如果服务器不发信息怎么办！！！！！！！！！！！！！！！！！！！！
                    JSONTokener jsonParser = new JSONTokener(state);
                    JSONObject person = (JSONObject)jsonParser.nextValue();
                    String token = person.getString("result");


                    if( token.equals("true")){
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        //intent.putExtra("mainid",tokenizer.nextToken());
                        startActivity(intent);
                    }else if(token.equals("false")) {
                        Toast.makeText(LoginActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                    }else if(token.equals("notexist")) {
                        Toast.makeText(LoginActivity.this, "用户名不存在", Toast.LENGTH_SHORT).show();
                    }
                }catch (IOException e) {
                    Toast.makeText(LoginActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                catch  (Exception e2) {
                }
            }
        }).start();
    }

    private void parseJSONWithJSONObject(String jsonData){
        try{
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String state = jsonObject.getString("state");
                if( state.equals("true")){
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    //intent.putExtra("mainid",tokenizer.nextToken());
                    startActivity(intent);
                }else if(state.equals("false")) {
                    Toast.makeText(LoginActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                }else if(state.equals("notexist")) {
                    Toast.makeText(LoginActivity.this, "用户名不存在", Toast.LENGTH_SHORT).show();
                }
                //String username = jsonObject.getString("username");
                //String password = jsonObject.getString("password");
                //Log.d("LoginActivity", "username is" + username);
                //Log.d("LoginActivity", "password is" + password);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

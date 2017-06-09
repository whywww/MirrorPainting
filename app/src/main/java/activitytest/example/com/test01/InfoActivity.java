package activitytest.example.com.test01;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;


public class InfoActivity extends AppCompatActivity implements View.OnClickListener{
    private Button signout, edit;
    private TextView username, gender,email, region, info;
    String UserName = "";
    String Gender = "";
    String Region = "";
    String Email = "";
    String Info = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        signout = (Button) findViewById(R.id.signout);
        edit = (Button) findViewById(R.id.edit);
        username = (TextView) findViewById(R.id.username);
        gender = (TextView) findViewById(R.id.gender);
        region = (TextView) findViewById(R.id.region);
        email = (TextView) findViewById(R.id.email);
        info = (TextView) findViewById(R.id.info);

        //获取用户名
        Intent intent =getIntent();
        UserName = intent.getStringExtra("username");

        signout.setOnClickListener(this);
        edit.setOnClickListener(this);

        connectServerAndSave();
    }

    public void onClick(View v){
        if(v.getId() == R.id.signout){//注销
            Intent intent1 = new Intent(InfoActivity.this, LoginActivity.class);
            startActivity(intent1);
        }
        if(v.getId() == R.id.edit){//编辑个人信息
            Intent intent2 = new Intent(InfoActivity.this, EditInfoActivity.class);
            intent2.putExtra("username",UserName);
            startActivity(intent2);
        }
    }

    public void connectServerAndSave(){

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
                        Toast.makeText(InfoActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));//接收信息
                    PrintWriter os = new PrintWriter(socket.getOutputStream());

                    JSONObject obj = new JSONObject();
                    obj.put("username", UserName);
                    obj.put("request", "getInfo");

                    //建立通道， print writer用来向服务器发信息
                    os.println(obj.toString());
                    os.flush();
                    String state = is.readLine();
                    //is用来收信息  存在问题，如果服务器不发信息怎么办！！！！！！！！！！！！！！！！！！！！
                    JSONTokener jsonParser = new JSONTokener(state);
                    JSONObject person = (JSONObject)jsonParser.nextValue();
                    String result = person.getString("result");
                    Gender = person.getString("gender");
                    Region = person.getString("region");
                    Email = person.getString("email");
                    Info = person.optString("info");

                    if( result.equals("true")){//查询成功
                        username.setText(UserName);
                        gender.setText(Gender);
                        region.setText(Region);
                        email.setText(Email);
                        info.setText(Info);
                    }else if(result.equals("false")) {
                        Toast.makeText(InfoActivity.this, "查询失败", Toast.LENGTH_SHORT).show();
                    }
                }catch (IOException e) {
                    Toast.makeText(InfoActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                catch  (Exception e2) {
                }
            }
        }).start();}

}

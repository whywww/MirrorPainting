package activitytest.example.com.test01;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.jar.Manifest;

public class EditInfoActivity extends BaseActivity implements View.OnClickListener {
    private Button save;
    private EditText region, email, info;
    private RadioButton girl, boy;
    String Gender = "";
    String Region = "";
    String Email = "";
    String Info = "";
    String username = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        save = (Button) findViewById(R.id.save);
        region = (EditText) findViewById(R.id.region);
        email = (EditText) findViewById(R.id.email);
        info =(EditText) findViewById(R.id.info);
        girl = (RadioButton) findViewById(R.id.girl);
        boy = (RadioButton) findViewById(R.id.boy);
        RadioGroup group = (RadioGroup)this.findViewById(R.id.radioGroup);

        Intent intent =getIntent();
        username = intent.getStringExtra("username");

        save.setOnClickListener(this);

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(R.id.girl == checkedId){
                    Gender = "girl";
                }
                else if(R.id.boy == checkedId){
                    Gender = "boy";
                }
            }
        });
    }

    public void onClick(View v){
        if(v.getId() == R.id.save){
            //保存数据至服务器
            connectServerAndSave();
        }
    }

    public void connectServerAndSave(){
        Region = region.getText().toString();
        Email = email.getText().toString();
        Info = info.getText().toString();

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
                        Toast.makeText(EditInfoActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));//接收信息
                    PrintWriter os = new PrintWriter(socket.getOutputStream());

                    JSONObject obj = new JSONObject();
                    obj.put("username", username);
                    obj.put("gender", Gender);
                    obj.put("region", Region);
                    obj.put("email", Email);
                    obj.put("info", Info);
                    obj.put("request", "edit");

                    //建立通道， print writer用来向服务器发信息
                    os.println(obj.toString());
                    os.flush();
                    String state = is.readLine();
                    //is用来收信息  存在问题，如果服务器不发信息怎么办！！！！！！！！！！！！！！！！！！！！
                    JSONTokener jsonParser = new JSONTokener(state);
                    JSONObject person = (JSONObject)jsonParser.nextValue();
                    String result = person.getString("result");

                    if( result.equals("true")){
                        Intent intent = new Intent(EditInfoActivity.this, InfoActivity.class);
                        intent.putExtra("username",username);
                        startActivity(intent);
                    }else if(result.equals("false")) {
                        Toast.makeText(EditInfoActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                    }
                }catch (IOException e) {
                    Toast.makeText(EditInfoActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                catch  (Exception e2) {
                }
            }
        }).start();
    }

}

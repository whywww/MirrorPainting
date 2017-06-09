package activitytest.example.com.test01;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText username, password, password2, email, region;
    private Button signup;
    //TextView responseText;
    private InputMethodManager in;
    String PassWord = "";
    String PassWord2 = "";
    String UserName = "";
    String Gender = "";
    String Email = "";
    String Region = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        username = (EditText) findViewById(R.id.username_edit);
        password = (EditText) findViewById(R.id.password_edit);
        password2 = (EditText) findViewById(R.id.password2_edit);
        email = (EditText) findViewById(R.id.email_edit);
        region = (EditText) findViewById(R.id.region_edit);
        signup = (Button) findViewById(R.id.signup);
        //responseText=(TextView)findViewById(R.id.response_text);
        RadioGroup group = (RadioGroup)this.findViewById(R.id.radioGroup);

        in = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

        signup.setOnClickListener(this);
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

    public void onClick(View v) {
        if (v.getId() == R.id.signup) {
            UserName = username.getText().toString();
            PassWord = password.getText().toString();
            PassWord2 = password2.getText().toString();
            Email = email.getText().toString();
            Region = region.getText().toString();
                //设置sign up点击事件
            if(checkRegisterInfoLegal()){ // 注册符合要求
                // 传递信息给服务器
                in.hideSoftInputFromWindow(v.getWindowToken(), 0);//隐藏输入法键盘
                //sendRequestWithHttpURLConnection();
                socketConnection();
                //跳转到主活动
                //Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                //startActivity(intent);
                //signup(UserName, PassWord);

            }
        }

    }

    // 判断用户注册信息是否合法
    public boolean checkRegisterInfoLegal(){
        if(TextUtils.isEmpty(UserName)){
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(PassWord)){
            password.setError("密码不能为空");
            return false;
        }

        if(TextUtils.isEmpty(PassWord2)){
            password2.setError("确认密码不能为空");
            return false;
        }
        if(!PassWord.equals(PassWord2)){
            Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
            return false;
        }
        //检测是否有相同用户名
        else return true;
    }

    //使用http协议连接
    private void sendRequestWithHttpURLConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection=null;
                BufferedReader reader=null;
                try{

                    URL url=new URL("http://139.199.162.139:5000/login");//服务器URL
                    connection=(HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(80000);
                    connection.setReadTimeout(80000);

                    JSONObject obj = new JSONObject();
                    obj.put("username", UserName);
                    obj.put("password", PassWord);

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
                    //showResponse(response.toString());

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
                //responseText.setText(response);
                //System.out.println(response);
            }
        });
    }

    //使用socket连接
    private void socketConnection(){
                try {
                        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().
                                detectDiskWrites().detectNetwork().penaltyLog().build());

                        Socket socket  = new Socket("192.168.56.1",6100);

                        BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter os = new PrintWriter(socket.getOutputStream());
                        BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));

                        JSONObject obj = new JSONObject();
                        obj.put("username", UserName);
                        obj.put("password", PassWord);
                        obj.put("gender", Gender);
                        obj.put("email", Email);
                        obj.put("region", Region);
                        obj.put("request", "register");

                        os.println(obj.toString());
                        os.flush();
                        String state = is.readLine();
                        System.out.println(state);
                        //is用来收信息  存在问题，如果服务器不发信息怎么办！！！！！！！！！！！！！！！！！！！！
                        JSONTokener jsonParser = new JSONTokener(state);
                        JSONObject person = (JSONObject)jsonParser.nextValue();
                        String token = person.getString("result");
                        System.out.println(token);

                        if( token.equals("true")){
                            Toast.makeText(SignUpActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                            socket.close();
                            finish();
                        }else if(token.equals("false")) {
                            Toast.makeText(SignUpActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                        }else if(token.equals("notexist")) {
                            Toast.makeText(SignUpActivity.this, "用户名不存在", Toast.LENGTH_SHORT).show();
                        }

                }catch(SocketException e){
                    Toast.makeText(SignUpActivity.this, "服务器连接失败", Toast.LENGTH_SHORT).show();
                    System.out.println("Error:" + e);
                } catch (Exception e2) {
                    System.out.println("Error:"+e2);
                }
            }


}
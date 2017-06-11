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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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


public class InfoActivity extends BaseActivity implements View.OnClickListener{
    private Button signout, edit;
    private TextView username, gender,email, region, info;
    private ImageView picture;
    private Uri imageUri;
    String UserName = "";
    String Gender = "";
    String Region = "";
    String Email = "";
    String Info = "";
    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;




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
        picture = (ImageView) findViewById(R.id.head);
        ImageButton takePhoto = (ImageButton) findViewById(R.id.take_photo);
        ImageButton chooseFromAlbum = (ImageButton) findViewById(R.id.choose_from_album);

        //获取用户名
        Intent intent =getIntent();
        UserName = intent.getStringExtra("username");

        signout.setOnClickListener(this);
        edit.setOnClickListener(this);
        takePhoto.setOnClickListener(this);
        chooseFromAlbum.setOnClickListener(this);

        connectServerAndSave();
    }

    public void onClick(View v){
        if(v.getId() == R.id.signout){//注销
            Intent intent1 = new Intent("example.com.test01.FORCE_OFFLINE");
            sendBroadcast(intent1);
        }
        if(v.getId() == R.id.edit){//编辑个人信息
            Intent intent2 = new Intent(InfoActivity.this, EditInfoActivity.class);
            intent2.putExtra("username",UserName);
            startActivity(intent2);
        }
        if(v.getId() == R.id.take_photo){
            //创建File对象，用于存储拍照后的图片
            File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
            try{
                if (outputImage.exists()){
                    outputImage.delete();
                }outputImage.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
            }
            if (Build.VERSION.SDK_INT >= 24){
                imageUri = FileProvider.getUriForFile(InfoActivity.this, "activitytest.example.com.test01.fileprovider", outputImage);
            }else{
                imageUri = Uri.fromFile(outputImage);
            }
            //启动相机程序
            Intent intent = new Intent("android media.action.IMAGE_CAPTURE");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, TAKE_PHOTO);
        }
        if(v.getId() == R.id.choose_from_album){
            if (ContextCompat.checkSelfPermission(InfoActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(InfoActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }else{openAlbum();}
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case TAKE_PHOTO:
                if (requestCode == RESULT_OK){
                    try{
                        //将拍摄的照片显示出来
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        picture.setImageBitmap(bitmap);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if(resultCode == RESULT_OK){
                    //判断手机系统版本号
                    if(Build.VERSION.SDK_INT >=19){
                        handleImageOnKitKat(data);
                    }else{
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void openAlbum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);//打开相册
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case 1:
                if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){openAlbum();}
                else{
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data){
        String imagePath = null;
        Uri uri = data.getData();
        if(DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            imagePath = getImagePath(uri, null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            imagePath = uri.getPath();
        }
        displayImage(imagePath);
    }

    private void handleImageBeforeKitKat(Intent data){
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection){
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if(cursor != null){
            if (cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;

    }

    private void displayImage(String imagePath){
        if(imagePath != null){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            picture.setImageBitmap(bitmap);
        }else{
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

}

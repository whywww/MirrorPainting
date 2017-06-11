package activitytest.example.com.test01;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RadioButton;
import android.app.Activity;


import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends BaseActivity{

    private Image[] images = {new Image("pic1.jpg", R.drawable.pic1, "This is my first pic!"),
            new Image("pic2.jpg", R.drawable.pic2, "This is the second pic"),
            new Image("pic3.jpg", R.drawable.pic3, "This is the third pic"),
            new Image("pic4.jpg", R.drawable.pic4, "This is the forth pic"),
            new Image("pic5.jpg", R.drawable.pic5, "This is the fifth pic")};

    private List<Image> imageList = new ArrayList<>();
    private ImageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        setSupportActionBar(toolbar);

        initImages();

        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);//每行一列数据
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ImageAdapter(imageList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(MainActivity.this, position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initImages(){
        imageList.clear();
        imageList.add(images[0]);
        imageList.add(images[1]);
        imageList.add(images[2]);
        imageList.add(images[3]);
        imageList.add(images[4]);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent intent =getIntent();
        String username = intent.getStringExtra("username");

        switch (item.getItemId()){
            case R.id.newPic:
                Toast.makeText(this, "new a pic", Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(MainActivity.this, EditActivity.class);
                intent1.putExtra("username",username);
                startActivity(intent1);
                break;

            case R.id.info:
                Intent intent2 = new Intent(MainActivity.this, InfoActivity.class);
                intent2.putExtra("username",username);
                startActivity(intent2);
                break;
            default:
        }
        return true;
    }
}

package activitytest.example.com.test01;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.widget.Button;
import android.widget.RadioButton;
import android.app.Activity;


import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private CardView cardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardView = (CardView)findViewById(R.id.cardView);

        cardView.setRadius(8);//设置图片圆角的半径大小
        cardView.setCardElevation(8);//设置阴影部分大小
        cardView.setContentPadding(5,5,5,5);//设置图片距离阴影大小

        cardView.setOnClickListener(this);

    }

    public void onClick(View v) {
        if (v.getId() == R.id.cardView) {

        }
    }

}

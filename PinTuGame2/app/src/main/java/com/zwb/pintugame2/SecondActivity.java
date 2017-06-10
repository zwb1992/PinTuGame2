package com.zwb.pintugame2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.zwb.pintugame2.view.PinTuView2;

public class SecondActivity extends AppCompatActivity {
    private PinTuView2 pt2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        pt2 = (PinTuView2) findViewById(R.id.pt2);
        pt2.setCallBack(new PinTuView2.CallBack() {
            @Override
            public void gameOver() {

            }

            @Override
            public void completed() {
                Toast.makeText(SecondActivity.this, "恭喜你完成拼图", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

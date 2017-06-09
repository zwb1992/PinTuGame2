package com.zwb.pintugame2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.zwb.pintugame2.view.PinTuView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private PinTuView gl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gl = (PinTuView) findViewById(R.id.gl);
        gl.setCallBack(new PinTuView.CallBack() {
            @Override
            public void gameOver() {

            }

            @Override
            public void completed() {
                Toast.makeText(MainActivity.this, "恭喜你完成拼图", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void resetRowColumn(View view) {
        int row = new Random().nextInt(3) + 3;
        gl.setRowColumn(row, row);
    }

    public void otherImpl(View view) {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }


}

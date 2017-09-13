package com.viclee.verticalswitchtextview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_maina);

      /*  ArrayList<String> list = new ArrayList<String>(
                Arrays.asList(
                "能够适应多行长文本的Android TextView的例子.理解的也很简单，我只是列出这个库里面我用到的一些方法理解的也很简单，我只是列出这个库里面我用到的一些方法",
                "理解的也很简单，我只是列出这个库里面我用到的一些方法",
                "适当放松放理解的也很简单，我只是列出这个库里面我用到的一些方法",
                "破解密钥理解的也很简单，我只是列出这个库里面我用到的一些方法",
                "实现了两种方式来检测Android app前后台切换的状态理解的也很简单，我只是列出这个库里面我用到的一些方法",
                "科比理解的也很简单，我只是列出这个库里面我用到的一些方法"));*/
        ArrayList<String> list = new ArrayList<String>(
                Arrays.asList(
                        "1开始1-2-3-4-5-6-7-8-9-10-11-12-13-14-15-16-17-18-19-20-21-22-23-24-25-26-27-28-29-30end",
                        "2开始1-2-3-4-5-end"
                ));
        VerticalSwitchTextView verticalSwitchTextView1 = (VerticalSwitchTextView) findViewById(R.id.vertical_switch_textview1);
//        VerticalSwitchTextView verticalSwitchTextView2 = (VerticalSwitchTextView) findViewById(R.id.vertical_switch_textview2);
        verticalSwitchTextView1.setCbInterface(new VerticalSwitchTextView.VerticalSwitchTextViewCbInterface() {
            @Override
            public void showNext(int index) {

            }

            @Override
            public void onItemClick(int index) {

            }
        });
       /* verticalSwitchTextView2.setCbInterface(new VerticalSwitchTextView.VerticalSwitchTextViewCbInterface() {
            @Override
            public void showNext(int index) {

            }

            @Override
            public void onItemClick(int index) {

            }
        });*/
        verticalSwitchTextView1.setTextContent(list);
//        verticalSwitchTextView2.setTextContent(list);
    }
}

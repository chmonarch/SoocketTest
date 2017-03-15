package com.example.administrator.soockettest;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity {

    private static final int MESSAGE_CONNTIONED = 0;
    private static final int MESSAGE_NEW_MESSAGE = 1;
    Socket socket = null;
    private PrintWriter mPrintWriter;
    private TextView tv;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, SocketService.class));
        tv = (TextView) findViewById(R.id.tv);
        editText = (EditText) findViewById(R.id.et);
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_CONNTIONED:
                    Toast.makeText(MainActivity.this, "已经连接上了", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_NEW_MESSAGE:
                    tv.setText(tv.getText() + "\n" + msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void action(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (socket == null) {
                    try {
                        socket = new Socket("localhost", 8688);
                        mPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                        mHandler.sendEmptyMessage(MESSAGE_CONNTIONED);
                        Log.e("tag", "conntion success");
                    } catch (IOException e) {
                        e.printStackTrace();
                        SystemClock.sleep(1000);
                        Log.e("tag", "conntion field");
                    }
                }

                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    while (!MainActivity.this.isFinishing()){
                        Log.e("tag","等待服务端出传入数据");
                        String str = bufferedReader.readLine();
                        Log.e("tag","读取到服务端传来的数据"+str);
                        if (!TextUtils.isEmpty(str)){
                            mHandler.obtainMessage(MESSAGE_NEW_MESSAGE,str).sendToTarget();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    public void getMessage(View view) {
        try {
            final String str = editText.getText().toString();
            if (!TextUtils.isEmpty(str) && mPrintWriter != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mPrintWriter.println(str);
                        Log.e("tag", "发送了" + str);
                    }
                }).start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }


}

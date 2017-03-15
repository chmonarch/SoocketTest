package com.example.administrator.soockettest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * Created by ${lida} on 2017/3/15.
 */
public class SocketService extends Service {
    private boolean mIsServiceIsDestoryed = false;
    private String[] messages = new String[]{
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        new Thread(new TcpService()).start();
        super.onCreate();
    }

    private class TcpService implements Runnable {
        ServerSocket serverSocket = null;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8688);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            while (!mIsServiceIsDestoryed) {
                try {
                    final Socket client = serverSocket.accept();
                    Log.e("tag", "accept");
                    new Thread() {
                        @Override
                        public void run() {
                            responseClient(client);
                        }
                    }.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void responseClient(Socket client) {
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);
            out.println("欢迎来到聊天室!");
            Log.e("tag", "欢迎来到聊天室");
            while (!mIsServiceIsDestoryed) {
                Log.e("tag", "等待读取数据");
                String s = in.readLine();
                Log.e("tag", "读取到 =" + s);
                if (s == null) {
                    break;
                }
                int i = new Random().nextInt(messages.length);
                out.println(messages[i]);
                Log.e("tag", "send" + messages[i]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
            try {
                assert in != null;
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        mIsServiceIsDestoryed = true;
        super.onDestroy();
    }
}

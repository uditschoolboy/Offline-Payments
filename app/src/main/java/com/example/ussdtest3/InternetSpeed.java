package com.example.ussdtest3;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;


public class InternetSpeed {
    private final OkHttpClient client = new OkHttpClient();
    private String TAG = this.getClass().getSimpleName();
    private long startTime;
    private long endTime;
    private long fileSize;

    InternetSpeed(){

    }
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    public ArrayList<Integer> getInternetSpeed(){
//        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo netInfo = cm.getActiveNetworkInfo();
//        //should check null because in airplane mode it will be null
//        NetworkCapabilities nc = null;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//            nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
//        }
//        int downSpeed = nc.getLinkDownstreamBandwidthKbps();
//        int upSpeed = nc.getLinkUpstreamBandwidthKbps();
//
//        ArrayList<Integer> speed = new ArrayList<Integer>();
//        speed.add(downSpeed);
//        speed.add(upSpeed);
//        return speed;
//    }
    public double downSpeed;
    public double getDownloadSpeed(){
        String myurl="https://upload.wikimedia.org/wikipedia/commons/thumb/5/5b/HTTP_logo.svg/768px-HTTP_logo.svg.png";
        Request request = new Request.Builder()
                .url(myurl) // replace image url
                .build();

        startTime = System.currentTimeMillis();
        System.out.println("starttime: "+startTime);


        final CountDownLatch latch = new CountDownLatch(1);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                latch.countDown();
            }


            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Headers responseHeaders = response.headers();
                for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                    System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }

                InputStream input = response.body().byteStream();

                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];

                    while (input.read(buffer) != -1) {
                        bos.write(buffer);
                    }
                    byte[] docBuffer = bos.toByteArray();
                    fileSize = bos.size();

                } finally {
                    input.close();
                }

                endTime = System.currentTimeMillis();

                // calculate how long it took by subtracting endtime from starttime

                final double timeTakenMills = Math.floor(endTime - startTime);  // time taken in milliseconds
                final double timeTakenInSecs = timeTakenMills / 1000;  // divide by 1000 to get time in seconds
                final int kilobytePerSec = (int) Math.round(1024 / timeTakenInSecs);
                final double speed = Math.round(fileSize / timeTakenMills);

                System.out.println("Time taken in secs: " + timeTakenInSecs);
                System.out.println("Kb per sec: " + kilobytePerSec);
                System.out.println("Download Speed: " + speed);
                System.out.println("File size in kb: " + fileSize);
                downSpeed=speed;
                latch.countDown();
            }
        });
        try {
            latch.await();
            return downSpeed;
        } catch (InterruptedException e) {
            return 0;
        }
    }
}

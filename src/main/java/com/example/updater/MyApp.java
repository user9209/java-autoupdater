package com.example.updater;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MyApp {

    public static final int version = 20240624;
    //public static final int version = 20240623;
    public static int newVersion = -1;

    public static void main(String[] args) throws IOException {

        OkHttpClient client = new OkHttpClient.Builder().build();

        if(checkUpdate(client)) {
            runUpdate(client);
        }

    }

    private static void runUpdate(OkHttpClient client) throws IOException {
        Request request = new Request.Builder()
                .url("https://raw.githubusercontent.com/user9209/java-autoupdater/master/_releases_/app_" + newVersion + ".jar")
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if(response.code() != 200) {
            System.err.println("Download failed! " + response.code());
            return;
        }

        FileOutputStream fos = new FileOutputStream("app_" + newVersion + ".jar");
        InputStream is = response.body().byteStream();
        is.transferTo(fos);
        fos.flush();
        fos.close();
        is.close();

    }

    private static boolean checkUpdate(OkHttpClient client) {
        try {
            Request request = new Request.Builder()
                    .url("https://raw.githubusercontent.com/user9209/java-autoupdater/master/_releases_/latest")
                    .build();

            Call call = client.newCall(request);
            Response response = call.execute();

            if(response.code() != 200) {
                return false;
            }

            int version = Integer.parseInt(response.body().string());

            newVersion = version;

            return version > MyApp.version;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

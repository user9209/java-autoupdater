package com.example.updater;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class MyApp {

    public static final int version = 20240624;
    //public static final int version = 20240623;
    public static int newVersion = -1;

    public static void main(String[] args) throws IOException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {

        OkHttpClient client = new OkHttpClient.Builder().build();

        if(checkUpdate(client)) {
            runUpdate(client);
        }

        System.out.println("Runs " + version);
    }

    private static void runUpdate(OkHttpClient client) throws IOException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        Request request = new Request.Builder()
                .url("https://raw.githubusercontent.com/user9209/java-autoupdater/master/_releases_/app_" + newVersion + ".jar")
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if(response.code() != 200) {
            System.err.println("Download failed! " + response.code());
            return;
        }

        // todo: verify signature
        byte[] signature = downloadSignature(client);
        Signature signEng = Signature.getInstance("SHA256withRSA");
        signEng.initVerify(getPublicKey());

        FileOutputStream fos = new FileOutputStream("app_" + newVersion + ".jar");
        InputStream is = response.body().byteStream();
        MessageDigest sha3_512 = MessageDigest.getInstance("SHA3-512");
        DigestInputStream dis = new DigestInputStream(is,sha3_512);
        dis.transferTo(fos);
        fos.flush();
        fos.close();
        is.close();
        dis.close();

        signEng.update(sha3_512.digest());
        if(!signEng.verify(signature)) {
            Files.delete(Path.of("app_" + newVersion + ".jar"));
        }
    }

    private static PublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String publicKey = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAjNDHQtrtIOKGycIcPCP9gZEVwYHoziI5UdpHeELjOVk2vchUQtCzv/EgPKVY1lSMC864Y81KV0yK5zW2wHCsAb2E3CWa6EBx37bj/6jS5dQNZlJdVTEvWwPAaE9yhVvyilRjQ8TubzxOOsLTE0jHIPlB7sqlh2ZTQho9mjsiBD4E+EJwrMyc1+mjaemRYl4a7kxD4BRTOOSju3FWpKKHtSlrj0xADP8JWjthJkLZNTa+V3DfssJml/gtPTmUfF6l2tG6BspBb8xya4UlOEa0tfmgxJGVxY1+i7Cd1JbZ48j0HauSzWDdvha6uwHA/ApZNGXQR2TRNl4C/cUPYeh49h+KxUZL6xTlo5yIRF8kpV1OMeV7B3CV5p3C5C5pJnV5SY2sSrilNb7BcFa+GaySUIeY4MKMJcRbRhYs2CV6ChEffQZUbhdn+ap7iOdKNqVQ61ns2WL9uiFNk/6tBInSc9YmkAoWGGXZms0PLgucDbmhV73OsOPRrhvEgqFgnuGiaWc9B/wLVWgLjkZq2ZvOJwZoXsJxDa155NCXBBYUMW416lOsNCA+E3IMCswHpcv1VbzDKuXLY6BMVAUIlV/BmqrEQMC7basDa3NdBi3HNFekNVyLx7updr96Ig5CMr5SiLAwv+Dr90cStTIkdcUOvi18SkjzhDCXdCSiCz9tbPsCAwEAAQ";

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    private static byte[] downloadSignature(OkHttpClient client) throws IOException {
        Request request = new Request.Builder()
                .url("https://raw.githubusercontent.com/user9209/java-autoupdater/master/_releases_/app_" + newVersion + ".jar.sig")
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();

        if(response.code() != 200) {
            System.err.println("Download failed! " + response.code());
            return new byte[0];
        }
        return response.body().bytes();
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

    public static PublicKey loadPublicKey(String base64PublicKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    public static boolean verifySignature(PublicKey publicKey, byte[] data, byte[] signatureBytes) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(signatureBytes);
    }
}

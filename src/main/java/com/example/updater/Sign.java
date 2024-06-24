package com.example.updater;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.Signature;

public class Sign {
    public static byte[] sign(PrivateKey privateKey, byte[] data) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    public static byte[] sign(PrivateKey privateKey, InputStream is) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);

        byte[] buf = new byte[4096];
        int l;
        while ((l = is.read(buf)) > 0) {
            signature.update(buf);
        }
        return signature.sign();
    }
}

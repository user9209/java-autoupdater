package com.example.updater;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

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

        MessageDigest sha3_512 = MessageDigest.getInstance("SHA3-512");
        DigestInputStream dis = new DigestInputStream(is,sha3_512);

        byte[] buf = new byte[4096];
        int l;
        while ((l = dis.read(buf)) > 0) {
           // signature.update(buf,0,l);
            // ignore data
        }
        dis.close();

        signature.update(sha3_512.digest());

        return signature.sign();
    }

    public static void main(String[] args) throws Exception {
        PrivateKey privateKey;

        if(!Files.exists(Path.of("prvkey.b64"))) {

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");

            // Initialize the KeyPairGenerator with the key size (e.g., 2048 bits)
            keyGen.initialize(4096);

            // Generate the KeyPair (public and private keys)
            KeyPair keyPair = keyGen.generateKeyPair();

            // Extract the Public and Private Keys from the KeyPair
            PublicKey publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();

            String publicKeyB64 = Base64.getEncoder().withoutPadding().encodeToString(publicKey.getEncoded());
            String privateKeyB64 = Base64.getEncoder().withoutPadding().encodeToString(privateKey.getEncoded());

            // Print the keys in Base64 encoding
            System.out.println("Public Key: " + publicKeyB64);
            System.out.println("Private Key: " + privateKeyB64);

            Files.writeString(Path.of("pubkey.b64"), publicKeyB64);
            Files.writeString(Path.of("prvkey.b64"), privateKeyB64);
        }
        else {
            privateKey = loadPrivateKey(Files.readString(Path.of("prvkey.b64")));
        }

        FileInputStream fis = new FileInputStream("_releases_/app_20240626.jar");
        byte[] sign = sign(privateKey,fis);
        fis.close();

        Files.write(Path.of("_releases_/app_20240626.jar.sig"), sign);
    }

    public static PrivateKey loadPrivateKey(String base64PublicKey) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }
}

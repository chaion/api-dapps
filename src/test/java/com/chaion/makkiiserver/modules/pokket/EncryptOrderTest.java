package com.chaion.makkiiserver.modules.pokket;

import com.chaion.makkiiserver.modules.pokket.security.AesProvider;
import com.chaion.makkiiserver.modules.pokket.security.CipherHelper;
import com.chaion.makkiiserver.modules.pokket.security.utils.RandomString;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class EncryptOrderTest {

    @Test
    public void testEncryptOrder() throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException {
        AesProvider aesProvider = new AesProvider();
        System.out.println("ssss");
        String keyString = aesProvider.createKey();
        System.out.println("2");
//        String input = "{\"product_id\":38350,\"amount\":10,\"return_address\":\"0xd2e7e98d0f951877311553b7a3b43f040a550761\",\"transaction_id\":\"0x9fa5ea174560a6bfc596d8c9044defef75162107a7b97db92609f3701ce8f27d\",\"create_time\":\"2019-08-26T11:23:19.364Z\",\"order_id\":\"201908261123173342325\"}";
        String input = "leo";
        byte[] bytes = aesProvider.encrypt(input);
        System.out.println("3");
        String encrypted = CipherHelper.toString(bytes);
        System.out.println("deposit_cipher: " + encrypted);
//        RsaProvider rsaProvider = new RsaProvider();
//        ClassPathResource classPathResource = new ClassPathResource("pokket_test_pk.pem");
//        InputStream stream = classPathResource.getInputStream();
//        rsaProvider.loadPemFile(stream);
//
//        System.out.println("key_hash" + CipherHelper.toString(rsaProvider.encrypt(keyString)));
    }

    @Test
    public void testRandomString() throws NoSuchAlgorithmException {
        RandomString r = new RandomString(256);
        System.out.println(r.generateString());
    }
}

package com.chaion.makkiiserver.modules.pokket;

import com.chaion.makkiiserver.blockchain.BlockchainException;
import com.chaion.makkiiserver.blockchain.aion.AionService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigInteger;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AionServiceTest {

    @Autowired
    AionService aionService;

    @Test
    public void sendTransaction() throws BlockchainException {
        String privateKey = "0x0e7ff5ec5e46ba057e0a9cfcb610a8560260fbfc2db92b08faba826dacac485eb6a639de25c0ca499d4161409b02f3a6e0ed0555bdbd56017f5fc9f3ce34e3d0";
        String address = "0xa0ada7e2aff49daec0dfaf16ad062ce2514941f9848a04156445ae5bd17740b0";
        BigInteger amount = BigInteger.valueOf(1l * 1000000000);
        String txHash = aionService.sendTransaction(privateKey, address, amount);
        System.out.println("txHash:" + txHash);
        Assert.assertNotNull(txHash);
    }
}

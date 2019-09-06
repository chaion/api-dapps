package com.chaion.makkiiserver.modules.pokket;

import com.chaion.makkiiserver.blockchain.btc.BtcService;
import com.chaion.makkiiserver.blockchain.btc.BtcTransaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BtcServiceTest {

    @Autowired
    BtcService btcService;

    @Test
    public void getTx() {
        BtcTransaction tx = btcService.getTransaction("56f522990775e8c02f3d33a943e6004c4fe40f856eeb3c0a4fdf34312dec56bd");
        System.out.println(tx);
    }
}

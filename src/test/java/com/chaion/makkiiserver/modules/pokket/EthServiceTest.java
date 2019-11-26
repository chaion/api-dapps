package com.chaion.makkiiserver.modules.pokket;

import com.chaion.makkiiserver.blockchain.BlockchainException;
import com.chaion.makkiiserver.blockchain.eth.EthService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EthServiceTest {

    @Autowired
    EthService ethService;

    @Test
    public void validateEthTx() {
        String txHash = "0x307773266c8182f134daa40a8924caafd3d1e297ca168f19a913b6363077de05";
        String from = "0xfe5a44605eed83dae7e2ca1a83f84ed61ce38dcd";
        String to = "0xdd6c48d56bab7c0cd5ce2517f9d4100e12bf8474";
        BigDecimal amount = new BigDecimal("0");
        Assert.assertTrue(ethService.validateEthTx(txHash, from, to, amount, (a1, a2) -> true));
    }

    @Test
    public void validateEthTx2() {
        String txHash = "0x8d43f98a399894e175aff9091d3d850fd5aa208ec0ac925400fdce8aad140d49";
        String from = "0x4f56279cfefea851bd359522f0c323a055f748a8";
        String to = "0xcfb5896a29820a4571cff44f2a31ac77a419e616";
        BigDecimal amount = new BigDecimal("0.99979");
        Assert.assertTrue(ethService.validateEthTx(txHash, from, to, amount, (a1, a2) -> true));
    }

    @Test
    public void validateERC20Tx() {
        String txHash = "0x3c76eb642257beaa9bc54bc0bc5812028b3db6bec6f3bcbec561923f8c3bf916";
        String from = "0x8b2f2e7c4e7fbfed72dcdedc853735e301331980";
        String to = "0x4be272f6ffcb20d7f427ae4a4583aa31e4a32a1c";
        String token = "DAI";
        BigDecimal amount = new BigDecimal("13.85715328");
        Assert.assertTrue(ethService.validateERC20Transaction(txHash, from, to, token, amount, null));
    }

    @Test
    public void testDate() {
        System.out.println(new Date().toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME));
        System.out.println(new Date().toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_INSTANT));
        System.out.println(new Date().toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

}

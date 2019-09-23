package com.chaion.makkiiserver.modules.pokket;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PokketUtilTest {

    @Test
    public void testCollateralTest() {
        BigDecimal result = PokketUtil.calculateCollateral(new BigDecimal("12"), new BigDecimal("0.200934228"), new BigDecimal("0.2088949522891436"));
                System.out.println(result);
    }
}

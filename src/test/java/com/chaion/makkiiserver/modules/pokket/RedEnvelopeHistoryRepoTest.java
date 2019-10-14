package com.chaion.makkiiserver.modules.pokket;

import com.chaion.makkiiserver.modules.market_activities.red_envelope.RedEnvelopeHistoryRepo;
import com.chaion.makkiiserver.modules.market_activities.red_envelope.RedEnvelopeHsitoryStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedEnvelopeHistoryRepoTest {

    @Autowired
    RedEnvelopeHistoryRepo repo;

    @Test
    public void test() {
        List<RedEnvelopeHsitoryStatus> in = new ArrayList<>();
        in.add(RedEnvelopeHsitoryStatus.PENDING);
        in.add(RedEnvelopeHsitoryStatus.SUCCESS);
      System.out.println(repo.countByPhoneIdAndStatusIn("11111", in));
    }
}

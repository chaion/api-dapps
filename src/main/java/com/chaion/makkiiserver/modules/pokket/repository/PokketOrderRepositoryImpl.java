package com.chaion.makkiiserver.modules.pokket.repository;

import com.chaion.makkiiserver.modules.pokket.model.PokketOrder;
import com.chaion.makkiiserver.modules.pokket.model.PokketOrderStatus;
import com.chaion.makkiiserver.modules.pokket.model.QueryOrderReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;

import java.util.ArrayList;
import java.util.List;

public class PokketOrderRepositoryImpl implements PokketOrderRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<PokketOrder> findAdvanced(QueryOrderReq dynamicQuery) {
        final Query query = new Query();

        Pageable pageable = PageRequest.of(dynamicQuery.getPage(), dynamicQuery.getSize());
        query.with(pageable);

        final List<Criteria> criteria = new ArrayList<>();
        String orderId = dynamicQuery.getOrderId();
        Long pokketOrderId = dynamicQuery.getPokketOrderId();
        if (orderId != null && !orderId.isEmpty()) {
            criteria.add(Criteria.where("_id").is(orderId));
        } else if (pokketOrderId != null) {
            criteria.add(Criteria.where("pokketOrderId").is(pokketOrderId));
        } else {
            Long fromTime = dynamicQuery.getFromTime();
            if (fromTime != null) {
                criteria.add(Criteria.where("createTime").gte(fromTime));
            }
            Long toTime = dynamicQuery.getToTime();
            if (toTime != null) {
                criteria.add(Criteria.where("createTime").lte(toTime));
            }
            PokketOrderStatus status = dynamicQuery.getStatus();
            if (status != null) {
                criteria.add(Criteria.where("status").is(status));
            }
            String token = dynamicQuery.getToken();
            if (token != null) {
                criteria.add(new Criteria().orOperator(Criteria.where("token").regex(token, "i"),
                        Criteria.where("tokenFullName").regex(token, "i")));
            }
            List<String> addresses = dynamicQuery.getInvestorAddresses();
            if (addresses != null && addresses.size() > 0) {
                criteria.add(Criteria.where("investorAddress").in(addresses));
            }
        }
        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));
        }

        int count = (int) mongoTemplate.count(query, PokketOrder.class );
        List<PokketOrder> list = mongoTemplate.find(query, PokketOrder.class);
        return PageableExecutionUtils.getPage(list, pageable, () -> count);
    }
}

package com.chaion.makkiiserver.modules.pokket.repository;

import com.chaion.makkiiserver.modules.pokket.model.PokketOrder;
import com.chaion.makkiiserver.modules.pokket.model.PokketOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PokketOrderRepository extends MongoRepository<PokketOrder, String>, PokketOrderRepositoryCustom {

    Page<PokketOrder> findByInvestorAddressIn(List<String> addresses, Pageable page);

    List<PokketOrder> findByOrderIdIn(List<String> pokketOrderIds);

}

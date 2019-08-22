package com.chaion.makkiiserver.pokket.repository;

import com.chaion.makkiiserver.pokket.model.PokketOrder;
import com.chaion.makkiiserver.pokket.model.PokketOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PokketOrderRepository extends MongoRepository<PokketOrder, String> {

    Page<PokketOrder> findByInvestorAddressInAndStatusIsNot(List<String> addresses,
                                                            PokketOrderStatus status,
                                                            Pageable page);

}

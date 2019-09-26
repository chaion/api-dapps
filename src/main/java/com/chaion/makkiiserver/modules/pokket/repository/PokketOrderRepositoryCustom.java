package com.chaion.makkiiserver.modules.pokket.repository;

import com.chaion.makkiiserver.modules.pokket.model.PokketOrder;
import com.chaion.makkiiserver.modules.pokket.model.QueryOrderReq;
import org.springframework.data.domain.Page;

public interface PokketOrderRepositoryCustom {
    Page<PokketOrder> findAdvanced(QueryOrderReq query);
}

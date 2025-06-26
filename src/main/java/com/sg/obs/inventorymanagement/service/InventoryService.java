package com.sg.obs.inventorymanagement.service;

import com.sg.obs.inventorymanagement.domain.InventoryRequest;
import com.sg.obs.inventorymanagement.domain.ItemInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class InventoryService {

    private final WebClient webClient;

    public InventoryService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8878/v1/inventory").build();
    }

    public List<ItemInfo> getItemsInfo(Pageable pageable, String filter) {
//        return webClient.get()
//                .uri(uriBuilder -> uriBuilder.path("/search")
//                        .queryParam("name", filter)
//                        .queryParam("page", pageable.getPageNumber())
//                        .queryParam("size", pageable.getPageSize())
//                        .build())
//                .retrieve()
//                .bodyToFlux(ItemInfo.class)
//                .collectList()
//                .block();
        log.info("KEYWORD: {}", filter);
        return new ArrayList<>();
    }

    public void saveInventory(InventoryRequest body) {
//        webClient.post()
//                .uri("")
//                .bodyValue(body)
//                .retrieve()
//                .bodyToMono(Void.class)
//                .block();

        log.info("Saving inventory for item: {}, quantity: {},Type: {}", body.id(), body.quantity(), body.type());
    }

}

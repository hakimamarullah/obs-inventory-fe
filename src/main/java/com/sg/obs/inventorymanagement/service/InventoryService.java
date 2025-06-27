package com.sg.obs.inventorymanagement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sg.obs.base.config.WebClientLoggingFilter;
import com.sg.obs.inventorymanagement.domain.InventoryDetails;
import com.sg.obs.inventorymanagement.domain.InventoryRequest;
import com.sg.obs.inventorymanagement.domain.InventorySummary;
import com.sg.obs.itemmanagement.domain.PageWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@Slf4j
public class InventoryService {

    private final WebClient webClient;

    private final ObjectMapper mapper;

    public InventoryService(WebClient.Builder webClientBuilder, ObjectMapper mapper) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8878/v1/inventories")
                .filter(WebClientLoggingFilter.logAndHandleErrors(mapper))
                .build();
        this.mapper = mapper;
    }


    public void saveInventory(InventoryRequest body) {
        webClient.post()
                .uri("")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class)
                .block();

        log.info("Saving inventory for item: {}, quantity: {},Type: {}", body.itemId(), body.quantity(), body.type());
    }

    public Mono<Page<InventoryDetails>> getAllInventory(Pageable pageable, String filter) {
        if (!StringUtils.isBlank(filter)) {
            return getAllInventoryByItemId(pageable, filter);
        }
        return getAllInventory(pageable);
    }

    protected Mono<Page<InventoryDetails>> getAllInventory(Pageable pageable) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("")
                        .queryParam("page", pageable.getPageNumber())
                        .queryParam("size", pageable.getPageSize())
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PageWrapper<InventoryDetails>>() {
                })
                .map(PageWrapper::getData)
                .map(page -> new PageImpl<>(page.getContent(), pageable, page.getMetadata().getTotalElements()));
    }

    protected Mono<Page<InventoryDetails>> getAllInventoryByItemId(Pageable pageable, String filter) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/items/{itemId}")
                        .queryParam("page", pageable.getPageNumber())
                        .queryParam("size", pageable.getPageSize())
                        .build(filter))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PageWrapper<InventoryDetails>>() {
                })
                .map(PageWrapper::getData)
                .map(page -> new PageImpl<>(page.getContent(), pageable, page.getMetadata().getTotalElements()));
    }

    public Mono<InventorySummary> getSummaryByItemId(String itemId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/items/{itemId}/summary").build(itemId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .map(it -> mapper.convertValue(it.get("data"), InventorySummary.class));
    }
}

package com.sg.obs.itemmanagement.service;

import com.sg.obs.itemmanagement.domain.ItemDto;
import com.sg.obs.itemmanagement.domain.PageWrapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;


@Service
public class ItemService {

    private final WebClient webClient;

    public ItemService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8878/v1/items").build();
    }

    public Mono<Page<ItemDto>> getItems(Pageable pageable, String currentFilter) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("")
                        .queryParam("page", pageable.getPageNumber())
                        .queryParam("size", pageable.getPageSize())
                        .queryParam("name", currentFilter == null ? "" : currentFilter)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PageWrapper<ItemDto>>() {})
                .map(PageWrapper::getData)
                .map(page -> new PageImpl<>(page.getContent(), pageable, page.getMetadata().getTotalElements())); // safely cast
    }

    public Mono<Void> createItem(String name, int price) {
        Map<String, Object> body = Map.of("name", name, "price", price);
        return webClient.post()
                .uri("")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Void> updateItem(Long id, String name, int price) {
        Map<String, Object> body = Map.of("name", name, "price", price, "id", id);
        return webClient.put()
                .uri("", id)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Void> deleteItem(Long id) {
        return webClient.delete()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(Void.class);
    }
}

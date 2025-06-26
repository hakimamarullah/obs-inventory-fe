package com.sg.obs.itemmanagement.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.web.PagedModel;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageWrapper<T> implements Serializable {

    private List<T> content;

    private PageMetadata page;

    private PageWrapper<T> data;



    public PageWrapper(PagedModel<T> pagedModel) {
        this.content = pagedModel.getContent();
        this.page = new PageMetadata();
        PagedModel.PageMetadata metadata = pagedModel.getMetadata();
        this.page.setNumber(Optional.ofNullable(metadata).map(PagedModel.PageMetadata::number).orElse(0L));
        this.page.setSize(Optional.ofNullable(metadata).map(PagedModel.PageMetadata::size).orElse(0L));
        this.page.setTotalElements(Optional.ofNullable(metadata).map(PagedModel.PageMetadata::totalElements).orElse(0L));
        this.page.setTotalPages(Optional.ofNullable(metadata).map(PagedModel.PageMetadata::totalPages).orElse(0L));
    }




    @JsonIgnore
    public static <T> PageWrapper<T> of(PagedModel<T> pagedModel) {
        return new PageWrapper<>(pagedModel);
    }

    @JsonIgnore
    public PageMetadata getMetadata() {
        return page;
    }


    @Setter
    @Getter
    public static class PageMetadata implements Serializable {
        private long number;
        private long size;
        private long totalElements;
        private long totalPages;

    }
}

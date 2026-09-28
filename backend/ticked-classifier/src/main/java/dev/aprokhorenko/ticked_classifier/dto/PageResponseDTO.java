package dev.aprokhorenko.ticked_classifier.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Data;

@Data
public class PageResponseDTO<T> {

    private List<T> content;
    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;

    public PageResponseDTO(Page<T> page) {
        this.content = page.getContent();
        this.currentPage = page.getNumber();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
    }

    public PageResponseDTO(List<T> content, Integer currentPage, Integer totalPages, Long totalElements) {
        this.content = content;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }

}

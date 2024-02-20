package com.example.catalogservice.service;

import com.example.catalogservice.dto.CatalogDto;

public interface CatalogService {
    Iterable<CatalogDto> getAllCatalogs();
    CatalogDto getCatalog(String productId);

    CatalogDto createCatalog(CatalogDto catalogDto);

    CatalogDto updateCatalog(String productId, CatalogDto catalogDto);

    void deleteCatalog(String productId);
}

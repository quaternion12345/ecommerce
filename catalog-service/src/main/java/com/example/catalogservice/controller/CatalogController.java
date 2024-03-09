package com.example.catalogservice.controller;

import com.example.catalogservice.dto.CatalogDto;
import com.example.catalogservice.service.CatalogService;
import com.example.catalogservice.vo.RequestCatalog;
import com.example.catalogservice.vo.RequestUpdateCatalog;
import com.example.catalogservice.vo.ResponseCatalog;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/catalog-service")
public class CatalogController {
    Environment env;
    CatalogService catalogService;

    @Autowired
    public CatalogController(Environment env, CatalogService catalogService) {
        this.env = env;
        this.catalogService = catalogService;
    }

    // 서버 health check
    @GetMapping("/health_check")
    public String status(){
        return String.format("It's Working in Catalog Service on PORT %s", env.getProperty("local.server.port"));
    }

    // 전체 물품 재고 확인
    @GetMapping("/catalogs")
    public ResponseEntity<List<ResponseCatalog>> getCatalogs(){
        Iterable<CatalogDto> catalogList = catalogService.getAllCatalogs();

        List<ResponseCatalog> result = new ArrayList<>();
        catalogList.forEach(v -> {
            result.add(new ModelMapper().map(v, ResponseCatalog.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    // 특정 물품 재고 확인
    @GetMapping("/{productId}/catalogs")
    public ResponseEntity<ResponseCatalog> getCatalog(@PathVariable String productId){
        CatalogDto catalog = catalogService.getCatalog(productId);
        return ResponseEntity.status(HttpStatus.OK).body(new ModelMapper().map(catalog, ResponseCatalog.class));
    }

    // 물품 입고
    @PostMapping("/catalogs")
    public ResponseEntity<ResponseCatalog> createCatalog(@RequestBody @Valid RequestCatalog catalog){
        ModelMapper mapper = new ModelMapper();
        CatalogDto catalogDto = mapper.map(catalog, CatalogDto.class);

        CatalogDto createdCatalog = catalogService.createCatalog(catalogDto);

        ResponseCatalog responseCatalog = mapper.map(createdCatalog, ResponseCatalog.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseCatalog);
    }

    // 물품 정보 변경 -> 이미 주문한 정보에는 굳이 반영할 필요 X
    @PatchMapping("/{productId}/catalogs")
    public ResponseEntity<ResponseCatalog> updateCatalog(@PathVariable String productId, @RequestBody @Valid RequestUpdateCatalog catalog){
        ModelMapper mapper = new ModelMapper();
        CatalogDto catalogDto = mapper.map(catalog, CatalogDto.class);

        CatalogDto updatedCatalog = catalogService.updateCatalog(productId, catalogDto);

        ResponseCatalog responseCatalog = mapper.map(updatedCatalog, ResponseCatalog.class);
        return ResponseEntity.status(HttpStatus.OK).body(responseCatalog);
    }

    // 물품 삭제
    @DeleteMapping("/{productId}/catalogs")
    public ResponseEntity<ResponseCatalog> deleteCatalog(@PathVariable String productId){
        catalogService.deleteCatalog(productId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}

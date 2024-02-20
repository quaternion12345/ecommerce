package com.example.catalogservice.controller;

import com.example.catalogservice.dto.CatalogDto;
import com.example.catalogservice.service.CatalogService;
import com.example.catalogservice.vo.RequestCatalog;
import com.example.catalogservice.vo.ResponseCatalog;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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
        try{
            CatalogDto catalog = catalogService.getCatalog(productId);
            return ResponseEntity.status(HttpStatus.OK).body(new ModelMapper().map(catalog, ResponseCatalog.class));
        }catch (NoSuchElementException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }catch (Exception ex){
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 물품 입고
    @PostMapping("/catalogs")
    public ResponseEntity<ResponseCatalog> createCatalog(@RequestBody RequestCatalog catalog){
        ModelMapper mapper = new ModelMapper();
        CatalogDto catalogDto = mapper.map(catalog, CatalogDto.class);
        try{
            CatalogDto createdCatalog = catalogService.createCatalog(catalogDto);
            ResponseCatalog responseCatalog = mapper.map(createdCatalog, ResponseCatalog.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseCatalog);
        }catch(NoSuchElementException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }catch(Exception ex){
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 물품 정보 변경
    @PatchMapping("/{productId}/catalogs")
    public ResponseEntity<ResponseCatalog> updateCatalog(@PathVariable String productId, @RequestBody RequestCatalog catalog){
        ModelMapper mapper = new ModelMapper();
        CatalogDto catalogDto = mapper.map(catalog, CatalogDto.class);
        try{
            CatalogDto updatedCatalog = catalogService.updateCatalog(productId, catalogDto);
            ResponseCatalog responseCatalog = mapper.map(updatedCatalog, ResponseCatalog.class);
            return ResponseEntity.status(HttpStatus.OK).body(responseCatalog);
        }catch(NoSuchElementException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        catch(Exception ex){
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 물품 삭제
    @DeleteMapping("/{productId}/catalogs")
    public ResponseEntity<ResponseCatalog> deleteCatalog(@PathVariable String productId){
        try{
            catalogService.deleteCatalog(productId);
            return ResponseEntity.status(HttpStatus.OK).build();
        }catch(NoSuchElementException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        catch(Exception ex){
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

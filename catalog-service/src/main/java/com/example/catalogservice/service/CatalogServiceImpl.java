package com.example.catalogservice.service;

import com.example.catalogservice.dto.CatalogDto;
import com.example.catalogservice.jpa.MongoCatalogRepository;
import com.example.catalogservice.jpa.MySQLCatalogEntity;
import com.example.catalogservice.jpa.MySQLCatalogRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Data
@Slf4j
@Service
public class CatalogServiceImpl implements CatalogService{
    MySQLCatalogRepository mySQLCatalogRepository;
    MongoCatalogRepository mongoCatalogRepository;

    @Autowired
    public CatalogServiceImpl(MySQLCatalogRepository mySQLCatalogRepository, MongoCatalogRepository mongoCatalogRepository) {
        this.mySQLCatalogRepository = mySQLCatalogRepository;
        this.mongoCatalogRepository = mongoCatalogRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogDto> getAllCatalogs() {
        return StreamSupport.stream(mongoCatalogRepository.findAll().spliterator(), false)
                .map(p -> new ModelMapper().map(p, CatalogDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogDto getCatalog(String productId) {
        return new ModelMapper().map(mongoCatalogRepository.findByProductId(productId).orElseThrow(NoSuchElementException::new), CatalogDto.class);
    }

    @Override
    @Transactional
    public CatalogDto createCatalog(CatalogDto catalogDto) {
        // Bad Inputs
        if(catalogDto.getUnitPrice() != null && catalogDto.getUnitPrice() < 0) throw new NoSuchElementException();
        if(catalogDto.getStock() != null && catalogDto.getStock() < 0) throw new NoSuchElementException();

        MySQLCatalogEntity mySQLCatalogEntity = MySQLCatalogEntity.builder()
                .productId(catalogDto.getProductId())
                .productName(catalogDto.getProductName())
                .stock(catalogDto.getStock())
                .unitPrice(catalogDto.getUnitPrice())
                .build();

        return new ModelMapper().map(mySQLCatalogRepository.save(mySQLCatalogEntity), CatalogDto.class);
    }

    @Override
    @Transactional
    public CatalogDto updateCatalog(String productId, CatalogDto catalogDto) {
        MySQLCatalogEntity mySQLCatalogEntity = mySQLCatalogRepository.findByProductId(productId).orElseThrow(NoSuchElementException::new);
        // Bad Inputs
        if(catalogDto.getUnitPrice() != null && catalogDto.getUnitPrice() < 0) throw new NoSuchElementException();
        if(catalogDto.getStock() != null && catalogDto.getStock() < 0) throw new NoSuchElementException();
        // Update Using Dirty Checking
        mySQLCatalogEntity.updateCatalog(catalogDto);
        return new ModelMapper().map(mySQLCatalogEntity, CatalogDto.class);
    }

    @Override
    @Transactional
    public void deleteCatalog(String productId) {
        mySQLCatalogRepository.delete(
                mySQLCatalogRepository.findByProductId(productId).orElseThrow(NoSuchElementException::new)
        );
    }
}

package com.example.orderservice;

import com.example.orderservice.client.CatalogServiceClient;
import com.example.orderservice.controller.OrderController;
import com.example.orderservice.jpa.MongoOrderRepository;
import com.example.orderservice.jpa.MySQLOrderEntity;
import com.example.orderservice.jpa.MySQLOrderRepository;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseCatalog;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class,
		MongoDataAutoConfiguration.class,
		MongoAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class})
@Slf4j
class OrderIntegrationTests {
	@Autowired OrderController orderController;
	@MockBean MySQLOrderRepository mySQLOrderRepository;
	@MockBean MongoOrderRepository mongoOrderRepository;
	@MockBean CatalogServiceClient catalogServiceClient;

	@Test
	void test() throws Exception{
		// given
		String userId = UUID.randomUUID().toString();

		RequestOrder requestOrder = new RequestOrder();
		requestOrder.setProductId("CATALOG-001");
		requestOrder.setQty(20);

		ResponseCatalog responseCatalog = new ResponseCatalog();
		responseCatalog.setStock(100);
		responseCatalog.setUnitPrice(1500);

		MySQLOrderEntity mySQLOrderEntity = MySQLOrderEntity.builder()
				.productId(requestOrder.getProductId())
				.qty(requestOrder.getQty())
				.unitPrice(responseCatalog.getUnitPrice())
				.totalPrice(requestOrder.getQty() * responseCatalog.getUnitPrice())
				.userId(userId)
				.orderId(UUID.randomUUID().toString())
				.build();

		// when
		when(catalogServiceClient.getCatalog(any())).thenReturn(responseCatalog);
		when(mySQLOrderRepository.save(any())).thenReturn(mySQLOrderEntity);

		// then
		ResponseEntity result = orderController.createOrder(userId, requestOrder);
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}

}

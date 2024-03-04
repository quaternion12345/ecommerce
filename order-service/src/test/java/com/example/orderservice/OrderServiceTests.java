package com.example.orderservice;

import com.example.orderservice.client.CatalogServiceClient;
import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.exception.BadRequestException;
import com.example.orderservice.exception.NotFoundException;
import com.example.orderservice.jpa.MongoOrderRepository;
import com.example.orderservice.jpa.MySQLOrderEntity;
import com.example.orderservice.jpa.MySQLOrderRepository;
import com.example.orderservice.service.OrderServiceImpl;
import com.example.orderservice.vo.ResponseCatalog;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@Slf4j
class OrderServiceTests {
	@InjectMocks OrderServiceImpl orderService;
	@Mock MySQLOrderRepository mySQLOrderRepository;
	@Mock MongoOrderRepository mongoOrderRepository;
	@Mock CatalogServiceClient catalogServiceClient;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	CircuitBreakerFactory circuitBreakerFactory;

	@BeforeEach
	public void init() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("Create Order Test - Valid")
	void create_order_test1() throws Exception{
		// given
		OrderDto orderDto = new OrderDto();
		orderDto.setUserId("TestUserId");
		orderDto.setProductId("CATALOG-001");
		orderDto.setQty(10);

		ResponseCatalog responseCatalog = new ResponseCatalog();
		responseCatalog.setUnitPrice(1500);
		responseCatalog.setStock(100);

		// when
		when(circuitBreakerFactory.create(any()).run(any(), any())).thenReturn(responseCatalog);
		when(mySQLOrderRepository.save(any())).thenReturn(new MySQLOrderEntity());

		OrderDto result = orderService.createOrder(orderDto);

		// then
		verify(mySQLOrderRepository, times(1)).save(any());
	}

	@Test
	@DisplayName("Create Order Test - Not Found")
	void create_order_test2() throws Exception{
		// given
		OrderDto orderDto = new OrderDto();
		orderDto.setQty(10);

		ResponseCatalog responseCatalog = new ResponseCatalog();
		responseCatalog.setUnitPrice(1500);
		responseCatalog.setStock(null);

		// when
		when(circuitBreakerFactory.create(any()).run(any(), any())).thenReturn(responseCatalog);

		// then
		assertThatCode(() -> orderService.createOrder(orderDto))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("Product");
	}

	@Test
	@DisplayName("Create Order Test - Bad Request")
	void create_order_test3() throws Exception{
		// given
		OrderDto orderDto = new OrderDto();
		orderDto.setQty(10);

		ResponseCatalog responseCatalog = new ResponseCatalog();
		responseCatalog.setUnitPrice(1500);
		responseCatalog.setStock(5);

		// when
		when(circuitBreakerFactory.create(any()).run(any(), any())).thenReturn(responseCatalog);

		// then
		assertThatCode(() -> orderService.createOrder(orderDto))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("stocks");
	}

}

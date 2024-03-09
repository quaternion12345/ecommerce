package com.example.orderservice;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.exception.BadRequestException;
import com.example.orderservice.exception.NotFoundException;
import com.example.orderservice.messagequeue.OrderProducer;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.RequestUpdateOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class OrderControllerTests {
	@Autowired private MockMvc mockMvc;

	@MockBean private OrderService orderService;
//	@MockBean private KafkaProducer kafkaProducer;
	@MockBean private OrderProducer orderProducer;


	@Test
	void health_check_test() throws Exception{
		mockMvc.perform(get("/health_check"))
				.andDo(print())
				.andExpect(content().string(containsString("Order Service")));
	}

	@Test
	@DisplayName("[POST] Create Order Test - Valid")
	void create_order_test1() throws Exception{
		// given
		String userId = "RandomUUID";

		RequestOrder requestOrder = new RequestOrder();
		requestOrder.setProductId("CATALOG-001");
		requestOrder.setQty(10);

		OrderDto orderDto = new OrderDto();
		orderDto.setProductId("CATALOG-001");
		orderDto.setQty(10);
		orderDto.setOrderId(UUID.randomUUID().toString());
		orderDto.setUnitPrice(1000);
		orderDto.setTotalPrice(10000);

		// when
		when(orderService.createOrder(any())).thenReturn(orderDto);

		mockMvc.perform(post("/{userId}/orders", userId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(requestOrder)))

		// then
				.andDo(print())
				.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("[POST] Create Order Test - Bean Validation Failed")
	void create_order_test2() throws Exception{
		// given
		String userId = "RandomUUID";

		RequestOrder requestOrder = new RequestOrder();
		requestOrder.setProductId(null); // Bean Validation Test
		requestOrder.setQty(-5); // Bean Validation Test

		OrderDto orderDto = new OrderDto();
		orderDto.setProductId(null);
		orderDto.setQty(-5);
		orderDto.setOrderId(UUID.randomUUID().toString());
		orderDto.setUnitPrice(1000);
		orderDto.setTotalPrice(10000);

		// when
		when(orderService.createOrder(any())).thenReturn(orderDto);

		mockMvc.perform(post("/{userId}/orders", userId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(requestOrder)))

		// then
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("[POST] Create Order Test - Not Found")
	void create_order_test3() throws Exception{
		// given
		String userId = "RandomUUID";

		RequestOrder requestOrder = new RequestOrder();
		requestOrder.setProductId("CATALOG-001");
		requestOrder.setQty(10);

		// when
		when(orderService.createOrder(any())).thenThrow(new NotFoundException("Product with productId: " +requestOrder.getProductId()));

		mockMvc.perform(post("/{userId}/orders", userId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(requestOrder)))

				// then
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("[POST] Create Order Test - BadRequest")
	void create_order_test4() throws Exception{
		// given
		String userId = "RandomUUID";

		RequestOrder requestOrder = new RequestOrder();
		requestOrder.setProductId("CATALOG-001");
		requestOrder.setQty(10);

		// when
		when(orderService.createOrder(any())).thenThrow(new BadRequestException("You cannot order more products than stocks we have"));

		mockMvc.perform(post("/{userId}/orders", userId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(requestOrder)))

				// then
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("[GET] Get Orders Test - Valid")
	void get_orders_test() throws Exception{
		// given
		String userId = "RandomUUID";

		// when
		when(orderService.getOrdersByUserId(any())).thenReturn(new ArrayList<OrderDto>());

		mockMvc.perform(get("/{userId}/orders", userId))

		// then
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("[DELETE] Delete Orders Test - Valid")
	void delete_orders_test() throws Exception{
		// given
		String userId = "RandomUUID";

		// when
		mockMvc.perform(delete("/{userId}/orders", userId))

		// then
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("[GET] Get Order Test - Valid")
	void get_order_test1() throws Exception{
		// given
		String orderId = "RandomUUID";

		OrderDto orderDto = new OrderDto();
		orderDto.setOrderId(orderId);

		// when
		when(orderService.getOrderByOrderId(any())).thenReturn(orderDto);

		mockMvc.perform(get("/{orderId}/order", orderId))

		// then
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("[GET] Get Order Test - Not Found")
	void get_order_test2() throws Exception{
		// given
		String orderId = "RandomUUID";

		// when
		when(orderService.getOrderByOrderId(any())).thenThrow(new NotFoundException("orderId: " + orderId));

		mockMvc.perform(get("/{orderId}/order", orderId))

		// then
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("[PATCH] Update Order Test - Valid")
	void update_order_test1() throws Exception{
		// given
		String orderId = "RandomUUID";

		RequestUpdateOrder requestOrder = new RequestUpdateOrder();
		requestOrder.setQty(10);

		OrderDto orderDto = new OrderDto();
		orderDto.setQty(10);

		// when
		when(orderService.updateOrderByOrderId(any())).thenReturn(orderDto);

		mockMvc.perform(patch("/{orderId}/order", orderId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(requestOrder)))

		// then
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("[PATCH] Update Order Test - Bean Validation Failed")
	void update_order_test2() throws Exception{
		// given
		String orderId = "RandomUUID";

		RequestUpdateOrder requestOrder = new RequestUpdateOrder();
		requestOrder.setQty(0);

		OrderDto orderDto = new OrderDto();
		orderDto.setQty(0);

		// when
		when(orderService.updateOrderByOrderId(any())).thenReturn(orderDto);

		mockMvc.perform(patch("/{orderId}/order", orderId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(requestOrder)))

		// then
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("[PATCH] Update Order Test - Not Found")
	void update_order_test3() throws Exception{
		// given
		String orderId = "RandomUUID";

		RequestUpdateOrder requestOrder = new RequestUpdateOrder();
		requestOrder.setQty(5);

		OrderDto orderDto = new OrderDto();
		orderDto.setQty(5);

		// when
		when(orderService.updateOrderByOrderId(any())).thenThrow(new NotFoundException("orderId: " + orderId));

		mockMvc.perform(patch("/{orderId}/order", orderId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(requestOrder)))

		// then
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("[DELETE] Delete Order Test - Valid")
	void delete_order_test1() throws Exception{
		// given
		String orderId = "RandomUUID";

		// when
		mockMvc.perform(delete("/{orderId}/order", orderId))

		// then
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("[DELETE] Delete Order Test - Not Found")
	void delete_order_test2() throws Exception{
		// given
		String orderId = "RandomUUID";

		// when
		doThrow(new NotFoundException("orderId: " + orderId))
				.when(orderService)
						.deleteOrderByOrderId(any());

		mockMvc.perform(delete("/{orderId}/order", orderId))

		// then
				.andDo(print())
				.andExpect(status().isNotFound());
	}
}

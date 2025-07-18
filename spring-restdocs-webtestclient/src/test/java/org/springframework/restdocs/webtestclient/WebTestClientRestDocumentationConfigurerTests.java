/*
 * Copyright 2014-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.restdocs.webtestclient;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import org.springframework.http.HttpMethod;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link WebTestClientRestDocumentationConfigurer}.
 *
 * @author Andy Wilkinson
 */
@ExtendWith(RestDocumentationExtension.class)
class WebTestClientRestDocumentationConfigurerTests {

	private WebTestClientRestDocumentationConfigurer configurer;

	@BeforeEach
	void setUp(RestDocumentationContextProvider restDocumentation) {
		this.configurer = new WebTestClientRestDocumentationConfigurer(restDocumentation);

	}

	@Test
	void configurationCanBeRetrievedButOnlyOnce() {
		ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test"))
			.header(WebTestClient.WEBTESTCLIENT_REQUEST_ID, "1")
			.build();
		this.configurer.filter(request, mock(ExchangeFunction.class));
		assertThat(WebTestClientRestDocumentationConfigurer.retrieveConfiguration(request.headers())).isNotNull();
		assertThatIllegalStateException()
			.isThrownBy(() -> WebTestClientRestDocumentationConfigurer.retrieveConfiguration(request.headers()));
	}

	@Test
	void requestUriHasDefaultsAppliedWhenItHasNoHost() {
		ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test?foo=bar#baz"))
			.header(WebTestClient.WEBTESTCLIENT_REQUEST_ID, "1")
			.build();
		ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
		this.configurer.filter(request, exchangeFunction);
		ArgumentCaptor<ClientRequest> requestCaptor = ArgumentCaptor.forClass(ClientRequest.class);
		verify(exchangeFunction).exchange(requestCaptor.capture());
		assertThat(requestCaptor.getValue().url()).isEqualTo(URI.create("http://localhost:8080/test?foo=bar#baz"));
	}

	@Test
	void requestUriIsNotChangedWhenItHasAHost() {
		ClientRequest request = ClientRequest
			.create(HttpMethod.GET, URI.create("https://api.example.com:4567/test?foo=bar#baz"))
			.header(WebTestClient.WEBTESTCLIENT_REQUEST_ID, "1")
			.build();
		ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
		this.configurer.filter(request, exchangeFunction);
		ArgumentCaptor<ClientRequest> requestCaptor = ArgumentCaptor.forClass(ClientRequest.class);
		verify(exchangeFunction).exchange(requestCaptor.capture());
		assertThat(requestCaptor.getValue().url())
			.isEqualTo(URI.create("https://api.example.com:4567/test?foo=bar#baz"));
	}

}

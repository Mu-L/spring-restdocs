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

package org.springframework.restdocs.mockmvc;

import java.net.URI;

import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.restdocs.generate.RestDocumentationGenerator;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.head;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.options;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.request;

/**
 * Tests for {@link RestDocumentationRequestBuilders}.
 *
 * @author Andy Wilkinson
 *
 */
class RestDocumentationRequestBuildersTests {

	private final ServletContext servletContext = new MockServletContext();

	@Test
	void getTemplate() {
		assertTemplate(get("/{template}", "t"), HttpMethod.GET);
	}

	@Test
	void getUri() {
		assertUri(get(URI.create("/uri")), HttpMethod.GET);
	}

	@Test
	void postTemplate() {
		assertTemplate(post("/{template}", "t"), HttpMethod.POST);
	}

	@Test
	void postUri() {
		assertUri(post(URI.create("/uri")), HttpMethod.POST);
	}

	@Test
	void putTemplate() {
		assertTemplate(put("/{template}", "t"), HttpMethod.PUT);
	}

	@Test
	void putUri() {
		assertUri(put(URI.create("/uri")), HttpMethod.PUT);
	}

	@Test
	void patchTemplate() {
		assertTemplate(patch("/{template}", "t"), HttpMethod.PATCH);
	}

	@Test
	void patchUri() {
		assertUri(patch(URI.create("/uri")), HttpMethod.PATCH);
	}

	@Test
	void deleteTemplate() {
		assertTemplate(delete("/{template}", "t"), HttpMethod.DELETE);
	}

	@Test
	void deleteUri() {
		assertUri(delete(URI.create("/uri")), HttpMethod.DELETE);
	}

	@Test
	void optionsTemplate() {
		assertTemplate(options("/{template}", "t"), HttpMethod.OPTIONS);
	}

	@Test
	void optionsUri() {
		assertUri(options(URI.create("/uri")), HttpMethod.OPTIONS);
	}

	@Test
	void headTemplate() {
		assertTemplate(head("/{template}", "t"), HttpMethod.HEAD);
	}

	@Test
	void headUri() {
		assertUri(head(URI.create("/uri")), HttpMethod.HEAD);
	}

	@Test
	void requestTemplate() {
		assertTemplate(request(HttpMethod.GET, "/{template}", "t"), HttpMethod.GET);
	}

	@Test
	void requestUri() {
		assertUri(request(HttpMethod.GET, URI.create("/uri")), HttpMethod.GET);
	}

	@Test
	void multipartTemplate() {
		assertTemplate(multipart("/{template}", "t"), HttpMethod.POST);
	}

	@Test
	void multipartUri() {
		assertUri(multipart(URI.create("/uri")), HttpMethod.POST);
	}

	private void assertTemplate(MockHttpServletRequestBuilder builder, HttpMethod httpMethod) {
		MockHttpServletRequest request = builder.buildRequest(this.servletContext);
		assertThat((String) request.getAttribute(RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE))
			.isEqualTo("/{template}");
		assertThat(request.getRequestURI()).isEqualTo("/t");
		assertThat(request.getMethod()).isEqualTo(httpMethod.name());
	}

	private void assertUri(MockHttpServletRequestBuilder builder, HttpMethod httpMethod) {
		MockHttpServletRequest request = builder.buildRequest(this.servletContext);
		assertThat(request.getRequestURI()).isEqualTo("/uri");
		assertThat(request.getMethod()).isEqualTo(httpMethod.name());
	}

}

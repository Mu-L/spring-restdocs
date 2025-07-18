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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.templates.TemplateFormat;
import org.springframework.restdocs.templates.TemplateFormats;
import org.springframework.restdocs.testfixtures.SnippetConditions;
import org.springframework.restdocs.testfixtures.SnippetConditions.CodeBlockCondition;
import org.springframework.restdocs.testfixtures.SnippetConditions.HttpResponseCondition;
import org.springframework.restdocs.testfixtures.SnippetConditions.TableCondition;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

/**
 * Integration tests for using Spring REST Docs with Spring Framework's WebTestClient.
 *
 * @author Andy Wilkinson
 */
@ExtendWith(RestDocumentationExtension.class)
public class WebTestClientRestDocumentationIntegrationTests {

	private WebTestClient webTestClient;

	@BeforeEach
	void setUp(RestDocumentationContextProvider restDocumentation) {
		RouterFunction<ServerResponse> route = RouterFunctions
			.route(RequestPredicates.GET("/"),
					(request) -> ServerResponse.status(HttpStatus.OK).body(fromValue(new Person("Jane", "Doe"))))
			.andRoute(RequestPredicates.GET("/{foo}/{bar}"),
					(request) -> ServerResponse.status(HttpStatus.OK).body(fromValue(new Person("Jane", "Doe"))))
			.andRoute(RequestPredicates.POST("/upload"),
					(request) -> request.body(BodyExtractors.toMultipartData())
						.map((parts) -> ServerResponse.status(HttpStatus.OK).build().block()))
			.andRoute(RequestPredicates.GET("/set-cookie"),
					(request) -> ServerResponse.ok()
						.cookie(ResponseCookie.from("name", "value").domain("localhost").httpOnly(true).build())
						.build());
		this.webTestClient = WebTestClient.bindToRouterFunction(route)
			.configureClient()
			.baseUrl("https://api.example.com")
			.filter(documentationConfiguration(restDocumentation))
			.build();
	}

	@Test
	void defaultSnippetGeneration() {
		File outputDir = new File("build/generated-snippets/default-snippets");
		FileSystemUtils.deleteRecursively(outputDir);
		this.webTestClient.get()
			.uri("/")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.consumeWith(document("default-snippets"));
		assertExpectedSnippetFilesExist(outputDir, "http-request.adoc", "http-response.adoc", "curl-request.adoc",
				"httpie-request.adoc", "request-body.adoc", "response-body.adoc");
	}

	@Test
	void pathParametersSnippet() {
		this.webTestClient.get()
			.uri("/{foo}/{bar}", "1", "2")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.consumeWith(
					document("path-parameters", pathParameters(parameterWithName("foo").description("Foo description"),
							parameterWithName("bar").description("Bar description"))));
		assertThat(new File("build/generated-snippets/path-parameters/path-parameters.adoc")).has(content(
				tableWithTitleAndHeader(TemplateFormats.asciidoctor(), "+/{foo}/{bar}+", "Parameter", "Description")
					.row("`foo`", "Foo description")
					.row("`bar`", "Bar description")));
	}

	@Test
	void queryParametersSnippet() {
		this.webTestClient.get()
			.uri("/?a=alpha&b=bravo")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.consumeWith(document("query-parameters",
					queryParameters(parameterWithName("a").description("Alpha description"),
							parameterWithName("b").description("Bravo description"))));
		assertThat(new File("build/generated-snippets/query-parameters/query-parameters.adoc"))
			.has(content(tableWithHeader(TemplateFormats.asciidoctor(), "Parameter", "Description")
				.row("`a`", "Alpha description")
				.row("`b`", "Bravo description")));
	}

	@Test
	void multipart() {
		MultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
		multipartData.add("a", "alpha");
		multipartData.add("b", "bravo");
		Consumer<EntityExchangeResult<byte[]>> documentation = document("multipart",
				requestParts(partWithName("a").description("Part a"), partWithName("b").description("Part b")));
		this.webTestClient.post()
			.uri("/upload")
			.body(BodyInserters.fromMultipartData(multipartData))
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.consumeWith(documentation);
		assertThat(new File("build/generated-snippets/multipart/request-parts.adoc"))
			.has(content(tableWithHeader(TemplateFormats.asciidoctor(), "Part", "Description").row("`a`", "Part a")
				.row("`b`", "Part b")));
	}

	@Test
	void responseWithSetCookie() {
		this.webTestClient.get()
			.uri("/set-cookie")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.consumeWith(document("set-cookie"));
		assertThat(new File("build/generated-snippets/set-cookie/http-response.adoc"))
			.has(content(httpResponse(TemplateFormats.asciidoctor(), HttpStatus.OK).header(HttpHeaders.SET_COOKIE,
					"name=value; Domain=localhost; HttpOnly")));
	}

	@Test
	void curlSnippetWithCookies() {
		this.webTestClient.get()
			.uri("/")
			.cookie("cookieName", "cookieVal")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.consumeWith(document("curl-snippet-with-cookies"));
		assertThat(new File("build/generated-snippets/curl-snippet-with-cookies/curl-request.adoc"))
			.has(content(codeBlock(TemplateFormats.asciidoctor(), "bash")
				.withContent(String.format("$ curl 'https://api.example.com/' -i -X GET \\%n"
						+ "    -H 'Accept: application/json' \\%n" + "    --cookie 'cookieName=cookieVal'"))));
	}

	@Test
	void curlSnippetWithEmptyParameterQueryString() {
		this.webTestClient.get()
			.uri("/?a=")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.consumeWith(document("curl-snippet-with-empty-parameter-query-string"));
		assertThat(
				new File("build/generated-snippets/curl-snippet-with-empty-parameter-query-string/curl-request.adoc"))
			.has(content(codeBlock(TemplateFormats.asciidoctor(), "bash").withContent(String
				.format("$ curl 'https://api.example.com/?a=' -i -X GET \\%n" + "    -H 'Accept: application/json'"))));
	}

	@Test
	void httpieSnippetWithCookies() {
		this.webTestClient.get()
			.uri("/")
			.cookie("cookieName", "cookieVal")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody()
			.consumeWith(document("httpie-snippet-with-cookies"));
		assertThat(new File("build/generated-snippets/httpie-snippet-with-cookies/httpie-request.adoc"))
			.has(content(codeBlock(TemplateFormats.asciidoctor(), "bash")
				.withContent(String.format("$ http GET 'https://api.example.com/' \\%n"
						+ "    'Accept:application/json' \\%n" + "    'Cookie:cookieName=cookieVal'"))));
	}

	@Test
	void illegalStateExceptionShouldBeThrownWhenCallDocumentWebClientNotConfigured() {
		assertThatThrownBy(() -> this.webTestClient
			.mutateWith((builder, httpHandlerBuilder, connector) -> builder.filters(List::clear).build())
			.get()
			.uri("/")
			.exchange()
			.expectBody()
			.consumeWith(document("default-snippets"))).isInstanceOf(IllegalStateException.class)
			.hasMessage("REST Docs configuration not found. Did you forget to register a "
					+ "WebTestClientRestDocumentationConfigurer as a filter?");
	}

	private void assertExpectedSnippetFilesExist(File directory, String... snippets) {
		Set<File> actual = new HashSet<>(Arrays.asList(directory.listFiles()));
		Set<File> expected = Stream.of(snippets)
			.map((snippet) -> new File(directory, snippet))
			.collect(Collectors.toSet());
		assertThat(actual).isEqualTo(expected);
	}

	private Condition<File> content(final Condition<String> delegate) {
		return new Condition<>() {

			@Override
			public boolean matches(File value) {
				try {
					return delegate.matches(FileCopyUtils
						.copyToString(new InputStreamReader(new FileInputStream(value), StandardCharsets.UTF_8)));
				}
				catch (IOException ex) {
					fail("Failed to read '" + value + "'", ex);
					return false;
				}
			}

		};
	}

	private CodeBlockCondition<?> codeBlock(TemplateFormat format, String language) {
		return SnippetConditions.codeBlock(format, language);
	}

	private HttpResponseCondition httpResponse(TemplateFormat format, HttpStatus status) {
		return SnippetConditions.httpResponse(format, status);
	}

	private TableCondition<?> tableWithHeader(TemplateFormat format, String... headers) {
		return SnippetConditions.tableWithHeader(format, headers);
	}

	private TableCondition<?> tableWithTitleAndHeader(TemplateFormat format, String title, String... headers) {
		return SnippetConditions.tableWithTitleAndHeader(format, title, headers);
	}

	/**
	 * A person.
	 */
	public static class Person {

		private final String firstName;

		private final String lastName;

		Person(String firstName, String lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}

		public String getFirstName() {
			return this.firstName;
		}

		public String getLastName() {
			return this.lastName;
		}

	}

}

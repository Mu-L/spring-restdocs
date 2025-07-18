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

package com.example.restassured;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyHeaders;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.restassured.RestAssuredRestDocumentation.documentationConfiguration;

@ExtendWith(RestDocumentationExtension.class)
class CustomDefaultOperationPreprocessors {

	@SuppressWarnings("unused")
	private RequestSpecification spec;

	@BeforeEach
	void setup(RestDocumentationContextProvider restDocumentation) {
		// tag::custom-default-operation-preprocessors[]
		this.spec = new RequestSpecBuilder()
			.addFilter(documentationConfiguration(restDocumentation).operationPreprocessors()
				.withRequestDefaults(modifyHeaders().remove("Foo")) // <1>
				.withResponseDefaults(prettyPrint())) // <2>
			.build();
		// end::custom-default-operation-preprocessors[]
	}

}

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

package org.springframework.restdocs.templates.mustache;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import org.springframework.restdocs.mustache.Template.Fragment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link AsciidoctorTableCellContentLambda}.
 *
 * @author Andy Wilkinson
 */
class AsciidoctorTableCellContentLambdaTests {

	@Test
	void verticalBarCharactersAreEscaped() throws IOException {
		Fragment fragment = mock(Fragment.class);
		given(fragment.execute()).willReturn("|foo|bar|baz|");
		StringWriter writer = new StringWriter();
		new AsciidoctorTableCellContentLambda().execute(fragment, writer);
		assertThat(writer.toString()).isEqualTo("\\|foo\\|bar\\|baz\\|");
	}

	@Test
	void escapedVerticalBarCharactersAreNotEscapedAgain() throws IOException {
		Fragment fragment = mock(Fragment.class);
		given(fragment.execute()).willReturn("\\|foo|bar\\|baz|");
		StringWriter writer = new StringWriter();
		new AsciidoctorTableCellContentLambda().execute(fragment, writer);
		assertThat(writer.toString()).isEqualTo("\\|foo\\|bar\\|baz\\|");
	}

}

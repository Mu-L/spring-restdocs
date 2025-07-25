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

package org.springframework.restdocs.payload;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link JsonContentHandler}.
 *
 * @author Andy Wilkinson
 * @author Mathias Düsterhöft
 */
class JsonContentHandlerTests {

	@Test
	void typeForFieldWithNullValueMustMatch() {
		FieldDescriptor descriptor = new FieldDescriptor("a").type(JsonFieldType.STRING);
		assertThatExceptionOfType(FieldTypesDoNotMatchException.class)
			.isThrownBy(() -> new JsonContentHandler("{\"a\": null}".getBytes(), Arrays.asList(descriptor))
				.resolveFieldType(descriptor));
	}

	@Test
	void typeForFieldWithNotNullAndThenNullValueMustMatch() {
		FieldDescriptor descriptor = new FieldDescriptor("a[].id").type(JsonFieldType.STRING);
		assertThatExceptionOfType(FieldTypesDoNotMatchException.class).isThrownBy(
				() -> new JsonContentHandler("{\"a\":[{\"id\":1},{\"id\":null}]}".getBytes(), Arrays.asList(descriptor))
					.resolveFieldType(descriptor));
	}

	@Test
	void typeForFieldWithNullAndThenNotNullValueMustMatch() {
		FieldDescriptor descriptor = new FieldDescriptor("a.[].id").type(JsonFieldType.STRING);
		assertThatExceptionOfType(FieldTypesDoNotMatchException.class).isThrownBy(
				() -> new JsonContentHandler("{\"a\":[{\"id\":null},{\"id\":1}]}".getBytes(), Arrays.asList(descriptor))
					.resolveFieldType(descriptor));
	}

	@Test
	void typeForOptionalFieldWithNumberAndThenNullValueIsNumber() {
		FieldDescriptor descriptor = new FieldDescriptor("a[].id").optional();
		Object fieldType = new JsonContentHandler("{\"a\":[{\"id\":1},{\"id\":null}]}".getBytes(),
				Arrays.asList(descriptor))
			.resolveFieldType(descriptor);
		assertThat((JsonFieldType) fieldType).isEqualTo(JsonFieldType.NUMBER);
	}

	@Test
	void typeForOptionalFieldWithNullAndThenNumberIsNumber() {
		FieldDescriptor descriptor = new FieldDescriptor("a[].id").optional();
		Object fieldType = new JsonContentHandler("{\"a\":[{\"id\":null},{\"id\":1}]}".getBytes(),
				Arrays.asList(descriptor))
			.resolveFieldType(descriptor);
		assertThat((JsonFieldType) fieldType).isEqualTo(JsonFieldType.NUMBER);
	}

	@Test
	void typeForFieldWithNumberAndThenNullValueIsVaries() {
		FieldDescriptor descriptor = new FieldDescriptor("a[].id");
		Object fieldType = new JsonContentHandler("{\"a\":[{\"id\":1},{\"id\":null}]}".getBytes(),
				Arrays.asList(descriptor))
			.resolveFieldType(descriptor);
		assertThat((JsonFieldType) fieldType).isEqualTo(JsonFieldType.VARIES);
	}

	@Test
	void typeForFieldWithNullAndThenNumberIsVaries() {
		FieldDescriptor descriptor = new FieldDescriptor("a[].id");
		Object fieldType = new JsonContentHandler("{\"a\":[{\"id\":null},{\"id\":1}]}".getBytes(),
				Arrays.asList(descriptor))
			.resolveFieldType(descriptor);
		assertThat((JsonFieldType) fieldType).isEqualTo(JsonFieldType.VARIES);
	}

	@Test
	void typeForOptionalFieldWithNullValueCanBeProvidedExplicitly() {
		FieldDescriptor descriptor = new FieldDescriptor("a").type(JsonFieldType.STRING).optional();
		Object fieldType = new JsonContentHandler("{\"a\": null}".getBytes(), Arrays.asList(descriptor))
			.resolveFieldType(descriptor);
		assertThat((JsonFieldType) fieldType).isEqualTo(JsonFieldType.STRING);
	}

	@Test
	void typeForFieldWithSometimesPresentOptionalAncestorCanBeProvidedExplicitly() {
		FieldDescriptor descriptor = new FieldDescriptor("a.[].b.c").type(JsonFieldType.NUMBER);
		FieldDescriptor ancestor = new FieldDescriptor("a.[].b").optional();
		Object fieldType = new JsonContentHandler("{\"a\":[ { \"d\": 4}, {\"b\":{\"c\":5}, \"d\": 4}]}".getBytes(),
				Arrays.asList(descriptor, ancestor))
			.resolveFieldType(descriptor);
		assertThat((JsonFieldType) fieldType).isEqualTo(JsonFieldType.NUMBER);
	}

	@Test
	void failsFastWithNonJsonContent() {
		assertThatExceptionOfType(PayloadHandlingException.class)
			.isThrownBy(() -> new JsonContentHandler("Non-JSON content".getBytes(), Collections.emptyList()));
	}

	@Test
	void describedFieldThatIsNotPresentIsConsideredMissing() {
		List<FieldDescriptor> descriptors = Arrays.asList(new FieldDescriptor("a"), new FieldDescriptor("b"),
				new FieldDescriptor("c"));
		List<FieldDescriptor> missingFields = new JsonContentHandler("{\"a\": \"alpha\", \"b\":\"bravo\"}".getBytes(),
				descriptors)
			.findMissingFields();
		assertThat(missingFields.size()).isEqualTo(1);
		assertThat(missingFields.get(0).getPath()).isEqualTo("c");
	}

	@Test
	void describedOptionalFieldThatIsNotPresentIsNotConsideredMissing() {
		List<FieldDescriptor> descriptors = Arrays.asList(new FieldDescriptor("a"), new FieldDescriptor("b"),
				new FieldDescriptor("c").optional());
		List<FieldDescriptor> missingFields = new JsonContentHandler("{\"a\": \"alpha\", \"b\":\"bravo\"}".getBytes(),
				descriptors)
			.findMissingFields();
		assertThat(missingFields.size()).isEqualTo(0);
	}

	@Test
	void describedFieldThatIsNotPresentNestedBeneathOptionalFieldThatIsPresentIsConsideredMissing() {
		List<FieldDescriptor> descriptors = Arrays.asList(new FieldDescriptor("a").optional(), new FieldDescriptor("b"),
				new FieldDescriptor("a.c"));
		List<FieldDescriptor> missingFields = new JsonContentHandler("{\"a\":\"alpha\",\"b\":\"bravo\"}".getBytes(),
				descriptors)
			.findMissingFields();
		assertThat(missingFields.size()).isEqualTo(1);
		assertThat(missingFields.get(0).getPath()).isEqualTo("a.c");
	}

	@Test
	void describedFieldThatIsNotPresentNestedBeneathOptionalFieldThatIsNotPresentIsNotConsideredMissing() {
		List<FieldDescriptor> descriptors = Arrays.asList(new FieldDescriptor("a").optional(), new FieldDescriptor("b"),
				new FieldDescriptor("a.c"));
		List<FieldDescriptor> missingFields = new JsonContentHandler("{\"b\":\"bravo\"}".getBytes(), descriptors)
			.findMissingFields();
		assertThat(missingFields.size()).isEqualTo(0);
	}

	@Test
	void describedFieldThatIsNotPresentNestedBeneathOptionalArrayThatIsEmptyIsNotConsideredMissing() {
		List<FieldDescriptor> descriptors = Arrays.asList(new FieldDescriptor("outer"),
				new FieldDescriptor("outer[]").optional(), new FieldDescriptor("outer[].inner"));
		List<FieldDescriptor> missingFields = new JsonContentHandler("{\"outer\":[]}".getBytes(), descriptors)
			.findMissingFields();
		assertThat(missingFields.size()).isEqualTo(0);
	}

	@Test
	void describedSometimesPresentFieldThatIsChildOfSometimesPresentOptionalArrayIsNotConsideredMissing() {
		List<FieldDescriptor> descriptors = Arrays.asList(new FieldDescriptor("a.[].c").optional(),
				new FieldDescriptor("a.[].c.d"));
		List<FieldDescriptor> missingFields = new JsonContentHandler(
				"{\"a\":[ {\"b\": \"bravo\"}, {\"b\": \"bravo\", \"c\": { \"d\": \"delta\"}}]}".getBytes(), descriptors)
			.findMissingFields();
		assertThat(missingFields.size()).isEqualTo(0);
	}

	@Test
	void describedMissingFieldThatIsChildOfNestedOptionalArrayThatIsEmptyIsNotConsideredMissing() {
		List<FieldDescriptor> descriptors = Arrays.asList(new FieldDescriptor("a.[].b").optional(),
				new FieldDescriptor("a.[].b.[]").optional(), new FieldDescriptor("a.[].b.[].c"));
		List<FieldDescriptor> missingFields = new JsonContentHandler("{\"a\":[{\"b\":[]}]}".getBytes(), descriptors)
			.findMissingFields();
		assertThat(missingFields.size()).isEqualTo(0);
	}

	@Test
	void describedMissingFieldThatIsChildOfNestedOptionalArrayThatContainsAnObjectIsConsideredMissing() {
		List<FieldDescriptor> descriptors = Arrays.asList(new FieldDescriptor("a.[].b").optional(),
				new FieldDescriptor("a.[].b.[]").optional(), new FieldDescriptor("a.[].b.[].c"));
		List<FieldDescriptor> missingFields = new JsonContentHandler("{\"a\":[{\"b\":[{}]}]}".getBytes(), descriptors)
			.findMissingFields();
		assertThat(missingFields.size()).isEqualTo(1);
		assertThat(missingFields.get(0).getPath()).isEqualTo("a.[].b.[].c");
	}

	@Test
	void describedMissingFieldThatIsChildOfOptionalObjectThatIsNullIsNotConsideredMissing() {
		List<FieldDescriptor> descriptors = Arrays.asList(new FieldDescriptor("a").optional(),
				new FieldDescriptor("a.b"));
		List<FieldDescriptor> missingFields = new JsonContentHandler("{\"a\":null}".getBytes(), descriptors)
			.findMissingFields();
		assertThat(missingFields.size()).isEqualTo(0);
	}

}

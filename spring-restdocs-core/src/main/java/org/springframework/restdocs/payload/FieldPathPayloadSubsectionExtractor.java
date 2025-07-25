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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldProcessor.ExtractedField;

/**
 * A {@link PayloadSubsectionExtractor} that extracts the subsection of the JSON payload
 * identified by a field path.
 *
 * @author Andy Wilkinson
 * @since 1.2.0
 * @see PayloadDocumentation#beneathPath(String)
 */
public class FieldPathPayloadSubsectionExtractor
		implements PayloadSubsectionExtractor<FieldPathPayloadSubsectionExtractor> {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private static final ObjectMapper prettyPrintingOjectMapper = JsonMapper.builder()
		.enable(SerializationFeature.INDENT_OUTPUT)
		.build();

	private final String fieldPath;

	private final String subsectionId;

	/**
	 * Creates a new {@code FieldPathPayloadSubsectionExtractor} that will extract the
	 * subsection of the JSON payload beneath the given {@code fieldPath}. The
	 * {@code fieldPath} prefixed with {@code beneath-} with be used as the subsection ID.
	 * @param fieldPath the path of the field
	 */
	protected FieldPathPayloadSubsectionExtractor(String fieldPath) {
		this(fieldPath, "beneath-" + fieldPath);
	}

	/**
	 * Creates a new {@code FieldPathPayloadSubsectionExtractor} that will extract the
	 * subsection of the JSON payload beneath the given {@code fieldPath} and that will
	 * use the given {@code subsectionId} to identify the subsection.
	 * @param fieldPath the path of the field
	 * @param subsectionId the ID of the subsection
	 */
	protected FieldPathPayloadSubsectionExtractor(String fieldPath, String subsectionId) {
		this.fieldPath = fieldPath;
		this.subsectionId = subsectionId;
	}

	@Override
	public byte[] extractSubsection(byte[] payload, @Nullable MediaType contentType) {
		return extractSubsection(payload, contentType, Collections.emptyList());
	}

	@Override
	public byte[] extractSubsection(byte[] payload, @Nullable MediaType contentType,
			List<FieldDescriptor> descriptors) {
		try {
			ExtractedField extractedField = new JsonFieldProcessor().extract(this.fieldPath,
					objectMapper.readValue(payload, Object.class));
			Object value = extractedField.getValue();
			if (value == ExtractedField.ABSENT) {
				throw new PayloadHandlingException(this.fieldPath + " does not identify a section of the payload");
			}
			Map<JsonFieldPath, FieldDescriptor> descriptorsByPath = descriptors.stream()
				.collect(Collectors.toMap(
						(descriptor) -> JsonFieldPath.compile(this.fieldPath + "." + descriptor.getPath()),
						this::prependFieldPath));
			if (value instanceof List) {
				List<?> extractedList = (List<?>) value;
				if (extractedList.isEmpty()) {
					throw new PayloadHandlingException(this.fieldPath + " identifies an empty section of the payload");
				}
				JsonContentHandler contentHandler = new JsonContentHandler(payload, descriptorsByPath.values());
				Set<JsonFieldPath> uncommonPaths = JsonFieldPaths.from(extractedList)
					.getUncommon()
					.stream()
					.map((path) -> JsonFieldPath
						.compile((path.equals("")) ? this.fieldPath : this.fieldPath + "." + path))
					.filter((path) -> {
						FieldDescriptor descriptorForPath = descriptorsByPath.getOrDefault(path,
								new FieldDescriptor(path.toString()));
						return contentHandler.isMissing(descriptorForPath);
					})
					.collect(Collectors.toSet());
				if (uncommonPaths.isEmpty()) {
					value = extractedList.get(0);
				}
				else {
					String message = this.fieldPath + " identifies multiple sections of "
							+ "the payload and they do not have a common structure. The "
							+ "following non-optional uncommon paths were found: ";
					message += uncommonPaths.stream()
						.map(JsonFieldPath::toString)
						.collect(Collectors.toCollection(TreeSet::new));
					throw new PayloadHandlingException(message);
				}
			}
			return getObjectMapper(payload).writeValueAsBytes(value);
		}
		catch (JacksonException ex) {
			throw new PayloadHandlingException(ex);
		}
	}

	private FieldDescriptor prependFieldPath(FieldDescriptor original) {
		FieldDescriptor prefixed = new FieldDescriptor(this.fieldPath + "." + original.getPath());
		if (original.isOptional()) {
			prefixed.optional();
		}
		return prefixed;
	}

	@Override
	public String getSubsectionId() {
		return this.subsectionId;
	}

	/**
	 * Returns the path of the field that will be extracted.
	 * @return the path of the field
	 */
	protected String getFieldPath() {
		return this.fieldPath;
	}

	@Override
	public FieldPathPayloadSubsectionExtractor withSubsectionId(String subsectionId) {
		return new FieldPathPayloadSubsectionExtractor(this.fieldPath, subsectionId);
	}

	private ObjectMapper getObjectMapper(byte[] payload) {
		if (isPrettyPrinted(payload)) {
			return prettyPrintingOjectMapper;
		}
		return objectMapper;
	}

	private boolean isPrettyPrinted(byte[] payload) {
		for (byte b : payload) {
			if (b == '\n') {
				return true;
			}
		}
		return false;
	}

}

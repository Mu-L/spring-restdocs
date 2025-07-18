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

package org.springframework.restdocs.headers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.Snippet;

/**
 * A {@link Snippet} that documents the headers in a request.
 *
 * @author Andreas Evers
 * @author Andy Wilkinson
 * @see HeaderDocumentation#requestHeaders(HeaderDescriptor...)
 * @see HeaderDocumentation#requestHeaders(Map, HeaderDescriptor...)
 */
public class RequestHeadersSnippet extends AbstractHeadersSnippet {

	/**
	 * Creates a new {@code RequestHeadersSnippet} that will document the headers in the
	 * request using the given {@code descriptors}.
	 * @param descriptors the descriptors
	 */
	protected RequestHeadersSnippet(List<HeaderDescriptor> descriptors) {
		this(descriptors, null);
	}

	/**
	 * Creates a new {@code RequestHeadersSnippet} that will document the headers in the
	 * request using the given {@code descriptors}. The given {@code attributes} will be
	 * included in the model during template rendering.
	 * @param descriptors the descriptors
	 * @param attributes the additional attributes
	 */
	protected RequestHeadersSnippet(List<HeaderDescriptor> descriptors, @Nullable Map<String, Object> attributes) {
		super("request", descriptors, attributes);
	}

	@Override
	protected Set<String> extractActualHeaders(Operation operation) {
		return operation.getRequest().getHeaders().headerNames();
	}

	/**
	 * Returns a new {@code RequestHeadersSnippet} configured with this snippet's
	 * attributes and its descriptors combined with the given
	 * {@code additionalDescriptors}.
	 * @param additionalDescriptors the additional descriptors
	 * @return the new snippet
	 */
	public final RequestHeadersSnippet and(HeaderDescriptor... additionalDescriptors) {
		return and(Arrays.asList(additionalDescriptors));
	}

	/**
	 * Returns a new {@code RequestHeadersSnippet} configured with this snippet's
	 * attributes and its descriptors combined with the given
	 * {@code additionalDescriptors}.
	 * @param additionalDescriptors the additional descriptors
	 * @return the new snippet
	 */
	public final RequestHeadersSnippet and(List<HeaderDescriptor> additionalDescriptors) {
		List<HeaderDescriptor> combinedDescriptors = new ArrayList<>(this.getHeaderDescriptors());
		combinedDescriptors.addAll(additionalDescriptors);
		return new RequestHeadersSnippet(combinedDescriptors, getAttributes());
	}

}

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

package org.springframework.restdocs.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.restdocs.operation.FormParameters;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.restdocs.snippet.SnippetException;

/**
 * A {@link Snippet} that documents the form parameters supported by a RESTful resource.
 *
 * @author Andy Wilkinson
 * @since 3.0.0
 * @see RequestDocumentation#formParameters(ParameterDescriptor...)
 * @see RequestDocumentation#formParameters(Map, ParameterDescriptor...)
 */
public class FormParametersSnippet extends AbstractParametersSnippet {

	/**
	 * Creates a new {@code FormParametersSnippet} that will document the request's form
	 * parameters using the given {@code descriptors}. Undocumented parameters will
	 * trigger a failure.
	 * @param descriptors the parameter descriptors
	 */
	protected FormParametersSnippet(List<ParameterDescriptor> descriptors) {
		this(descriptors, null, false);
	}

	/**
	 * Creates a new {@code FormParametersSnippet} that will document the request's form
	 * parameters using the given {@code descriptors}. If
	 * {@code ignoreUndocumentedParameters} is {@code true}, undocumented parameters will
	 * be ignored and will not trigger a failure.
	 * @param descriptors the parameter descriptors
	 * @param ignoreUndocumentedParameters whether undocumented parameters should be
	 * ignored
	 */
	protected FormParametersSnippet(List<ParameterDescriptor> descriptors, boolean ignoreUndocumentedParameters) {
		this(descriptors, null, ignoreUndocumentedParameters);
	}

	/**
	 * Creates a new {@code FormParametersSnippet} that will document the request's form
	 * parameters using the given {@code descriptors}. The given {@code attributes} will
	 * be included in the model during template rendering. Undocumented parameters will
	 * trigger a failure.
	 * @param descriptors the parameter descriptors
	 * @param attributes the additional attributes
	 */
	protected FormParametersSnippet(List<ParameterDescriptor> descriptors, @Nullable Map<String, Object> attributes) {
		this(descriptors, attributes, false);
	}

	/**
	 * Creates a new {@code FormParametersSnippet} that will document the request's form
	 * parameters using the given {@code descriptors}. The given {@code attributes} will
	 * be included in the model during template rendering. If
	 * {@code ignoreUndocumentedParameters} is {@code true}, undocumented parameters will
	 * be ignored and will not trigger a failure.
	 * @param descriptors the parameter descriptors
	 * @param attributes the additional attributes
	 * @param ignoreUndocumentedParameters whether undocumented parameters should be
	 * ignored
	 */
	protected FormParametersSnippet(List<ParameterDescriptor> descriptors, @Nullable Map<String, Object> attributes,
			boolean ignoreUndocumentedParameters) {
		super("form-parameters", descriptors, attributes, ignoreUndocumentedParameters);
	}

	@Override
	protected void verificationFailed(Set<String> undocumentedParameters, Set<String> missingParameters) {
		String message = "";
		if (!undocumentedParameters.isEmpty()) {
			message += "Form parameters with the following names were not documented: " + undocumentedParameters;
		}
		if (!missingParameters.isEmpty()) {
			if (message.length() > 0) {
				message += ". ";
			}
			message += "Form parameters with the following names were not found in the request: " + missingParameters;
		}
		throw new SnippetException(message);
	}

	@Override
	protected Set<String> extractActualParameters(Operation operation) {
		return FormParameters.from(operation.getRequest()).keySet();
	}

	/**
	 * Returns a new {@code FormParametersSnippet} configured with this snippet's
	 * attributes and its descriptors combined with the given
	 * {@code additionalDescriptors}.
	 * @param additionalDescriptors the additional descriptors
	 * @return the new snippet
	 */
	public FormParametersSnippet and(ParameterDescriptor... additionalDescriptors) {
		return and(Arrays.asList(additionalDescriptors));
	}

	/**
	 * Returns a new {@code FormParametersSnippet} configured with this snippet's
	 * attributes and its descriptors combined with the given
	 * {@code additionalDescriptors}.
	 * @param additionalDescriptors the additional descriptors
	 * @return the new snippet
	 */
	public FormParametersSnippet and(List<ParameterDescriptor> additionalDescriptors) {
		List<ParameterDescriptor> combinedDescriptors = new ArrayList<>(getParameterDescriptors().values());
		combinedDescriptors.addAll(additionalDescriptors);
		return new FormParametersSnippet(combinedDescriptors, this.getAttributes(),
				this.isIgnoreUndocumentedParameters());
	}

}

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

package org.springframework.restdocs.operation.preprocess;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import org.springframework.http.MediaType;

/**
 * A {@link ContentModifier} that modifies the content by replacing occurrences of a
 * regular expression {@link Pattern}.
 *
 * @author Andy Wilkinson
 * @author Dewet Diener
 */
class PatternReplacingContentModifier implements ContentModifier {

	private final Pattern pattern;

	private final String replacement;

	private final Charset fallbackCharset;

	/**
	 * Creates a new {@link PatternReplacingContentModifier} that will replace occurrences
	 * of the given {@code pattern} with the given {@code replacement}. The content is
	 * handled using the charset from its content type. When no content type is specified
	 * the JVM's {@link Charset#defaultCharset() default charset is used}.
	 * @param pattern the pattern
	 * @param replacement the replacement
	 */
	PatternReplacingContentModifier(Pattern pattern, String replacement) {
		this(pattern, replacement, Charset.defaultCharset());
	}

	/**
	 * Creates a new {@link PatternReplacingContentModifier} that will replace occurrences
	 * of the given {@code pattern} with the given {@code replacement}. The content is
	 * handled using the charset from its content type. When no content type is specified
	 * the given {@code fallbackCharset} is used.
	 * @param pattern the pattern
	 * @param replacement the replacement
	 * @param fallbackCharset the charset to use as a fallback
	 */
	PatternReplacingContentModifier(Pattern pattern, String replacement, Charset fallbackCharset) {
		this.pattern = pattern;
		this.replacement = replacement;
		this.fallbackCharset = fallbackCharset;
	}

	@Override
	public byte[] modifyContent(byte[] content, @Nullable MediaType contentType) {
		Charset charset = (contentType != null && contentType.getCharset() != null) ? contentType.getCharset()
				: this.fallbackCharset;
		String original = new String(content, charset);
		Matcher matcher = this.pattern.matcher(original);
		StringBuilder builder = new StringBuilder();
		int previous = 0;
		while (matcher.find()) {
			String prefix;
			if (matcher.groupCount() > 0) {
				prefix = original.substring(previous, matcher.start(1));
				previous = matcher.end(1);
			}
			else {
				prefix = original.substring(previous, matcher.start());
				previous = matcher.end();
			}
			builder.append(prefix);
			builder.append(this.replacement);
		}
		if (previous < original.length()) {
			builder.append(original.substring(previous));
		}
		return builder.toString().getBytes(charset);
	}

}

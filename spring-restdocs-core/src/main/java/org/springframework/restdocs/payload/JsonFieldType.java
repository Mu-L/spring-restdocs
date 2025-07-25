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

import java.util.Locale;

import org.springframework.util.StringUtils;

/**
 * An enumeration of the possible types for a field in a JSON request or response payload.
 *
 * @author Andy Wilkinson
 */
public enum JsonFieldType {

	/**
	 * An array.
	 */
	ARRAY,

	/**
	 * A boolean value.
	 */
	BOOLEAN,

	/**
	 * An object (map).
	 */
	OBJECT,

	/**
	 * A number.
	 */
	NUMBER,

	/**
	 * {@code null}.
	 */
	NULL,

	/**
	 * A string.
	 */
	STRING,

	/**
	 * A variety of different types.
	 */
	VARIES;

	@Override
	public String toString() {
		return StringUtils.capitalize(this.name().toLowerCase(Locale.ENGLISH));
	}

}

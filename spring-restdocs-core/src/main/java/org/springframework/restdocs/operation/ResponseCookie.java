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

package org.springframework.restdocs.operation;

/**
 * A representation of a Cookie returned in a response.
 *
 * @author Clyde Stubbs
 * @since 3.0
 */
public final class ResponseCookie {

	private final String name;

	private final String value;

	/**
	 * Creates a new {@code ResponseCookie} with the given {@code name} and {@code value}.
	 * @param name the name of the cookie
	 * @param value the value of the cookie
	 */
	public ResponseCookie(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Returns the name of the cookie.
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the value of the cookie.
	 * @return the value
	 */
	public String getValue() {
		return this.value;
	}

}

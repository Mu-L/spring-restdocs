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

package org.springframework.restdocs.hypermedia;

import org.jspecify.annotations.Nullable;

import org.springframework.core.style.ToStringCreator;

/**
 * Representation of a link used in a Hypermedia-based API.
 *
 * @author Andy Wilkinson
 */
public class Link {

	private final String rel;

	private final String href;

	private final @Nullable String title;

	/**
	 * Creates a new {@code Link} with the given {@code rel} and {@code href}.
	 * @param rel the link's rel
	 * @param href the link's href
	 */
	public Link(String rel, String href) {
		this(rel, href, null);
	}

	/**
	 * Creates a new {@code Link} with the given {@code rel}, {@code href}, and
	 * {@code title}.
	 * @param rel the link's rel
	 * @param href the link's href
	 * @param title the link's title
	 */
	public Link(String rel, String href, @Nullable String title) {
		this.rel = rel;
		this.href = href;
		this.title = title;
	}

	/**
	 * Returns the link's {@code rel}.
	 * @return the link's {@code rel}
	 */
	public String getRel() {
		return this.rel;
	}

	/**
	 * Returns the link's {@code href}.
	 * @return the link's {@code href}
	 */
	public String getHref() {
		return this.href;
	}

	/**
	 * Returns the link's {@code title}, or {@code null} if it does not have a title.
	 * @return the link's {@code title} or {@code null}
	 */
	public @Nullable String getTitle() {
		return this.title;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Link other = (Link) obj;
		if (!this.href.equals(other.href)) {
			return false;
		}
		if (!this.rel.equals(other.rel)) {
			return false;
		}
		if (this.title == null) {
			if (other.title != null) {
				return false;
			}
		}
		else if (!this.title.equals(other.title)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.href.hashCode();
		result = prime * result + this.rel.hashCode();
		result = prime * result + ((this.title == null) ? 0 : this.title.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("rel", this.rel)
			.append("href", this.href)
			.append("title", this.title)
			.toString();
	}

}

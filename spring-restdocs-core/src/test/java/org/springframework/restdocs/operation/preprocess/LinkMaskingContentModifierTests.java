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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.restdocs.hypermedia.Link;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LinkMaskingContentModifier}.
 *
 * @author Andy Wilkinson
 *
 */
class LinkMaskingContentModifierTests {

	private final ContentModifier contentModifier = new LinkMaskingContentModifier();

	private final Link[] links = new Link[] { new Link("a", "alpha"), new Link("b", "bravo") };

	private final Link[] maskedLinks = new Link[] { new Link("a", "..."), new Link("b", "...") };

	@Test
	void halLinksAreMasked() throws Exception {
		assertThat(this.contentModifier.modifyContent(halPayloadWithLinks(this.links), null))
			.isEqualTo(halPayloadWithLinks(this.maskedLinks));
	}

	@Test
	void formattedHalLinksAreMasked() throws Exception {
		assertThat(this.contentModifier.modifyContent(formattedHalPayloadWithLinks(this.links), null))
			.isEqualTo(formattedHalPayloadWithLinks(this.maskedLinks));
	}

	@Test
	void atomLinksAreMasked() throws Exception {
		assertThat(this.contentModifier.modifyContent(atomPayloadWithLinks(this.links), null))
			.isEqualTo(atomPayloadWithLinks(this.maskedLinks));
	}

	@Test
	void formattedAtomLinksAreMasked() throws Exception {
		assertThat(this.contentModifier.modifyContent(formattedAtomPayloadWithLinks(this.links), null))
			.isEqualTo(formattedAtomPayloadWithLinks(this.maskedLinks));
	}

	@Test
	void maskCanBeCustomized() throws Exception {
		assertThat(
				new LinkMaskingContentModifier("custom").modifyContent(formattedAtomPayloadWithLinks(this.links), null))
			.isEqualTo(formattedAtomPayloadWithLinks(new Link("a", "custom"), new Link("b", "custom")));
	}

	@Test
	void maskCanUseUtf8Characters() throws Exception {
		String ellipsis = "\u2026";
		assertThat(
				new LinkMaskingContentModifier(ellipsis).modifyContent(formattedHalPayloadWithLinks(this.links), null))
			.isEqualTo(formattedHalPayloadWithLinks(new Link("a", ellipsis), new Link("b", ellipsis)));
	}

	private byte[] atomPayloadWithLinks(Link... links) throws JacksonException {
		return new ObjectMapper().writeValueAsBytes(createAtomPayload(links));
	}

	private byte[] formattedAtomPayloadWithLinks(Link... links) throws JacksonException {
		return JsonMapper.builder()
			.enable(SerializationFeature.INDENT_OUTPUT)
			.build()
			.writeValueAsBytes(createAtomPayload(links));
	}

	private AtomPayload createAtomPayload(Link... links) {
		AtomPayload payload = new AtomPayload();
		payload.setLinks(Arrays.asList(links));
		return payload;
	}

	private byte[] halPayloadWithLinks(Link... links) throws JacksonException {
		return new ObjectMapper().writeValueAsBytes(createHalPayload(links));
	}

	private byte[] formattedHalPayloadWithLinks(Link... links) throws JacksonException {
		return JsonMapper.builder()
			.enable(SerializationFeature.INDENT_OUTPUT)
			.build()
			.writeValueAsBytes(createHalPayload(links));
	}

	private HalPayload createHalPayload(Link... links) {
		HalPayload payload = new HalPayload();
		Map<String, Object> linksMap = new LinkedHashMap<>();
		for (Link link : links) {
			Map<String, String> linkMap = new HashMap<>();
			linkMap.put("href", link.getHref());
			linksMap.put(link.getRel(), linkMap);
		}
		payload.setLinks(linksMap);
		return payload;
	}

	public static final class AtomPayload {

		private List<Link> links;

		public List<Link> getLinks() {
			return this.links;
		}

		public void setLinks(List<Link> links) {
			this.links = links;
		}

	}

	public static final class HalPayload {

		private Map<String, Object> links;

		@JsonProperty("_links")
		public Map<String, Object> getLinks() {
			return this.links;
		}

		public void setLinks(Map<String, Object> links) {
			this.links = links;
		}

	}

}

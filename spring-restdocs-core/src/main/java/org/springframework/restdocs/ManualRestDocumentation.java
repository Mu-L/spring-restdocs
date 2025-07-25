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

package org.springframework.restdocs;

import java.io.File;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.Extension;

import org.springframework.util.Assert;

/**
 * {@code ManualRestDocumentation} is used to manually manage the
 * {@link RestDocumentationContext}. Primarly intended for use with TestNG, but suitable
 * for use in any environment where manual management of the context is required.
 * <p>
 * Users of JUnit should use {@link RestDocumentationExtension} and take advantage of its
 * {@link Extension}-based support for automatic management of the context.
 *
 * @author Andy Wilkinson
 * @since 1.1.0
 */
public final class ManualRestDocumentation implements RestDocumentationContextProvider {

	private final File outputDirectory;

	private @Nullable StandardRestDocumentationContext context;

	/**
	 * Creates a new {@code ManualRestDocumentation} instance that will generate snippets
	 * to &lt;gradle/maven build path&gt;/generated-snippets.
	 */
	public ManualRestDocumentation() {
		this(getDefaultOutputDirectory());
	}

	/**
	 * Creates a new {@code ManualRestDocumentation} instance that will generate snippets
	 * to the given {@code outputDirectory}.
	 * @param outputDirectory the output directory
	 */
	public ManualRestDocumentation(String outputDirectory) {
		this(new File(outputDirectory));
	}

	private ManualRestDocumentation(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	/**
	 * Notification that a test is about to begin. Creates a
	 * {@link RestDocumentationContext} for the test on the given {@code testClass} with
	 * the given {@code testMethodName}. Must be followed by a call to
	 * {@link #afterTest()} once the test has completed.
	 * @param testClass the test class
	 * @param testMethodName the name of the test method
	 * @throws IllegalStateException if a context has already be created
	 */
	public void beforeTest(Class<?> testClass, String testMethodName) {
		Assert.isNull(this.context, () -> "Context already exists. Did you forget to call afterTest()?");
		this.context = new StandardRestDocumentationContext(testClass, testMethodName, this.outputDirectory);
	}

	/**
	 * Notification that a test has completed. Clears the {@link RestDocumentationContext}
	 * that was previously established by a call to {@link #beforeTest(Class, String)}.
	 */
	public void afterTest() {
		this.context = null;
	}

	@Override
	public RestDocumentationContext beforeOperation() {
		Assert.notNull(this.context, () -> "Context is null. Did you forget to call beforeTest(Class, String)?");
		this.context.getAndIncrementStepCount();
		return this.context;
	}

	private static File getDefaultOutputDirectory() {
		if (new File("pom.xml").exists()) {
			return new File("target/generated-snippets");
		}
		return new File("build/generated-snippets");
	}

}

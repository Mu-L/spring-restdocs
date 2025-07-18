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

package org.springframework.restdocs.constraints;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

import javax.money.MonetaryAmount;

import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.CodePointLength;
import org.hibernate.validator.constraints.CreditCardNumber;
import org.hibernate.validator.constraints.Currency;
import org.hibernate.validator.constraints.EAN;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.LuhnCheck;
import org.hibernate.validator.constraints.Mod10Check;
import org.hibernate.validator.constraints.Mod11Check;
import org.hibernate.validator.constraints.Range;
import org.junit.jupiter.api.Test;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ResourceBundleConstraintDescriptionResolver}.
 *
 * @author Andy Wilkinson
 */
class ResourceBundleConstraintDescriptionResolverTests {

	private final ResourceBundleConstraintDescriptionResolver resolver = new ResourceBundleConstraintDescriptionResolver();

	@Test
	void defaultMessageAssertFalse() {
		assertThat(constraintDescriptionForField("assertFalse")).isEqualTo("Must be false");
	}

	@Test
	void defaultMessageAssertTrue() {
		assertThat(constraintDescriptionForField("assertTrue")).isEqualTo("Must be true");
	}

	@Test
	void defaultMessageCodePointLength() {
		assertThat(constraintDescriptionForField("codePointLength"))
			.isEqualTo("Code point length must be between 2 and 5 inclusive");
	}

	@Test
	void defaultMessageCurrency() {
		assertThat(constraintDescriptionForField("currency"))
			.isEqualTo("Must be in an accepted currency unit (GBP, USD)");
	}

	@Test
	void defaultMessageDecimalMax() {
		assertThat(constraintDescriptionForField("decimalMax")).isEqualTo("Must be at most 9.875");
	}

	@Test
	void defaultMessageDecimalMin() {
		assertThat(constraintDescriptionForField("decimalMin")).isEqualTo("Must be at least 1.5");
	}

	@Test
	void defaultMessageDigits() {
		assertThat(constraintDescriptionForField("digits"))
			.isEqualTo("Must have at most 2 integral digits and 5 fractional digits");
	}

	@Test
	void defaultMessageFuture() {
		assertThat(constraintDescriptionForField("future")).isEqualTo("Must be in the future");
	}

	@Test
	void defaultMessageFutureOrPresent() {
		assertThat(constraintDescriptionForField("futureOrPresent")).isEqualTo("Must be in the future or the present");
	}

	@Test
	void defaultMessageMax() {
		assertThat(constraintDescriptionForField("max")).isEqualTo("Must be at most 10");
	}

	@Test
	void defaultMessageMin() {
		assertThat(constraintDescriptionForField("min")).isEqualTo("Must be at least 10");
	}

	@Test
	void defaultMessageNotNull() {
		assertThat(constraintDescriptionForField("notNull")).isEqualTo("Must not be null");
	}

	@Test
	void defaultMessageNull() {
		assertThat(constraintDescriptionForField("nul")).isEqualTo("Must be null");
	}

	@Test
	void defaultMessagePast() {
		assertThat(constraintDescriptionForField("past")).isEqualTo("Must be in the past");
	}

	@Test
	void defaultMessagePastOrPresent() {
		assertThat(constraintDescriptionForField("pastOrPresent")).isEqualTo("Must be in the past or the present");
	}

	@Test
	void defaultMessagePattern() {
		assertThat(constraintDescriptionForField("pattern"))
			.isEqualTo("Must match the regular expression `[A-Z][a-z]+`");
	}

	@Test
	void defaultMessageSize() {
		assertThat(constraintDescriptionForField("size")).isEqualTo("Size must be between 2 and 10 inclusive");
	}

	@Test
	void defaultMessageCreditCardNumber() {
		assertThat(constraintDescriptionForField("creditCardNumber"))
			.isEqualTo("Must be a well-formed credit card number");
	}

	@Test
	void defaultMessageEan() {
		assertThat(constraintDescriptionForField("ean")).isEqualTo("Must be a well-formed EAN13 number");
	}

	@Test
	void defaultMessageEmail() {
		assertThat(constraintDescriptionForField("email")).isEqualTo("Must be a well-formed email address");
	}

	@Test
	void defaultMessageLength() {
		assertThat(constraintDescriptionForField("length")).isEqualTo("Length must be between 2 and 10 inclusive");
	}

	@Test
	void defaultMessageLuhnCheck() {
		assertThat(constraintDescriptionForField("luhnCheck"))
			.isEqualTo("Must pass the Luhn Modulo 10 checksum algorithm");
	}

	@Test
	void defaultMessageMod10Check() {
		assertThat(constraintDescriptionForField("mod10Check")).isEqualTo("Must pass the Mod10 checksum algorithm");
	}

	@Test
	void defaultMessageMod11Check() {
		assertThat(constraintDescriptionForField("mod11Check")).isEqualTo("Must pass the Mod11 checksum algorithm");
	}

	@Test
	void defaultMessageNegative() {
		assertThat(constraintDescriptionForField("negative")).isEqualTo("Must be negative");
	}

	@Test
	void defaultMessageNegativeOrZero() {
		assertThat(constraintDescriptionForField("negativeOrZero")).isEqualTo("Must be negative or zero");
	}

	@Test
	void defaultMessageNotBlank() {
		assertThat(constraintDescriptionForField("notBlank")).isEqualTo("Must not be blank");
	}

	@Test
	void defaultMessageNotEmpty() {
		assertThat(constraintDescriptionForField("notEmpty")).isEqualTo("Must not be empty");
	}

	@Test
	void defaultMessageNotEmptyHibernateValidator() {
		assertThat(constraintDescriptionForField("notEmpty")).isEqualTo("Must not be empty");
	}

	@Test
	void defaultMessagePositive() {
		assertThat(constraintDescriptionForField("positive")).isEqualTo("Must be positive");
	}

	@Test
	void defaultMessagePositiveOrZero() {
		assertThat(constraintDescriptionForField("positiveOrZero")).isEqualTo("Must be positive or zero");
	}

	@Test
	void defaultMessageRange() {
		assertThat(constraintDescriptionForField("range")).isEqualTo("Must be at least 10 and at most 100");
	}

	@Test
	void defaultMessageUrl() {
		assertThat(constraintDescriptionForField("url")).isEqualTo("Must be a well-formed URL");
	}

	@Test
	void customMessage() {
		Thread.currentThread().setContextClassLoader(new ClassLoader() {

			@Override
			public URL getResource(String name) {
				if (name.startsWith("org/springframework/restdocs/constraints/ConstraintDescriptions")) {
					return super.getResource(
							"org/springframework/restdocs/constraints/TestConstraintDescriptions.properties");
				}
				return super.getResource(name);
			}

		});

		try {
			String description = new ResourceBundleConstraintDescriptionResolver()
				.resolveDescription(new Constraint(NotNull.class.getName(), Collections.<String, Object>emptyMap()));
			assertThat(description).isEqualTo("Should not be null");

		}
		finally {
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		}
	}

	@Test
	void customResourceBundle() {
		ResourceBundle bundle = new ListResourceBundle() {

			@Override
			protected Object[][] getContents() {
				return new String[][] { { NotNull.class.getName() + ".description", "Not null" } };
			}

		};
		String description = new ResourceBundleConstraintDescriptionResolver(bundle)
			.resolveDescription(new Constraint(NotNull.class.getName(), Collections.<String, Object>emptyMap()));
		assertThat(description).isEqualTo("Not null");
	}

	@Test
	void allBeanValidationConstraintsAreTested() throws Exception {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = resolver.getResources("jakarta/validation/constraints/*.class");
		Set<Class<?>> beanValidationConstraints = new HashSet<>();
		for (Resource resource : resources) {
			String className = ClassUtils.convertResourcePathToClassName(((ClassPathResource) resource).getPath());
			if (className.endsWith(".class")) {
				className = className.substring(0, className.length() - 6);
			}
			Class<?> type = Class.forName(className);
			if (type.isAnnotation() && type.isAnnotationPresent(jakarta.validation.Constraint.class)) {
				beanValidationConstraints.add(type);
			}
		}
		ReflectionUtils.doWithFields(Constrained.class, (field) -> {
			for (Annotation annotation : field.getAnnotations()) {
				beanValidationConstraints.remove(annotation.annotationType());
			}
		});
		assertThat(beanValidationConstraints).isEmpty();
	}

	private String constraintDescriptionForField(String name) {
		return this.resolver.resolveDescription(getConstraintFromField(name));
	}

	private Constraint getConstraintFromField(String name) {
		Annotation[] annotations = ReflectionUtils.findField(Constrained.class, name).getAnnotations();
		Assert.isTrue(annotations.length == 1,
				"The field '" + name + "' must have " + "exactly one @Constrained annotation");
		return new Constraint(annotations[0].annotationType().getName(),
				AnnotationUtils.getAnnotationAttributes(annotations[0]));
	}

	private static final class Constrained {

		@AssertFalse
		private boolean assertFalse;

		@AssertTrue
		private boolean assertTrue;

		@CodePointLength(min = 2, max = 5)
		private String codePointLength;

		@Currency({ "GBP", "USD" })
		private MonetaryAmount currency;

		@DecimalMax("9.875")
		private BigDecimal decimalMax;

		@DecimalMin("1.5")
		private BigDecimal decimalMin;

		@Digits(integer = 2, fraction = 5)
		private String digits;

		@Future
		private Date future;

		@FutureOrPresent
		private Date futureOrPresent;

		@Max(10)
		private int max;

		@Min(10)
		private int min;

		@NotNull
		private String notNull;

		@Null
		private String nul;

		@Past
		private Date past;

		@PastOrPresent
		private Date pastOrPresent;

		@Pattern(regexp = "[A-Z][a-z]+")
		private String pattern;

		@Size(min = 2, max = 10)
		private List<String> size;

		@CreditCardNumber
		private String creditCardNumber;

		@EAN
		private String ean;

		@Email
		private String email;

		@Length(min = 2, max = 10)
		private String length;

		@LuhnCheck
		private String luhnCheck;

		@Mod10Check
		private String mod10Check;

		@Mod11Check
		private String mod11Check;

		@Negative
		private int negative;

		@NegativeOrZero
		private int negativeOrZero;

		@NotBlank
		private String notBlank;

		@NotEmpty
		private String notEmpty;

		@Positive
		private int positive;

		@PositiveOrZero
		private int positiveOrZero;

		@Range(min = 10, max = 100)
		private int range;

		@org.hibernate.validator.constraints.URL
		private String url;

	}

}

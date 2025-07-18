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

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
import org.hibernate.validator.constraints.URL;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;
import org.springframework.util.StringUtils;

/**
 * A {@link ConstraintDescriptionResolver} that resolves constraint descriptions from a
 * {@link ResourceBundle}. The resource bundle's keys are the name of the constraint with
 * {@code .description} appended. For example, the key for the constraint named
 * {@code jakarta.validation.constraints.NotNull} is
 * {@code jakarta.validation.constraints.NotNull.description}.
 * <p>
 * Default descriptions are provided for all of Bean Validation 3.1's constraints:
 *
 * <ul>
 * <li>{@link AssertFalse}
 * <li>{@link AssertTrue}
 * <li>{@link DecimalMax}
 * <li>{@link DecimalMin}
 * <li>{@link Digits}
 * <li>{@link Email}
 * <li>{@link Future}
 * <li>{@link FutureOrPresent}
 * <li>{@link Max}
 * <li>{@link Min}
 * <li>{@link Negative}
 * <li>{@link NegativeOrZero}
 * <li>{@link NotBlank}
 * <li>{@link NotEmpty}
 * <li>{@link NotNull}
 * <li>{@link Null}
 * <li>{@link Past}
 * <li>{@link PastOrPresent}
 * <li>{@link Pattern}
 * <li>{@link Positive}
 * <li>{@link PositiveOrZero}
 * <li>{@link Size}
 * </ul>
 *
 * <p>
 * Default descriptions are also provided for the following Hibernate Validator
 * constraints:
 *
 * <ul>
 * <li>{@link CodePointLength}
 * <li>{@link CreditCardNumber}
 * <li>{@link Currency}
 * <li>{@link EAN}
 * <li>{@link Length}
 * <li>{@link LuhnCheck}
 * <li>{@link Mod10Check}
 * <li>{@link Mod11Check}
 * <li>{@link Range}
 * <li>{@link URL}
 * </ul>
 *
 * @author Andy Wilkinson
 */
public class ResourceBundleConstraintDescriptionResolver implements ConstraintDescriptionResolver {

	private final PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("${", "}");

	private final ResourceBundle defaultDescriptions = getDefaultDescriptions();

	private final @Nullable ResourceBundle userDescriptions;

	/**
	 * Creates a new {@code ResourceBundleConstraintDescriptionResolver} that will resolve
	 * descriptions by looking them up in a resource bundle with the base name
	 * {@code org.springframework.restdocs.constraints.ConstraintDescriptions} in the
	 * default locale loaded using the thread context class loader.
	 */
	public ResourceBundleConstraintDescriptionResolver() {
		this.userDescriptions = getBundle("ConstraintDescriptions");
	}

	/**
	 * Creates a new {@code ResourceBundleConstraintDescriptionResolver} that will resolve
	 * descriptions by looking them up in the given {@code resourceBundle}.
	 * @param resourceBundle the resource bundle
	 */
	public ResourceBundleConstraintDescriptionResolver(ResourceBundle resourceBundle) {
		this.userDescriptions = resourceBundle;
	}

	private static ResourceBundle getDefaultDescriptions() {
		ResourceBundle bundle = getBundle("DefaultConstraintDescriptions");
		Assert.notNull(bundle, () -> "Failed to load default constraint descriptions");
		return bundle;
	}

	private static @Nullable ResourceBundle getBundle(String name) {
		try {
			return ResourceBundle.getBundle(
					ResourceBundleConstraintDescriptionResolver.class.getPackage().getName() + "." + name,
					Locale.getDefault(), Thread.currentThread().getContextClassLoader());
		}
		catch (MissingResourceException ex) {
			return null;
		}
	}

	@Override
	public String resolveDescription(Constraint constraint) {
		String key = constraint.getName() + ".description";
		return this.propertyPlaceholderHelper.replacePlaceholders(getDescription(key),
				new ConstraintPlaceholderResolver(constraint));
	}

	private String getDescription(String key) {
		try {
			if (this.userDescriptions != null) {
				return this.userDescriptions.getString(key);
			}
		}
		catch (MissingResourceException ex) {
			// Continue and return default description, if available
		}
		return this.defaultDescriptions.getString(key);
	}

	private static final class ConstraintPlaceholderResolver implements PlaceholderResolver {

		private final Constraint constraint;

		private ConstraintPlaceholderResolver(Constraint constraint) {
			this.constraint = constraint;
		}

		@Override
		public @Nullable String resolvePlaceholder(String placeholderName) {
			Object replacement = this.constraint.getConfiguration().get(placeholderName);
			if (replacement == null) {
				return null;
			}
			if (replacement.getClass().isArray()) {
				return StringUtils.arrayToDelimitedString((Object[]) replacement, ", ");
			}
			return replacement.toString();
		}

	}

}

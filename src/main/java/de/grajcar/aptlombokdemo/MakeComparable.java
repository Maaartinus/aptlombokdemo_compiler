package de.grajcar.aptlombokdemo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates an implementation for the {@code compareTo} method, consisting of comparing the values of relevant fields
 * as customized via {@link MakeComparable.Include} and {@link MakeComparable.Exclude}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface MakeComparable {
	/**
	 * Exclude a field from appearing in {@link MakeComparable}.
	 * When put on a class, exclude all its fields by default.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@Target({ElementType.FIELD, ElementType.TYPE})
	public @interface Exclude {
	}

	/**
	 * Include a field or (the result of) a no-args method in {@link MakeComparable}.
	 *
	 * // TODO allow one-arg non-static int-returning methods taking a parameter of self-type
	 * // TODO allow two-args static int-returning methods taking two parameters of self-type
	 */
	@Retention(RetentionPolicy.SOURCE)
	@Target({ElementType.FIELD, ElementType.METHOD})
	public @interface Include {
		/** When true, then the reversed ordering gets used. */
		boolean reverse() default false;

		/**
		 * Fields and methods with numerically lower rank get compared earlier.
		 * Members with equal rank get compared in the order in which they appear in the source file.
		 */
		int rank() default 0;

		boolean nullsFirst() default false;

		boolean nullsLast() default false;
	}

	boolean nullsFirst() default false;

	boolean nullsLast() default false;
}

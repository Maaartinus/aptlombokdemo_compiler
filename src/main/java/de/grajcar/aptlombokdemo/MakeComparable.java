package de.grajcar.aptlombokdemo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface MakeComparable {
	@Retention(RetentionPolicy.SOURCE)
	@Target({ElementType.FIELD, ElementType.TYPE})
	public @interface Exclude {
	}

	@Retention(RetentionPolicy.SOURCE)
	@Target({ElementType.FIELD})
	public @interface Include {
		/** When true, then the reversed ordering gets used. */
		boolean reverse() default false;

		/**
		 * Fields and methods with numerically lower rank get compared earlier.
		 * Fields with equal rank get compared in the order in which they appear in the source file.
		 */
		int rank() default 0;
	}
}

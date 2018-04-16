package de.grajcar.aptlombokdemo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates an implementation for the {@code toString} method inherited by all objects, consisting of printing the values of relevant fields
 * as customized via {@link ToString.Include} and {@link ToString.Exclude}.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ToString {
	/**
	 * Exclude a field from appearing in {@link ToString}.
	 * When put on a class, exclude all its fields by default.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@Target({ElementType.FIELD, ElementType.TYPE})
	public @interface Exclude {
	}

	/**
	 * Include a field or (the result of) a no-args method in {@link ToString}.
	 */
	@Retention(RetentionPolicy.SOURCE)
	@Target({ElementType.FIELD, ElementType.METHOD})
	public @interface Include {
		/**
		 * Provide an alternate name for the class member (field or no-argument method).
		 * By default, the name is equals to the name of the field.
		 *
		 * @return The alternate name, if any; otherwise the empty string.
		 */
		String name() default "";
	}

	/**
	 * Include the name of each field when printing it.
	 * <strong>default: true</strong>
	 *
	 * @return Whether or not to include the names of fields in the string produced by the generated {@code toString()}.
	 */
	boolean includeFieldNames() default true;

	/**
	 * Include the result of the superclass's implementation of {@code toString} in the output.
	 * <strong>default: false</strong>
	 *
	 * @return Whether to call the superclass's {@code toString} implementation as part of the generated toString algorithm.
	 */
	boolean callSuper() default false; //TODO

	/**
	 * Normally, if getters are available, those are called. To suppress this and let the generated code use the fields directly, set this to {@code true}.
	 * <strong>default: false</strong>
	 *
	 * @return If {@code true}, always use direct field access instead of calling the getter method.
	 */
	boolean doNotUseGetters() default true; //TODO
}

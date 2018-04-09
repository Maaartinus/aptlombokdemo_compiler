package de.grajcar.aptlombokdemo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ToString {
	@Retention(RetentionPolicy.SOURCE)
	@Target({ElementType.FIELD, ElementType.TYPE})
	public @interface Exclude {
	}

	@Retention(RetentionPolicy.SOURCE)
	@Target({ElementType.FIELD, ElementType.METHOD})
	public @interface Include {
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

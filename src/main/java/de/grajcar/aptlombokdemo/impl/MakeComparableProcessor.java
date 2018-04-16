package de.grajcar.aptlombokdemo.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import de.grajcar.aptlombokdemo.MakeComparable;
import de.grajcar.aptlombokdemo.MakeComparable.Include;

public class MakeComparableProcessor extends TypeProcessor {
	MakeComparableProcessor(ProcessingEnvironment processingEnv, TypeElement typeElement) {
		super(processingEnv, typeElement);
		excludeByDefault = typeElement.getAnnotation(MakeComparable.Exclude.class) != null;
		makeComparableAnn = typeElement.getAnnotation(MakeComparable.class);
		if (makeComparableAnn.nullsFirst()) raiseWarning(typeElement, "nullsFirst is not implemented"); //TODO
		if (makeComparableAnn.nullsLast()) raiseWarning(typeElement, "nullsLast is not implemented"); //TODO
	}

	@Override protected List<Element> collectElements() {
		return typeElement.getEnclosedElements().stream().filter(this::accept).sorted(Comparator.comparingInt(this::getRank)).collect(Collectors.toList());
	}

	private int getRank(Element element) {
		final Include includeAnn = element.getAnnotation(MakeComparable.Include.class);
		return includeAnn==null ? 0 : includeAnn.rank();
	}

	private boolean accept(Element element) {
		final MakeComparable.Exclude excludeAnn = element.getAnnotation(MakeComparable.Exclude.class);
		final MakeComparable.Include includeAnn = element.getAnnotation(MakeComparable.Include.class);
		return accept(element, excludeAnn, includeAnn);
	}

	private boolean accept(Element element, MakeComparable.Exclude excludeAnn, MakeComparable.Include includeAnn) {
		if (includeAnn != null) {
			if (includeAnn.nullsFirst()) raiseWarning(typeElement, "nullsFirst is not implemented"); //TODO
			if (includeAnn.nullsLast()) raiseWarning(typeElement, "nullsLast is not implemented"); //TODO
		}
		if (excludeAnn!=null || includeAnn!=null) {
			final String errorMessage = errorMessageIfAnnotated(element);
			if (!errorMessage.isEmpty()) {
				raiseError(element, errorMessage);
				return false;
			}
		}
		if (excludeAnn != null) {
			if (includeAnn != null) raiseError(element, "Combining Include and Exclude on a single element is contradictory.");
			return false;
		}
		final boolean includedWhenUnanotated = !excludeByDefault && element instanceof VariableElement;

		if (includeAnn == null) return includedWhenUnanotated;

		if (includedWhenUnanotated && includeAnn.rank()==0 && includeAnn.reverse()==false) raiseWarning(element, "Needless @MakeComparable.Include");
		return true;
	}

	private String errorMessageIfAnnotated(Element element) {
		if (element.getModifiers().contains(Modifier.STATIC)) return "MakeComparable doesn't work with a static element.";
		if (element instanceof VariableElement) return "";
		if (element instanceof ExecutableElement) {
			final ExecutableElement executableElement = (ExecutableElement) element;
			if (!executableElement.getParameters().isEmpty()) return "MakeComparable doesn't work with a method with arguments.";
			return "";
		}
		return "Include and Exclude on this elements is forbidden.";
	}

	@Override protected String generatedClassName() {
		return packageName() + "._" + saneTypeName().replace(".", "_") + "_MakeComparableHelper";
	}

	@Override protected void intro(List<Element> elements) {
		append("public class ", generatedClassName().replaceAll(".*\\.", ""), " {");
		append("public static int compare(", typeElement.getQualifiedName(), " first, ", typeElement.getQualifiedName(), " second) {");
		append("int result = 0;");
	}

	@Override protected void outtro(List<Element> elements) {
		append("return result;");
		append("}");

		final String typeFqn = typeElement.getQualifiedName().toString();

		if (elements.stream().anyMatch(VariableElement.class::isInstance)) {
			append();
			append("private static ", Object.class, " getFieldValue(", String.class, " name, ", typeFqn, " object) {");
			append("try {");
			append(Field.class, " f = ", typeFqn, ".class.getDeclaredField(name);");
			append("f.setAccessible(true);");
			append("return f.get(object);");
			append("} catch (", ReflectiveOperationException.class, " e) {");
			append("throw new ", RuntimeException.class, "(e);");
			append("}");
			append("}");

			append();
			append("private static int compareField(", String.class, " name, ", typeFqn, " first, ", typeFqn, " second) {");
			append("return compareValue(getFieldValue(name, first), getFieldValue(name, second));");
			append("}");
		}

		if (elements.stream().anyMatch(ExecutableElement.class::isInstance)) {
			append();
			append("private static ", Object.class, " getMethodValue(", String.class, " name, ", typeFqn, " object) {");
			append("try {");
			append(Method.class, " m = ", typeFqn, ".class.getDeclaredMethod(name);");
			append("m.setAccessible(true);");
			append("return m.invoke(object);");
			append("} catch (", ReflectiveOperationException.class, " e) {");
			append("throw new ", RuntimeException.class, "(e);");
			append("}");
			append("}");


			append();
			append("private static int compareMethod(", String.class, " name, ", typeFqn, " first, ", typeFqn, " second) {");
			append("return compareValue(getMethodValue(name, first), getMethodValue(name, second));");
			append("}");
		}

		if (!elements.isEmpty()) {
			append();
			append("@", SuppressWarnings.class, "(\"all\")");
			append("private static int compareValue(", Object.class, " first, ", Object.class, " second) {");
			append("if (first == second) return 0;");
			append("Comparable<Object> a =(Comparable<Object>) first;");
			append("Comparable<Object> b =(Comparable<Object>) second;");
			append("return a.compareTo(b);");
			append("}");
		}

		append("}");
	}

	@Override protected void body(VariableElement element) {
		bodyInternal(element);
	}

	@Override protected void body(ExecutableElement element) {
		bodyInternal(element);
	}

	private void bodyInternal(Element element) {
		final String s = element instanceof VariableElement ? "Field" : "Method";
		if (!isFirst) append("if (result != 0) return result;");
		append("result = compare", s, "(\"", element.getSimpleName().toString(), "\", first, second);");
		isFirst = false;
	}

	private final boolean excludeByDefault;
	private final MakeComparable makeComparableAnn;
	private boolean isFirst = true;
}

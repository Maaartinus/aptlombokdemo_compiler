package de.grajcar.aptlombokdemo.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import de.grajcar.aptlombokdemo.ToString;

public class ToStringProcessor extends TypeProcessor {
	ToStringProcessor(ProcessingEnvironment processingEnv, TypeElement typeElement) {
		super(processingEnv, typeElement);
		excludeByDefault = typeElement.getAnnotation(ToString.Exclude.class) != null;
		toStringAnn = typeElement.getAnnotation(ToString.class);
	}

	@Override protected List<Element> collectElements() {
		final List<Element> collected = typeElement.getEnclosedElements().stream().filter(this::accept).collect(Collectors.toList());
		final Set<String> explicitlyIncludedNames = collected
				.stream()
				.filter(e -> e.getAnnotation(ToString.Include.class) != null)
				.map(this::toDisplayName)
				.collect(Collectors.toSet());
		return collected.stream()
				.filter(e -> !explicitlyIncludedNames.contains(toDisplayName(e)) || e.getAnnotation(ToString.Include.class) != null)
				.collect(Collectors.toList());
	}

	private boolean accept(Element element) {
		final ToString.Exclude excludeAnn = element.getAnnotation(ToString.Exclude.class);
		final ToString.Include includeAnn = element.getAnnotation(ToString.Include.class);
		return accept(element, excludeAnn, includeAnn);
	}

	private boolean accept(Element element, ToString.Exclude excludeAnn, ToString.Include includeAnn) {
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

		//TODO This should generate a warning, but only if the field isn't excluded later by an included equally named method.
		// if (includedWhenUnanotated && includeAnn.name().isEmpty()) raiseWarning(element, "Needless @ToString.Include");
		return true;
	}

	private String errorMessageIfAnnotated(Element element) {
		if (element.getModifiers().contains(Modifier.STATIC)) return "ToString doesn't work with a static element.";
		if (element instanceof VariableElement) return "";
		if (element instanceof ExecutableElement) {
			final ExecutableElement executableElement = (ExecutableElement) element;
			if (!executableElement.getParameters().isEmpty()) return "ToString doesn't work with a method with arguments.";
			return "";
		}
		return "Include and Exclude on this elements is forbidden.";
	}

	@Override protected String generatedClassName() {
		return packageName() + "._" + saneTypeName().replace(".", "_") + "_Demohelper";
	}

	@Override protected void intro(List<Element> elements) {
		append("public class ", generatedClassName().replaceAll(".*\\.", ""), " {");
		append("public static ", String.class, " toString(", typeElement.getQualifiedName(), " object) {");
		append(StringBuilder.class, " result = new StringBuilder();");
		append("result.append(\"", saneTypeName(), "\").append(\"(\");");
		if (toStringAnn.callSuper()) {
			raiseWarning(typeElement, "callSuper is not implemented as it needs a big hack.");
			//TODO Calling super needs a big hack: https://stackoverflow.com/a/25212108/581205
			// append("result" + (toStringAnn.callSuper() ? ".append(\"super=\")" : "") + ".append(super.toString());");
		}
		if (!toStringAnn.doNotUseGetters()) {
			raiseWarning(typeElement, "doNotUseGetters is not implemented as it's unclear how it should interact with using methods.");
		}
	}

	@Override protected void outtro(List<Element> elements) {
		append("result.append(\")\");");
		append("return result.toString();");
		append("}");

		if (elements.stream().anyMatch(VariableElement.class::isInstance)) {
			append();
			append("private static ", Object.class, " getFieldValue(", String.class, " name, ", Object.class, " object) {");
			append("try {");
			append(Field.class, " f = ", typeElement.getQualifiedName().toString(), ".class.getDeclaredField(name);");
			append("f.setAccessible(true);");
			append("return f.get(object);");
			append("} catch (", ReflectiveOperationException.class, " e) {");
			append("throw new ", RuntimeException.class, "(e);");
			append("}");
			append("}");
		}

		if (elements.stream().anyMatch(ExecutableElement.class::isInstance)) {
			append();
			append("private static ", Object.class, " getMethodValue(", String.class, " name, ", Object.class, " object) {");
			append("try {");
			append(Method.class, " m = ", typeElement.getQualifiedName().toString(), ".class.getDeclaredMethod(name);");
			append("m.setAccessible(true);");
			append("return m.invoke(object);");
			append("} catch (", ReflectiveOperationException.class, " e) {");
			append("throw new ", RuntimeException.class, "(e);");
			append("}");
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

		final String line = "result"
				+ (isFirst ? "" : ".append(\", \")")
				+ (toStringAnn.includeFieldNames() ? ".append(\"" + toDisplayName(element) + "=\")" : "")
				+ ".append(get" + s + "Value(\"" + element.getSimpleName().toString() + "\", object));";
		append(line);
		isFirst = false;
	}

	private String toDisplayName(Element element) {
		final ToString.Include includeAnn = element.getAnnotation(ToString.Include.class);
		return includeAnn!=null && !includeAnn.name().isEmpty() ? includeAnn.name() : element.getSimpleName().toString();
	}

	private final boolean excludeByDefault;
	private final ToString toStringAnn;
	private boolean isFirst = true;
}

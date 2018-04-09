package de.grajcar.aptlombokdemo.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public abstract class TypeProcessor {
	protected TypeProcessor(ProcessingEnvironment processingEnv, TypeElement typeElement) {
		this.processingEnv = processingEnv;
		this.typeElement = typeElement;
	}

	protected final void process() {
		final List<Element> elements = collectElements();
		intro(elements);
		elements.forEach(this::body);
		outtro(elements);
		try {

			final JavaFileObject file = processingEnv.getFiler().createSourceFile(generatedClassName());
			try (PrintWriter writer = new PrintWriter(file.openWriter())) {
				writer.println("package " + packageName() + ";");
				writer.println();
				imports.forEach(i -> writer.println("import " + i + ";"));
				writer.println();
				lines.forEach(writer::println);
				if (writer.checkError()) throw new IOException("Exception in PrintWriter.");
			}
		} catch (final IOException e) {
			raiseError(typeElement, e.getClass().getName() + ": " + e.getMessage());
		}
	}

	protected abstract String generatedClassName();

	protected abstract void intro(List<Element> elements);

	protected abstract void outtro(List<Element> elements);

	protected final void body(Element element) {
		if (element instanceof VariableElement) {
			body((VariableElement) element);
		} else if (element instanceof ExecutableElement) {
			body((ExecutableElement) element);
		}
	}

	protected abstract void body(VariableElement element);

	protected abstract void body(ExecutableElement element);

	protected abstract List<Element> collectElements();

	protected final String packageName() {
		return processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
	}

	protected final String saneTypeName() {
		final List<TypeElement> result = new ArrayList<>();
		for (Element e=typeElement; e instanceof TypeElement; e=e.getEnclosingElement()) result.add((TypeElement) e);
		Collections.reverse(result);
		return result.stream().map(TypeElement::getSimpleName).map(Object::toString).collect(Collectors.joining("."));
	}

	protected final void addImport(Class<?> clazz) {
		addImport(clazz.getName());
	}

	protected final void addImport(String name) {
		imports.add(name);
	}

	protected final void append(Object... parts) {
		final StringBuilder sb = new StringBuilder();
		for (final Object p : parts) {
			if (p instanceof Class) {
				sb.append(((Class<?>) p).getName());
			} else {
				sb.append(p);
			}
		}
		appendInternal(sb.toString());
	}

	private void appendInternal(String line) {
		if (line.startsWith("}")) --indent;
		lines.add((line.isEmpty() ? "" : indentation()) + line);
		if (line.endsWith("{")) ++indent;
	}

	private String indentation() {
		final char[] result = new char[indent];
		Arrays.fill(result, '\t');
		return new String(result);
	}

	protected final void raiseWarning(Element element, String message) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message, element);
	}

	protected final void raiseError(Element element, String message) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
	}

	protected final ProcessingEnvironment processingEnv;
	protected final TypeElement typeElement;

	private final List<String> lines = new ArrayList<>();
	private final Set<String> imports = new HashSet<>();
	private int indent;
}

package de.grajcar.aptlombokdemo.impl;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import de.grajcar.aptlombokdemo.ToString;

@SupportedAnnotationTypes("de.grajcar.aptlombokdemo.ToString")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class DemoProcessor extends AbstractProcessor {
	@Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (final Element e : roundEnv.getElementsAnnotatedWith(ToString.class)) new ToStringProcessor(processingEnv, (TypeElement) e).process();
		return true;
	}
}

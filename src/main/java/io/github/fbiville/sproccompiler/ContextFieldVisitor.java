package io.github.fbiville.sproccompiler;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.SimpleElementVisitor8;
import java.util.Set;
import java.util.stream.Stream;

class ContextFieldVisitor extends SimpleElementVisitor8<Stream<CompilationError>, Void> {

    @Override
    public Stream<CompilationError> visitVariable(VariableElement field, Void ignored) {
        Set<Modifier> modifiers = field.getModifiers();
        if (!modifiers.contains(Modifier.PUBLIC)
                || modifiers.contains(Modifier.STATIC)
                || modifiers.contains(Modifier.FINAL)) {

            return Stream.of(new ContextFieldError(
                    field,
                    "Field %s#%s should be public, non-static and non-final",
                    field.getEnclosingElement().getSimpleName(),
                    field.getSimpleName()
            ));
        }
        return Stream.empty();
    }
}

package io.github.fbiville.sproccompiler;

import com.google.auto.service.AutoService;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Procedure;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
public class StoredProcedureProcessor extends AbstractProcessor {

    private static final Class<? extends Annotation> sprocType = Procedure.class;
    private static final Class<? extends Annotation> contextType = Context.class;
    private ElementVisitor<Stream<CompilationError>, Void> parameterVisitor;
    private ElementVisitor<Stream<CompilationError>, Void> contextFieldVisitor;
    private Messager messager;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(sprocType.getName());
        types.add(contextType.getName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();

        parameterVisitor = new StoredProcedureVisitor(
            processingEnv.getTypeUtils(),
            processingEnv.getElementUtils()
        );
        contextFieldVisitor = new ContextFieldVisitor();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {

        processStoredProcedures(roundEnv);
        processStoredProcedureContextFields(roundEnv);
        return false;
    }

    private void processStoredProcedures(RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(sprocType)
                .stream()
                .flatMap(this::validateStoredProcedure)
                .forEachOrdered(this::printError);
    }

    private void processStoredProcedureContextFields(RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(contextType)
                .stream()
                .flatMap(this::validateContextField)
                .forEachOrdered(this::printError);

    }

    private Stream<CompilationError> validateStoredProcedure(Element element) {
        return parameterVisitor.visit(element);
    }

    private Stream<CompilationError> validateContextField(Element element) {
        return contextFieldVisitor.visit(element);
    }

    private void printError(CompilationError error) {
        messager.printMessage(
                ERROR,
                error.getErrorMessage(),
                error.getElement(),
                error.getMirror()
        );
    }
}

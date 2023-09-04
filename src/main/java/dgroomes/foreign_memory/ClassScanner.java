package dgroomes.foreign_memory;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ClassMemberInfo;

import java.util.List;

/**
 * Find class metadata by scanning classes on the classpath.
 * <p>
 * This class uses the ClassGraph library to do the heavy lifting.
 */
public class ClassScanner {

    /**
     * A class and the names of its fields and methods.
     */
    public record ClassInfo(String className, List<String> fieldNames, List<String> methodNames) {
    }

    /**
     * Build a dictionary of class names and their field names and method names.
     */
    public static List<ClassInfo> scanClasses() {
        ClassGraph classGraph = new ClassGraph()
                .enableSystemJarsAndModules()
                .enableFieldInfo()
                .enableMethodInfo();

        ClassInfoList classInfos;
        try (var scanResult = classGraph.scan()) {
            classInfos = scanResult.getAllClasses();
        }

        return classInfos.stream().map(classInfo -> {
            var fieldNames = classInfo.getFieldInfo().stream().map(ClassMemberInfo::getName).toList();
            var methodNames = classInfo.getMethodInfo().stream().map(ClassMemberInfo::getName).toList();
            return new ClassInfo(classInfo.getName(), fieldNames, methodNames);
        }).toList();
    }
}

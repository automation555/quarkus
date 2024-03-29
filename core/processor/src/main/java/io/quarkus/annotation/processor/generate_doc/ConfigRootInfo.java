package io.quarkus.annotation.processor.generate_doc;

import java.util.Objects;

import javax.lang.model.element.TypeElement;

final public class ConfigRootInfo {
    private final String name;
    private final TypeElement clazz;
    private final ConfigPhase configPhase;
    private final boolean mapping;
    private final String fileName;

    public ConfigRootInfo(
            final String name,
            final TypeElement clazz,
            final ConfigPhase configPhase, final boolean mapping, final String fileName) {
        this.name = name;
        this.clazz = clazz;
        this.configPhase = configPhase;
        this.mapping = mapping;
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ConfigRootInfo that = (ConfigRootInfo) o;
        return mapping == that.mapping &&
                name.equals(that.name) &&
                clazz.equals(that.clazz) &&
                configPhase == that.configPhase &&
                fileName.equals(that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, clazz, configPhase, mapping, fileName);
    }

    @Override
    public String toString() {
        return "ConfigRootInfo{" +
                "name='" + name + '\'' +
                ", clazz=" + clazz +
                ", configPhase=" + configPhase +
                ", mapping=" + mapping +
                ", fileName='" + fileName + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public TypeElement getClazz() {
        return clazz;
    }

    public ConfigPhase getConfigPhase() {
        return configPhase;
    }

    public boolean isMapping() {
        return mapping;
    }
}

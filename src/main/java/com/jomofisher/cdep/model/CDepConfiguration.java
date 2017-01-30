package com.jomofisher.cdep.model;

public class CDepConfiguration {
    final public Dependency dependencies[];

    public CDepConfiguration() {
        dependencies  = new Dependency[] {};
    }

    public String toYaml(int indent) {
        String prefix = new String(new char[indent * 2]).replace('\0', ' ');
        StringBuilder sb = new StringBuilder();
        if (dependencies != null && dependencies.length > 0) {
            sb.append(String.format("%sdependencies:\n", prefix));
            for (Dependency dependency : dependencies) {
                sb.append("- ");
                sb.append(dependency.toYaml(indent + 1));
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toYaml(0);
    }
}

package io.cdep.model;

public class CDepYml {

    final public BuildSystem builders[] = new BuildSystem[0];
    final public Dependency dependencies[] = new Dependency[0];

    public CDepYml() {
    }

    private String toYaml(@SuppressWarnings("SameParameterValue") int indent) {
        String prefix = new String(new char[indent * 2]).replace('\0', ' ');
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%sbuilders: [", prefix));
        for (int j = 0; j < builders.length; ++j) {
            if (j != 0) {
                sb.append(", ");
            }
            sb.append(builders[j]);
        }
        sb.append("]\n");

        if (dependencies.length > 0) {
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

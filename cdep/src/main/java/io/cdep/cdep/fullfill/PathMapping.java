package io.cdep.cdep.fullfill;

import io.cdep.cdep.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PathMapping {
  final public File from;
  final public File to;

  PathMapping(File from, File to) {
    this.from = from;
    this.to = to;
  }

  public static PathMapping[] parse(String text) {
    List<PathMapping> result = new ArrayList<>();
    String[] mappings = text.split("\\|");
    for (String mapping : mappings) {
      String[] fromTo = mapping.split("->");
      if (fromTo.length == 1) {
        if (fromTo[0].endsWith("/...")) {
          // Have some like path/...
          File baseFolder = new File(fromTo[0].substring(0, fromTo[0].length() - 4));
          for (File from : FileUtils.listFileTree(baseFolder)) {
            File to = new File(from.getPath().substring(baseFolder.getPath().length() + 1));
            result.add(new PathMapping(from, to));
          }
        } else {
          result.add(new PathMapping(
              new File(fromTo[0].trim()),
              new File(new File(fromTo[0].trim()).getName())));
        }
      } else if (fromTo.length == 2) {
        result.add(new PathMapping(
            new File(fromTo[0].trim()), new File(
            fromTo[1].trim())));
      }
    }
    return result.toArray(new PathMapping[result.size()]);
  }

}

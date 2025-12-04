package util;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ContainerBuilder {

    public static File buildContainer(Path baseDir, File containerFile) throws IOException {

        if (!Files.exists(baseDir) || !Files.isDirectory(baseDir)) {
            throw new IllegalArgumentException("Diretório base inválido: " + baseDir);
        }

        List<Path> arquivos = new ArrayList<>();

        Files.walk(baseDir)
                .filter(Files::isRegularFile)
                .filter(p -> !p.toString().contains("keys" + File.separator)) // ← IGNORA pasta keys
                .forEach(arquivos::add);

        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(containerFile)))) {

            dos.writeInt(arquivos.size()); // quantidade de arquivos

            for (Path p : arquivos) {

                Path rel = baseDir.relativize(p);
                String caminho = rel.toString().replace(File.separatorChar, '/');

                byte[] caminhoBytes = caminho.getBytes("UTF-8");
                long fileSize = Files.size(p);

                dos.writeInt(caminhoBytes.length);
                dos.write(caminhoBytes);
                dos.writeLong(fileSize);

                try (InputStream in = Files.newInputStream(p)) {
                    byte[] buffer = new byte[8192];
                    long remaining = fileSize;
                    int read;
                    while (remaining > 0 && (read = in.read(buffer, 0, (int)Math.min(buffer.length, remaining))) != -1) {
                        dos.write(buffer, 0, read);
                        remaining -= read;
                    }
                }
            }
        }

        return containerFile;
    }
}

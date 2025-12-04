package util;

import java.io.*;
import java.nio.file.*;

public class ContainerExtractor {
    private static void skipFully(DataInputStream dis, long n) throws IOException {
        long remaining = n;
        while (remaining > 0) {
            long skipped = dis.skip(remaining);
            if (skipped <= 0) {
                // se não conseguiu pular, tenta ler e descartar 1 byte
                if (dis.read() == -1) {
                    throw new EOFException("Fim inesperado ao tentar pular bytes");
                }
                skipped = 1;
            }
            remaining -= skipped;
        }
    }

    public static void extractContainer(File containerFile, Path targetDir) throws IOException {

        if (!containerFile.exists()) {
            throw new FileNotFoundException("Container não encontrado: " + containerFile.getPath());
        }

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(containerFile)))) {

            int totalArquivos = dis.readInt();

            for (int i = 0; i < totalArquivos; i++) {

                int pathLen = dis.readInt();
                byte[] pathBytes = new byte[pathLen];
                dis.readFully(pathBytes);
                String relPath = new String(pathBytes, "UTF-8");

                if (relPath.startsWith("keys/")) {
                    // avança conteúdo sem salvar
                    long skip = dis.readLong();
                    System.out.println("Ignorando arquivo da pasta keys: " + relPath);
                    skipFully(dis, skip);
                    continue;
                }

                long fileSize = dis.readLong();
                Path outPath = targetDir.resolve(relPath.replace('/', File.separatorChar));

                Files.createDirectories(outPath.getParent());

                try (OutputStream out = Files.newOutputStream(outPath)) {
                    byte[] buffer = new byte[8192];
                    long remaining = fileSize;
                    while (remaining > 0) {
                        int read = dis.read(buffer, 0, (int)Math.min(buffer.length, remaining));
                        if (read == -1) throw new EOFException("Erro no container.");
                        out.write(buffer, 0, read);
                        remaining -= read;
                    }
                }
            }
        }
    }
}

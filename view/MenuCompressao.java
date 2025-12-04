package view;

import util.ContainerBuilder;
import util.ContainerExtractor;
import dao.LZWCompressor;
import dao.LZWDecompressor; 
import java.io.File;
import java.nio.file.*;
import java.util.Scanner;

public class MenuCompressao {

    private Scanner console;

    public MenuCompressao(Scanner console) {
        this.console = console;
    }

    public void menu() {
        int opcao;

        do {
            System.out.println("\n=== MENU DE COMPRESSÃO ===");
            System.out.println("1 - Compactar em arquivo .lzw");
            System.out.println("2 - Descompactar backup.lzw");
            System.out.println("0 - Voltar");

            System.out.print("Opção: ");
            try {
                opcao = Integer.parseInt(console.nextLine());
            } catch (Exception e) {
                opcao = -1;
            }

            switch (opcao) {
                case 1: 
                    compactarTudo();
                    break;
                case 2: 
                    descompactarTudo();
                    break;
                case 0: 
                    break;
                default: 
                    System.out.println("Opção inválida!");
            }

        } while (opcao != 0);
    }

    private void compactarTudo() {
        try {
            Path base = Paths.get("dados");
            File containerBin = new File("dados/container.bin");
            File containerLzw = new File("dados/backup.lzw");
            ContainerBuilder.buildContainer(base, containerBin);

            System.out.println("Compactando com LZW...");
            LZWCompressor.main(new String[]{ containerBin.getPath(), containerLzw.getPath() });

            containerBin.delete(); // remove arquivo temporário

        } catch (Exception e) {
            System.err.println("Erro na compactação:");
            e.printStackTrace();
        }
    }

    private void descompactarTudo() {
        try {
            File containerLzw = new File("dados/backup.lzw");
            if (!containerLzw.exists()) {
                System.out.println("ERRO: dados/backup.lzw não existe.");
                return;
            }

            File tempBin = new File("dados/container_temp.bin");

            System.out.println("Descompactando LZW...");
            LZWDecompressor.main(new String[]{ containerLzw.getPath(), tempBin.getPath() });
            ContainerExtractor.extractContainer(tempBin, Paths.get("dados"));

            tempBin.delete(); // remove temporário

            System.out.println("Arquivos restaurados em dados/");
        } catch (Exception e) {
            System.err.println("Erro na descompactação:");
            e.printStackTrace();
        }
    }
}

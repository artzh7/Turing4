import org.kohsuke.args4j.*;

import java.io.*;
import java.util.*;

public class Main {

    @Option(name = "-tape", usage = "input tape file path")
    private String tapeFilePath = "";

    @Option(name = "-start", usage = "index of tape to start")
    private int index = 0;

    @Option(name = "-table", usage = "input table of statements file path")
    private String tableFilePath = "";

    @Option(name = "-output", usage = "output tape file path")
    private String outputFilePath = "";

    public static void main(String[] args) throws FileNotFoundException {

        new Main().launch(args);

        // java -jar Turing4.jar -tape "files/tape.txt" -start 3 -table "files/table.txt" -output "files/output.txt"
        // или в указать параметры в конфиге при запуске в среде
    }

    private void launch(String[] args) throws FileNotFoundException {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            e.printStackTrace();
            return;
        }

        if (tapeFilePath.isEmpty() || tableFilePath.isEmpty())
            throw new IllegalArgumentException("Нет пути до файла входных данных");
        if (outputFilePath.isEmpty())
            throw new IllegalArgumentException("Нет пути до файла выходных данных");

        File tapeFile = new File(tapeFilePath);
        int start = index;
        File tableFile = new File(tableFilePath);
        File output = new File(outputFilePath);

        // считываем ленту
        Scanner scanner = new Scanner(tapeFile);
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNextLine()){
            sb.append(scanner.nextLine());
        }
        String tape = sb.toString();

        // считываем таблицу
        scanner = new Scanner(tableFile);
        sb = new StringBuilder();
        List<String> table = new ArrayList<>();
        while (scanner.hasNextLine()){
            sb.append(scanner.nextLine());
            table.add(sb.toString());
            sb = new StringBuilder();
        }

        Machine machine = new Machine(tape, start, table);
        int ret = machine.start();
        if (ret != -1){
            machine.print(outputFilePath);
        }
    }
}

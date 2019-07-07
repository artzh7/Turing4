import org.kohsuke.args4j.*;

import java.io.*;
import java.util.*;

public class Main {

    @Option(name = "-tape", usage = "input tape file path", required = true)
    private String tapeFilePath = "";

    @Option(name = "-start", usage = "index of tape to start")
    private int index = 0;

    @Option(name = "-steps", usage = "how many steps need to perform")
    private int steps = -1;

    @Option(name = "-ms", usage = "idle between steps in ms")
    private int ms = -1;

    @Option(name = "-table", usage = "input table of statements file path", required = true)
    private String tableFilePath = "";

    @Option(name = "-output", usage = "output tape file path", required = true)
    private String outputFilePath = "";

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        new Main().launch(args);
    }

    private void launch(String[] args) throws FileNotFoundException, InterruptedException {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            e.printStackTrace();
            return;
        }

        try {
            if (tapeFilePath.isEmpty() || tableFilePath.isEmpty())
                throw new IllegalArgumentException("no input file path");
            if (outputFilePath.isEmpty())
                throw new IllegalArgumentException("no output file path");
            if (index < 0)
                throw new IllegalArgumentException("the start index must be non-negative");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }

        File tapeFile = new File(tapeFilePath);
        int start = index;
        File tableFile = new File(tableFilePath);

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
        scanner.close();

        Machine machine;
        Process ret;
        try {
            machine = new Machine(tape, start, steps, ms, table);
            ret = machine.start();
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
            return;
        }
        if (ret == Process.IDLE){
            machine.print(outputFilePath);
            System.out.println("the result is in output file");
        }
    }
}

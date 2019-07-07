import java.io.*;
import java.util.*;
import java.util.regex.*;

// - переход в новое состояние
// - запись на ленту ИЛИ сдвиг

class Machine {
    private Tape tape;
    private Table table;
    private int
            ms, steps,
            currentStep = 1,
            currentState = 1;
    private volatile Process process = Process.IDLE;

    static String
            leftException = "can't move the head left",
            illegalCharOnTape = "an illegal symbol on the tape was found",
            incorrectTable = "the table is incorrect";

    Machine (String tape, int start, int steps, int ms, List<String> table){
        this.tape = new Tape(tape, start);
        this.table = new Table(table);
        this.ms = ms;
        this.steps = steps;
    }

    // работа машины на основе таблицы состояний
    Process start() throws InterruptedException {
        process = Process.RUNNING;
        System.out.println("running");
        if (ms > 0)
            System.out.println("\n0  |  " + tape);
        performing.start();
        if (ms > 0) listening.start();
        performing.join();
        listening.interrupt();
        scanner = null;
        return process;
    }

    private Process perform(){
        // смотрим на символ под кареткой
        char curr = tape.getSymbols().get(tape.getCurrent());
        // ищем строку с инструкциями под этот символ
        int row = table.getSymbols().indexOf(curr);
        if (row == -1)
            throw new IllegalArgumentException("no match between symbol on tape and table");
        // получаем действие под символ+состояние
        Table.Action action = table.getActions().get(row).get(currentState-1);


        switch (action.getSymbol()){
            case '<':
                try {
                    tape.left();
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println(e.getMessage());
                    return Process.FAILURE;
                }
                break;
            case '>':
                tape.right();
                break;
            case '=':
                break;
            default:
                tape.getSymbols().set(tape.getCurrent(), action.getSymbol());
                break;
        }

        if (action.getState() == 0) {
            return Process.IDLE;
        } else {
            currentState = action.getState();
        }
        return Process.RUNNING;
    }

    private void pause(){
        synchronized (this) {
            if (process == Process.RUNNING) {
                process = Process.PAUSED;
                System.out.println("paused");
            } else if (process == Process.PAUSED) {
                process = Process.RUNNING;
                System.out.println("running");
            }
        }
    }

    private void shutdown(){
        synchronized (this) {
            if (process == Process.RUNNING || process == Process.PAUSED)
                process = Process.IDLE;
        }
    }

    private volatile Scanner scanner = new Scanner(System.in);
    private Thread listening = new Thread(() -> {
        while (process == Process.RUNNING || process == Process.PAUSED){
            if (scanner.nextLine().equals("s")) shutdown();
            else pause();
        }
        scanner = null;
    });

    private Thread performing = new Thread(() -> {
        while (process == Process.RUNNING || process == Process.PAUSED){
            if (ms > 0) {
                try {
                    Thread.sleep(ms);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (process == Process.RUNNING) {
                try {
                    process = perform();
                } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
                    System.out.println(e.getMessage());
                    process = Process.FAILURE;
                    break;
                }
                if (ms > 0)
                    System.out.println(currentStep + "  |  " + tape);
                currentStep++;
                if (steps != -1 && currentStep > steps) {
                    process = Process.IDLE;
                }
            }
        }
        if (ms > 0) System.out.print("\nfinished ");
        else System.out.print("finished ");
        if (process == Process.IDLE) System.out.println("successfully");
        else if (process == Process.FAILURE) System.out.println("with an error");
        listening.interrupt();
        scanner = null;
    });

    void print(String outputFilePath) throws FileNotFoundException {
        File output = new File(outputFilePath);
        PrintWriter printWriter = new PrintWriter(output);
        for (char ch : tape.getSymbols()){
            printWriter.print(ch);
        }
        printWriter.close();
    }
}

enum Process {
    IDLE, RUNNING, PAUSED, FAILURE
}

class Tape{
    private LinkedList<Character> symbols = new LinkedList<>();
    private int current;

    Tape(String tape, int start){
        for (int i = 0; i < tape.length(); i++)
            if (tape.charAt(i) == '<' || tape.charAt(i) == '>'){
                throw new IllegalArgumentException(Machine.illegalCharOnTape);
            }
        for (int i = 0; i < tape.length(); i++){
            symbols.add(tape.charAt(i));
        }
        this.current = start;
    }

    void left(){
        if (current == 0) {
            throw new ArrayIndexOutOfBoundsException(
                    Machine.leftException
            );
        }
        current--;
    }

    void right(){
        current++;
    }

    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        for (char symbol : symbols){
            stringBuilder.append(symbol);
        }
        return stringBuilder.toString();
    }

    LinkedList<Character> getSymbols() {
        return symbols;
    }

    int getCurrent() {
        return current;
    }
}

class Table{
    private List<Character> symbols = new ArrayList<>();
    private List<List<Action>> actions = new ArrayList<>();

    Table(List<String> table){
        for (String row : table) {
            char symbol;
            int state;
            Pattern pattern2 = Pattern.compile("^.?,(\\d+)?$");
            Matcher matcher;

            String[] temp = row.split("\t");
            if (temp[0].length() != 1)
                throw new IllegalArgumentException(Machine.incorrectTable);
            for (int i = 1; i < temp.length; i++){
                matcher = pattern2.matcher(temp[i]);
                if (!matcher.matches())
                    throw new IllegalArgumentException(Machine.incorrectTable);
            }
            symbols.add(temp[0].charAt(0));
            List<Action> lineOfActions = new ArrayList<>();

            for (int j = 1; j < temp.length; j++) {
                String strAction = temp[j];
                if (strAction.equals(",")) {
                    symbol = '=';
                    state = j;
                } else {
                    symbol = strAction.charAt(0);
                    String changeState = strAction.substring(2);
                    state = Integer.parseInt(changeState);
                }
                Action action = new Action(symbol, state);
                lineOfActions.add(action);
            }
            actions.add(lineOfActions);
        }
    }

    List<Character> getSymbols() {
        return symbols;
    }

    List<List<Action>> getActions() {
        return actions;
    }

    class Action{
        private int state;
        private char symbol;

        Action(char symbol, int state){
            this.state = state;
            this.symbol = symbol;
        }

        int getState() {
            return state;
        }

        char getSymbol() {
            return symbol;
        }
    }
}
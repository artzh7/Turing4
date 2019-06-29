import java.io.*;
import java.util.*;

// - переход в новое состояние
// - запись на ленту ИЛИ сдвиг

public class Machine {
    private Tape tape;
    private Table table;
    private int currentState = 1;

    static String leftException = "Can't move the head left";
    static String illegalChar = "Illegal symbol was found";

    Machine (String tape, int start, List<String> table){
        this.tape = new Tape(tape, start);
        this.table = new Table(table);
    }

    public int start(){
        // проверка корректности ленты
        for (char ch : tape.symbols)
            if (ch == '<' || ch == '>'){
                System.out.println(Machine.illegalChar);
                return -1;
            }
        // проверка корректности таблицы инструкций
        // .......

        // работа машины на основе таблицы состояний
        for (;;) {
            char curr = tape.symbols.get(tape.current);     // смотрим на символ под указателем
            int row = table.symbols.indexOf(curr);          // ищем строку с инструкциями под этот символ
            Table.Action action = table.actions.get(row).get(currentState-1); // получаем действие под символ+состояние
            switch (action.symbol){
                case '<':
                    try {
                        tape.left();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println(e.getMessage());
                        return -1;
                    }
                    break;
                case '>':
                    tape.right();
                    break;
                case '=':
                    break;
                default:
                    tape.symbols.set(tape.current, action.symbol);
                    break;
            }
            if (action.state == 0) {
                break;
            } else {
                currentState = action.state;
            }
        }
        return 0;
    }

    public void print(String outputFilePath) throws FileNotFoundException {
        File output = new File(outputFilePath);
        PrintWriter printWriter = new PrintWriter(output);
        for (char ch : tape.symbols){
            printWriter.print(ch);
        }
        printWriter.close();
    }
}

class Tape{
    LinkedList<Character> symbols = new LinkedList<>();
    int start;
    int current;

    Tape(String tape, int start){
        for (int i = 0; i < tape.length(); i++){
            symbols.add(tape.charAt(i));
        }
        this.start = start;
        this.current = start;
    }

    public void left(){
        if (current == 0) {
            throw new ArrayIndexOutOfBoundsException(
                    Machine.leftException
            );
        }
        current--;
    }

    public void right(){
        current++;
    }
}

class Table{
    List<Character> symbols = new ArrayList<>();
    List<List<Action>> actions = new ArrayList<>();

    Table(List<String> table){
        for (String row : table) {
            char symbol;
            int state;

            String[] temp = row.split("\t");
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

    class Action{
        int state;
        char symbol;

        Action(char symbol, int state){
            this.state = state;
            this.symbol = symbol;
        }
    }
}
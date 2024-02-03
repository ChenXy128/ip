package duke.main;

import duke.storage.Storage;
import duke.parser.Parser;
import duke.ui.Ui;
import duke.tasklist.TaskList;
import duke.task.Task;
import duke.exception.DukeException;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.IOException;
import java.io.File;

/**
 * Represents the main class of the Duke application, a personal assistant chatbot
 * that helps users to keep track of various tasks. The application is capable of
 * understanding commands to add, delete, mark, and list tasks among other functionalities.
 *
 * Duke uses a combination of components to manage tasks:
 * - {@link duke.storage.Storage} for loading from and saving tasks to a file.
 * - {@link duke.parser.Parser} for interpreting user input.
 * - {@link duke.ui.Ui} for handling interactions with the user.
 * - {@link duke.tasklist.TaskList} for storing and managing a list of tasks.
 *
 * The application supports tasks of types: Todo, Deadline, and Event, which can be
 * added, marked as done, deleted, and listed. It operates in a loop, processing user
 * commands until a termination command is received.
 */
public class Duke {
    
    private static final String FILE_PATH = System.getProperty("user.home") + File.separator
            + "Downloads" + File.separator + "duke.txt";
    private Storage storage;
    private Ui ui;
    private Parser parser;
    private TaskList list;
    
    /**
     * Constructs a new Duke object with a specified file path for data storage.
     * Initializes the UI, Parser, TaskList, and Storage components of Duke.
     *
     * @param filePath The path of the file where task data is stored.
     */
    public Duke(String filePath) {
        this.parser = new Parser();
        this.ui = new Ui();
        this.list = new TaskList(new ArrayList<>());
        this.storage = new Storage(filePath, parser);
        
    }
    
    /**
     * Enum representing possible instructions that Duke can process.
     */
    public enum Instruction {
        LIST, MARK, UNMARK, TODO, DEADLINE, EVENT, DELETE, BYE, FIND, ANYTHING_ELSE
    }
    
    /**
     * Determines the type of instruction given by the user input.
     *
     * @param input The raw input string from the user.
     * @return The Instruction that corresponds to the user's command.
     */
    public Instruction getInstr(String input) {
        if (input.toLowerCase().startsWith("list")) {
            return Instruction.LIST;
        } else if (input.toLowerCase().startsWith("mark")) {
            return Instruction.MARK;
        } else if (input.toLowerCase().startsWith("unmark")) {
            return Instruction.UNMARK;
        } else if (input.toLowerCase().startsWith("todo")) {
            return Instruction.TODO;
        } else if (input.toLowerCase().startsWith("deadline")) {
            return Instruction.DEADLINE;
        } else if (input.toLowerCase().startsWith("event")) {
            return Instruction.EVENT;
        } else if (input.toLowerCase().startsWith("delete")) {
            return Instruction.DELETE;
        } else if (input.toLowerCase().startsWith("find")) {
            return Instruction.FIND;
        } else if (input.toLowerCase().startsWith("bye")){
            return Instruction.BYE;
        } else {
            return Instruction.ANYTHING_ELSE;
        }
    }
    
    /**
     * Starts the Duke application. This method sets up necessary components,
     * loads existing tasks from storage, and processes user input until the "bye"
     * command is received.
     *
     * @throws DukeException If the application encounters an error it cannot recover from.
     */
    public void run() throws DukeException {
        Scanner sc = new Scanner(System.in);
        Duke duke = new Duke(FILE_PATH);
        boolean isRunning = true;
        try {
            duke.storage.loadFile(duke.list);
        } catch (IOException e) {
            duke.ui.loadingError();
        }
    
        duke.ui.greeting();
    
        while(isRunning) {
            String inputFromUser = sc.nextLine();
            Instruction instruction = duke.getInstr(inputFromUser);
        
            switch (instruction) {
            case LIST:
                duke.ui.listing(duke.list);
                break;
            case MARK:
                duke.ui.handleMarkError(inputFromUser, duke.list);
                Task taskToBeMarked = duke.parser.getTaskTobeMarked(inputFromUser, duke.list);
                taskToBeMarked.markDone();
                duke.ui.marking(taskToBeMarked);
                try {
                    duke.storage.changeFileContent(duke.list);
                } catch (IOException e) {
                    duke.ui.changingFileError();
                }
                break;
            case UNMARK:
                duke.ui.handleUnmarkError(inputFromUser, duke.list);
                Task taskToBeUnmarked = duke.parser.getTaskToBeUnmarked(inputFromUser, duke.list);
                taskToBeUnmarked.markUndone();
                duke.ui.unmarking(taskToBeUnmarked);
                try {
                    duke.storage.changeFileContent(duke.list);
                } catch (IOException e) {
                    duke.ui.changingFileError();
                }
                break;
            case TODO:
                duke.ui.handleTodoError(inputFromUser);
                Task todoTask = duke.parser.createToDo(inputFromUser);
                duke.ui.echo(todoTask, duke.list);
                try {
                    duke.storage.addTaskToFile((todoTask));
                } catch (IOException e) {
                    duke.ui.changingFileError();
                }
                break;
            case DEADLINE:
                duke.ui.handleDeadlineError(inputFromUser);
                Task deadlineTask = duke.parser.createDeadline(inputFromUser, duke.ui);
                duke.ui.echo(deadlineTask, duke.list);
                try {
                    duke.storage.addTaskToFile((deadlineTask));
                } catch (IOException e) {
                    duke.ui.changingFileError();
                }
                break;
            case EVENT:
                duke.ui.handleEventError(inputFromUser);
                Task eventTask = duke.parser.createEvent(inputFromUser, duke.ui);
                duke.ui.echo(eventTask, duke.list);
                try {
                    duke.storage.addTaskToFile((eventTask));
                } catch (IOException e) {
                    duke.ui.changingFileError();
                }
                break;
            case DELETE:
                int indexOfTaskToDelete = Integer.parseInt(inputFromUser.substring(7));
                duke.ui.handleDeleteError(duke.list,indexOfTaskToDelete);
                Task taskToDelete = duke.parser.getTaskToDelete(inputFromUser, duke.list);
                duke.ui.deleting(taskToDelete, duke.list);
                duke.list.delete(taskToDelete);
                try {
                    duke.storage.changeFileContent(duke.list);
                } catch (IOException e) {
                    duke.ui.changingFileError();
                }
                break;
            case FIND:
                String keyword = duke.parser.getKeywordForFind(inputFromUser);
                duke.ui.finding(duke.list, keyword);
                break;
            case BYE:
                duke.ui.bye();
                isRunning = false;
                break;
            default:
                throw new DukeException("Can't understand your instruction");
            }
        }
        sc.close();
    }
    
    /**
     * The main entry point of the Duke application.
     *
     * @param args Command line arguments (not used).
     * @throws DukeException If the application encounters an unrecoverable error during startup.
     */
    public static void main(String[] args) throws DukeException {
        new Duke(FILE_PATH).run();
    }
}
import javafx.scene.layout.*;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;

import java.io.*;
import java.util.Scanner;

public class EditorPane extends BorderPane {
    private TextArea editorTextArea;
    private File currentFile;
    private int caretPosition;

    public EditorPane() {
        addTextArea();
        currentFile = null;
    }

    private void addTextArea() {
        editorTextArea = new TextArea();
        editorTextArea.setFont(Font.font("Menlo, Monaco, 'Courier New', monospace", 14));
        editorTextArea.setStyle("-fx-focus-color: -fx-control-inner-background ; -fx-faint-focus-color: -fx-control-inner-background ;");
        this.setCenter(editorTextArea);
    }

    public void addLine(String line) {
        caretPosition = editorTextArea.getCaretPosition();
        this.editorTextArea.insertText(caretPosition, line);
    }

    public File getCurrentFile() {
        return this.currentFile;
    }

    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
    }

    public void saveCurrentFile() throws FileNotFoundException, IOException {
        PrintWriter output;

        output = null;

        output = new PrintWriter(new BufferedWriter(new FileWriter(this.currentFile)));
        output.write(this.editorTextArea.getText());

        if (output != null) {
            output.close();
        }
    }//end of saveCurrentFile() method

    public void openFile(File file) throws FileNotFoundException, IOException {
        setCurrentFile(file);
        openCurrentFile();
    }//end of openFile(File) method

    public void openCurrentFile() throws FileNotFoundException, IOException {
        Scanner currentFileScanner;

        currentFileScanner = null;

        if (currentFile.exists()) {
            currentFileScanner = new Scanner(this.currentFile);

            while (currentFileScanner.hasNextLine()) {
                this.editorTextArea.appendText(currentFileScanner.nextLine() + "\n");
            }
        }

        if (currentFileScanner != null) {
            currentFileScanner.close();
        }
    }//end of openCurrentFile() method

}//end of class
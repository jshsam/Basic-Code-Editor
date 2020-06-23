import java.io.*;
import java.util.Scanner;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.TextAlignment;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class EditorGUI extends Application {
    public final int CLASS_IMPORT = 0;
    public final int PACKAGE_IMPORT = 1;

    private Scene scene;
    private BorderPane mainPane;
    private SplitPane splitPane;
    private EditorPane editorPane = new EditorPane();
    private EditorPane currentEditorPane;
    private EditorPane newEditorPane;
    private TabPane editorTabPane;
    private VBox buttonsToolBox;
    private Tab newTab;
    private MenuBar menuBar;
    private Menu fileMenu;
    private Menu classMenu;
    private Menu packageMenu;
    private Menu toolsMenu;
    private Menu helpMenu;
    private MenuItem newFileMenuItem;
    private MenuItem openFileMenuItem;
    private MenuItem saveAsFileMenuItem;
    private MenuItem saveFileMenuItem;
    private MenuItem exitFileMenuItem;
    private MenuItem newClassMenuItem;
    private MenuItem importClassMenuItem;
    private MenuItem importPackageMenuItem;
    private MenuItem addStatementPackageMenuItem;
    private MenuItem commandLineToolsMenuItem;
    private MenuItem aboutHelpMenuItem;
    private Label toolBoxLabel;
    private FileChooser fileChooser;
    private MenuHandler menuHandler;
    private ButtonHandler buttonHandler;
    private int currentImportType;
    private Button okButton = new Button("Ok");
    private TextField inputTextField;
    private RadioButton addMainRadioButton;
    private boolean isMacOS = false;
    private Button[] toolBoxButtons;

    public void start(Stage primaryStage) {
        createButtons();
        checkSystem();
        createMenus();
        createButtonsToolBox();
        editorTabPane = new TabPane();
        addNewTab(editorPane, "New");
        createSplitPane();
        createMainPane();
        createAndRegisterHandler(primaryStage);
        openTabs();

        scene = new Scene(mainPane, 1000, 820);
        primaryStage.setTitle("Java Code Editor");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(ev -> {
            saveTabs(primaryStage);
            System.exit(0);
        });
        splitPane.lookupAll(".split-pane-divider").stream()
        .forEach(div ->  div.setMouseTransparent(true) );
    }//end of start(Stage) method

    private class MenuHandler implements EventHandler<ActionEvent> {
        private Stage primaryStage;
        private EditorPane editorPane;

        public MenuHandler(Stage primaryStage) {
            this.primaryStage = primaryStage;
        }

        public void handle(ActionEvent ae) {
            okButton.setDefaultButton(true);
            if (ae.getSource() == newFileMenuItem) {
                editorPane = new EditorPane();
                addNewTab(editorPane, "New");
            }
            if (ae.getSource() == openFileMenuItem) {
                open(primaryStage);
            }
            else if (ae.getSource() == saveAsFileMenuItem) {
                save(primaryStage);
            }
            else if (ae.getSource() == saveFileMenuItem) {
                saveToExistingFile(primaryStage);
            }
            else if (ae.getSource() == exitFileMenuItem) {
                saveTabs(primaryStage);
                System.exit(0);
            }
            else if (ae.getSource() == newClassMenuItem) {
                newClassWindow();
            }
            else if (ae.getSource() == importClassMenuItem) {
                currentImportType = CLASS_IMPORT;
                importStatementWindow();
            }
            else if (ae.getSource() == importPackageMenuItem) {
                currentImportType = PACKAGE_IMPORT;
                importStatementWindow();
            }
            else if (ae.getSource() == addStatementPackageMenuItem) {
                addPackageStatementWindow();
            }
            else if (ae.getSource() == commandLineToolsMenuItem) {
                launchCommandLineTool();
            }
            else if (ae.getSource() == aboutHelpMenuItem) {
                aboutWindow();
            }
        }
    }

    private class ButtonHandler implements EventHandler<ActionEvent> {
        private Stage popupWindow;
        private String inputName;
        private String inputType;
        private Boolean isNewClass = false;
        public void handle(ActionEvent ae) {
            okButton.setDefaultButton(true);
            inputType = ((Button)ae.getSource()).getText();
            popupWindow = createPopUpWindow("New " + inputType, "Enter " + inputType + " name:", isNewClass);
            popupWindow.initModality(Modality.APPLICATION_MODAL);
            okButton.setOnAction(e -> {
                inputName = inputTextField.getText();
                if (!inputName.trim().isEmpty()) {
                    currentEditorPane = (EditorPane) editorTabPane.getSelectionModel().getSelectedItem().getContent();
                    currentEditorPane.addLine("\n" + inputType + " " + inputName + ";\n\n" + inputName + " = new " + inputType + "();");
                }
                popupWindow.hide();
            });
            popupWindow.showAndWait();
        }
    }

    public void createButtons() {
        toolBoxButtons = new Button[18];
        toolBoxButtons[0] = new Button("Stage");
        toolBoxButtons[1] = new Button("Scene");
        toolBoxButtons[2] = new Button("BorderPane");
        toolBoxButtons[3] = new Button("GridPane");
        toolBoxButtons[4] = new Button("SplitPane");
        toolBoxButtons[5] = new Button("TabPane");
        toolBoxButtons[6] = new Button("VBox");
        toolBoxButtons[7] = new Button("HBox");
        toolBoxButtons[8] = new Button("Tab");
        toolBoxButtons[9] = new Button("MenuBar");
        toolBoxButtons[10] = new Button("Menu");
        toolBoxButtons[11] = new Button("MenuItem");
        toolBoxButtons[12] = new Button("Button");
        toolBoxButtons[13] = new Button("RadioButton");
        toolBoxButtons[14] = new Button("ToggleGroup");
        toolBoxButtons[15] = new Button("Label");
        toolBoxButtons[16] = new Button("TextField");
        toolBoxButtons[17] = new Button("TextArea");
        
        for (Button button: toolBoxButtons) {
            button.setPrefWidth(100);
        }
    }

    public void checkSystem() {          //checks if operating system is Windows or MacOS to know which command line tool to use
        String osName = System.getProperty("os.name").toLowerCase();
        isMacOS = osName.startsWith("mac os x");
    }

    public void createMenus() {
        menuBar = new MenuBar();

        fileMenu = new Menu("_File");
        newFileMenuItem = new MenuItem("New");
        openFileMenuItem = new MenuItem("Open");
        saveAsFileMenuItem = new MenuItem("Save As");
        saveFileMenuItem = new MenuItem("Save");
        exitFileMenuItem = new MenuItem("Exit");

        newFileMenuItem.setAccelerator(KeyCombination.keyCombination("SHORTCUT + N"));
        openFileMenuItem.setAccelerator(KeyCombination.keyCombination("SHORTCUT + O"));
        saveAsFileMenuItem.setAccelerator(KeyCombination.keyCombination("SHIFT + SHORTCUT + S"));
        saveFileMenuItem.setAccelerator(KeyCombination.keyCombination("SHORTCUT + S"));
        exitFileMenuItem.setAccelerator(KeyCombination.keyCombination("SHORTCUT + Q"));

        fileMenu.getItems().add(newFileMenuItem);
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(openFileMenuItem);
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(saveAsFileMenuItem);
        fileMenu.getItems().add(saveFileMenuItem);
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(exitFileMenuItem);

        classMenu = new Menu("_Class");
        newClassMenuItem = new MenuItem("New Class");
        importClassMenuItem = new MenuItem("Import Class");

        newClassMenuItem.setAccelerator(KeyCombination.keyCombination("SHIFT + SHORTCUT + N"));
        importClassMenuItem.setAccelerator(KeyCombination.keyCombination("SHIFT + SHORTCUT + C"));

        classMenu.getItems().add(newClassMenuItem);
        classMenu.getItems().add(importClassMenuItem);

        packageMenu = new Menu("_Package");
        importPackageMenuItem = new MenuItem("Import Package");
        addStatementPackageMenuItem = new MenuItem("Add Statement");

        importPackageMenuItem.setAccelerator(KeyCombination.keyCombination("SHORTCUT + I"));
        addStatementPackageMenuItem.setAccelerator(KeyCombination.keyCombination("SHORTCUT + P"));

        packageMenu.getItems().add(importPackageMenuItem);
        packageMenu.getItems().add(addStatementPackageMenuItem);

        toolsMenu = new Menu("_Tools");
        if (isMacOS) {
            commandLineToolsMenuItem = new MenuItem("Terminal");
        }
        else {
            commandLineToolsMenuItem = new MenuItem("MS_DOS Shell");
        }

        commandLineToolsMenuItem.setAccelerator(KeyCombination.keyCombination("SHORTCUT + W"));

        toolsMenu.getItems().add(commandLineToolsMenuItem);

        helpMenu = new Menu("_Help");
        aboutHelpMenuItem = new MenuItem("About");

        aboutHelpMenuItem.setAccelerator(KeyCombination.keyCombination("SHORTCUT + ;"));

        helpMenu.getItems().add(aboutHelpMenuItem);

        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(classMenu);
        menuBar.getMenus().add(packageMenu);
        menuBar.getMenus().add(toolsMenu);
        menuBar.getMenus().add(helpMenu);
    }

    public void createAndRegisterHandler(Stage primaryStage) {
        menuHandler = new MenuHandler(primaryStage);
        openFileMenuItem.setOnAction(menuHandler);
        saveAsFileMenuItem.setOnAction(menuHandler);
        saveFileMenuItem.setOnAction(menuHandler);
        exitFileMenuItem.setOnAction(menuHandler);
        newClassMenuItem.setOnAction(menuHandler);
        importClassMenuItem.setOnAction(menuHandler);
        importPackageMenuItem.setOnAction(menuHandler);
        addStatementPackageMenuItem.setOnAction(menuHandler);
        commandLineToolsMenuItem.setOnAction(menuHandler);
        aboutHelpMenuItem.setOnAction(menuHandler);
        newFileMenuItem.setOnAction(menuHandler);
        buttonHandler = new ButtonHandler();
        for (Button button: toolBoxButtons) {
            button.setOnAction(buttonHandler);
        }
    }

    public void createButtonsToolBox() {
        toolBoxLabel = new Label("Tool Box:");
        buttonsToolBox = new VBox(15);
        buttonsToolBox.setAlignment(Pos.CENTER);
        
        for (Button button : toolBoxButtons) {
            buttonsToolBox.getChildren().add(button);
        }
    }

    public void addNewTab(EditorPane editorPane, String title) {
        newTab = new Tab(title);
        newTab.setContent(editorPane);
        editorTabPane.getTabs().add(newTab);
        editorTabPane.getSelectionModel().select(newTab);
    }

    public void createSplitPane() {
        splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.setDividerPosition(0, 0.85);
        splitPane.setResizableWithParent(buttonsToolBox, false);

        splitPane.getItems().add(editorTabPane);
        splitPane.getItems().add(buttonsToolBox);
    }

    public void createMainPane() {
        mainPane = new BorderPane();
        mainPane.setTop(menuBar);
        mainPane.setCenter(splitPane);
    }

    public void showFileNotFoundDialog() {
        Alert errorAlert;

        errorAlert = new Alert(AlertType.ERROR);
        errorAlert.setTitle("Error");
        errorAlert.setHeaderText("File not found");
        errorAlert.setContentText("Could not find output file!");
        errorAlert.showAndWait();
    }

    public void showIODialog() {
        Alert errorAlert;

        errorAlert = new Alert(AlertType.ERROR);
        errorAlert.setTitle("Error");
        errorAlert.setHeaderText("Write error");
        errorAlert.setContentText("Could not write to file!");
        errorAlert.showAndWait();
    }

    public void showSavedDialog() {
        Alert savedAlert;
        savedAlert = new Alert(AlertType.INFORMATION);
        savedAlert.setTitle("Success!");
        savedAlert.setHeaderText(null);
        savedAlert.setContentText("File saved!");
        savedAlert.show();
        PauseTransition wait = new PauseTransition(Duration.seconds(1));
        wait.setOnFinished((e) -> {
            savedAlert.hide();
            wait.playFromStart();
        });
        wait.play();
    }

    public void save(Stage primaryStage) {
        File outputFile;
        Boolean isSaved;
        
        outputFile = null;
        isSaved = false;
        
        fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JAVA", "*.java"));
		fileChooser.setTitle("Save as");

        outputFile = fileChooser.showSaveDialog(primaryStage);
        

        if (outputFile != null) {
            editorTabPane.getSelectionModel().getSelectedItem().setText(outputFile.getName().toString());
            try {
                editorPane.setCurrentFile(outputFile);
                editorPane.saveCurrentFile();
                isSaved = true;
            }
            catch (FileNotFoundException fnfe) {
                showFileNotFoundDialog();
            }
            catch (IOException ioe) {
                showIODialog();
            }
            finally {
                if (isSaved) {
                    showSavedDialog();
                }
            }
        }
        else {
            return;
        }
    }//end of save(Stage)

    public void saveToExistingFile(Stage primaryStage) {
        EditorPane currentEditorPane;
        File currentFile = null;
        Boolean isSaved = false;

        currentEditorPane = (EditorPane) editorTabPane.getSelectionModel().getSelectedItem().getContent();
        currentFile = currentEditorPane.getCurrentFile();
        if (currentFile == null) {
            save(primaryStage);
            return;
        }

        try {
            currentEditorPane.saveCurrentFile();
            isSaved = true;
        }
        catch (FileNotFoundException fnfe) {
            showFileNotFoundDialog();
        }
        catch (IOException ioe) {
            showIODialog();
        }
        finally {
            if (isSaved) {
                showSavedDialog();
            }
        }
    }//end of saveToExistingFile(Stage) method

    public void open(Stage primaryStage) {
        Tab newTab;
        EditorPane newEditorPane;
        File inputFile;
        boolean isOpened = false;
        Alert errorAlert;
        Alert openAlert;
        
        newEditorPane = new EditorPane();
        inputFile = null;
        errorAlert = new Alert(AlertType.ERROR);
        openAlert = new Alert(AlertType.INFORMATION);

		fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JAVA", "*.java"));
		fileChooser.setTitle("Open");

        inputFile = fileChooser.showOpenDialog(primaryStage);
        
        if (inputFile != null) {
            newTab = new Tab(inputFile.getName());
            newTab.setContent(newEditorPane);
            editorTabPane.getTabs().add(newTab);
            try {
                newEditorPane.openFile(inputFile);
                isOpened = true;
            }
            catch (FileNotFoundException fnfe) {
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("File not found");
                errorAlert.setContentText("Could not find input file!");
                errorAlert.showAndWait();
            }
            catch (IOException ioe) {
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Read error");
                errorAlert.setContentText("Could not read from file!");
                errorAlert.showAndWait();
            }
            finally {
                if (isOpened) {
                    editorTabPane.getSelectionModel().select(newTab);
                }
            }
        }
        else {
            return;
        }
    }//end of open(Stage) method

    public void saveTabs(Stage primaryStage) {
        EditorPane editorPane;
        PrintWriter out;
        File file;

        file = null;
        out = null;
           
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter("openTabs.txt")));
            for (Tab tab: editorTabPane.getTabs()) {
                if (tab.getText() != "New") {
                    editorPane = (EditorPane) tab.getContent();
                    file = editorPane.getCurrentFile();
                    if (file != null) {
                        out.println(file);
                        editorPane.saveCurrentFile();
                    }
                }
            }
        }
        catch (FileNotFoundException fnfe) {
            System.out.println("File not found");
        }
        catch (IOException ioe) {
            System.out.println("Could not save to file");
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }//end of saveTabs(Stage)

    public void openTabs() {
        Tab newTab;
        EditorPane newEditorPane;
        Scanner fileScanner;
        File tabsFile;
        File inputFile;
        String line;
        Alert errorAlert;

        fileScanner = null;
        tabsFile = new File("openTabs.txt");
        inputFile = null;
        errorAlert = new Alert(AlertType.ERROR);

        try {
            if (tabsFile.exists()) {
                fileScanner = new Scanner(tabsFile);

                while (fileScanner.hasNextLine()){
                    line = fileScanner.nextLine().trim();
                    inputFile = new File(line);
                    newEditorPane = new EditorPane();
                    newTab = new Tab(inputFile.getName());
                    newTab.setContent(newEditorPane);
                    editorTabPane.getTabs().add(newTab);
                    

                    if (inputFile != null) {
                        newEditorPane.openFile(inputFile);
                    }
                }
            }
        }
        catch (FileNotFoundException fnfe) {
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("File not found");
            errorAlert.setContentText("Could not find file!");
            errorAlert.showAndWait();
        }
        catch (IOException ioe) {
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Read error");
            errorAlert.setContentText("Could not open file!");
            errorAlert.showAndWait();
        }
        finally {
            if (fileScanner != null) {
                fileScanner.close();
            }
        }
    }//end of opentTabs()

    public void newClassWindow() {
        Stage newClassWindow = createPopUpWindow("New Class", "Enter the class name:", true);
        
        newClassWindow.initModality(Modality.APPLICATION_MODAL);
        okButton.setOnAction(ae -> {
            String className = inputTextField.getText();
            if (!className.trim().isEmpty()) {
                newEditorPane = new EditorPane();
                if (addMainRadioButton.isSelected()) {
                    newEditorPane.addLine("public class " + className.trim() + " {\n\tpublic static void main(String[] args) {\n\n\t}//end of main method\n}//end of class");
                }
                else {
                    newEditorPane.addLine("public class " + className.trim() + " {\n\n}//end of class");
                }
                addNewTab(newEditorPane, className.trim() + ".java");
            }
            newClassWindow.hide();
        });
        newClassWindow.showAndWait();

    }//end of newClassWindow() method

    public void importStatementWindow() {
        String importInstructions = null;
        String title = null;
        
        if (currentImportType == CLASS_IMPORT) {
            title = "Import Class";
            importInstructions = "Enter the class to import:";
        }
        else if (currentImportType == PACKAGE_IMPORT) {
            title = "Import Package";
            importInstructions = "Enter the package to import:";
        }
        Stage importClassWindow = createPopUpWindow(title, importInstructions, false);
        
        importClassWindow.initModality(Modality.APPLICATION_MODAL);
        okButton.setOnAction(ae -> {
            String importName = inputTextField.getText();
            if (!importName.trim().isEmpty()) {
                currentEditorPane = (EditorPane) editorTabPane.getSelectionModel().getSelectedItem().getContent();
                if (EditorGUI.this.currentImportType == CLASS_IMPORT) {
                    currentEditorPane.addLine("import " + importName + ";\n");
                }
                else if (EditorGUI.this.currentImportType == PACKAGE_IMPORT) {
                    currentEditorPane.addLine("import " + importName + ".*;\n");
                }
            }
            importClassWindow.hide();
        });
        importClassWindow.showAndWait();
    }//end of importStatementWindow() method

    public void addPackageStatementWindow() {
        Stage addPackageWindow = createPopUpWindow("Add Package", "Enter package name:", false);

        addPackageWindow.initModality(Modality.APPLICATION_MODAL);
        okButton.setOnAction(ae -> {
            String packageName = inputTextField.getText();
            if (!packageName.trim().isEmpty()) {
                currentEditorPane = (EditorPane) editorTabPane.getSelectionModel().getSelectedItem().getContent();
                currentEditorPane.addLine("package " + packageName + ";\n");
            }
            addPackageWindow.hide();
        });
        addPackageWindow.showAndWait();
    }//end of addPackageStatementWindow() method

    public void launchCommandLineTool() {
        try {
            if (isMacOS) {        //based on which operating system is running: Windows, MacOS
                Process p = Runtime.getRuntime().exec("open -n -F -a /System/Applications/Utilities/Terminal.app");
            }
            else {
                ProcessBuilder newProcessBuilder = new ProcessBuilder("cmd", "/c", "start");
                newProcessBuilder.start();
            } 
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }//end of launchCommandLineTool() method

    public void aboutWindow() {
        Stage aboutStage = new Stage();
        Label titleLabel = new Label("Java Code Editor v1.0");
        Label infoLabel = new Label("\nJava Code Editor version 1.0 provides a clean and helpful tool to write " +
                                        "\nyour java/javafx code in. Features include auto-code-insertion for new " + 
                                        "\nclasses, import statements and packages. Command-line features allow you " + 
                                        "\nto open a command-line shell from within Java Code Editor. Also with the " + 
                                        "\ntoolbox you can auto-create different javafx controls with a click of a button." + 
                                        "\n\n\u00a92020 Java Code Editor Version: 1.0 | Release: May 2020 | James Hembree\n\n");
        titleLabel.setFont(Font.font("", FontWeight.BOLD, 25));
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);
        GridPane gridPane = new GridPane();
        gridPane.setFillWidth(titleLabel, true);
        gridPane.setHgap(10);
        gridPane.setVgap(5);
        gridPane.setPadding(new Insets(20, 10, 15, 10));
        gridPane.add(titleLabel, 0, 0);
        gridPane.add(infoLabel, 0, 1);
        okButton.setPrefSize(75, 15);
        gridPane.add(okButton, 0, 2);
        gridPane.setAlignment(Pos.CENTER);

        scene = new Scene(gridPane, 440, 245);
        aboutStage.setTitle("About");
        aboutStage.setScene(scene);
        aboutStage.initModality(Modality.APPLICATION_MODAL);
        okButton.setOnAction(ae -> {
            aboutStage.hide();
        });
        aboutStage.showAndWait();
    }//end of aboutWindow()

    public Stage createPopUpWindow(String title, String instructions, boolean isNewClassWindow) {
        Stage stage = new Stage();
        Label instructionLabel = new Label(instructions);
        addMainRadioButton = new RadioButton("Add Main Method");
        inputTextField = new TextField("");
        okButton.setPrefSize(75, 15);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(15);
        gridPane.setPadding(new Insets(20, 10, 25, 10));
        gridPane.add(instructionLabel, 0, 0);
        if (isNewClassWindow) {
            gridPane.add(addMainRadioButton, 0, 1);
        }
        gridPane.add(inputTextField, 1, 0);
        gridPane.add(okButton, 1, 1);

        scene = new Scene(gridPane, 350, 100);
        inputTextField.requestFocus();
        stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);

        return stage;
    }//end of createPopUpWindow(String, String, boolean) method
}//end of class
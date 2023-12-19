import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class TextGame implements Serializable /*штука что бы работало созранение\загрузка */ {
    public static final Scanner scanner = new Scanner(System.in);
    public static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
    private static final int BOARD_SIZE = 10;
    private static final int WIN_THRESHOLD = BOARD_SIZE * BOARD_SIZE / 2;

    private int[][] gameBoard;
    private int playerPeasants;
    private int waterSupply;
    private int riceSupply;
    private int playerHome;
    private int opponentPeasants;
    private int opponentWaterSupply;
    private int opponentRiceSupply;
    private int opponentHome;
    private int day;

    private TextGame() /* основные переменные */ {
        this.gameBoard = new int[BOARD_SIZE][BOARD_SIZE];
        this.playerPeasants = 10;        // крестьяне игрока
        this.waterSupply = 0;          // Начальный запас воды для игрока
        this.riceSupply = 10;           // Начальный запас риса для игрока
        this.playerHome = 0;
        this.opponentPeasants = 1;        // крестьяне оппонента
        this.opponentWaterSupply = 10;    // Начальный запас воды для оппонента
        this.opponentRiceSupply = 10;     // Начальный запас риса для оппонента
        this.opponentHome = 0;
        this.day = 1;

        initializeGameBoard();
    }

    private void initializeGameBoard() /* заполнение игрового поля значениями крестьян нужных для захвата этой области*/ {
        Random random = new Random();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                gameBoard[i][j] = random.nextInt(5) + 1;
            }
        }
        gameBoard[0][0] = -1;
        gameBoard[BOARD_SIZE - 1][BOARD_SIZE - 1] = 0;
    }

    public void printGameStatus() /* вывод статуса всех переменных */ {
        System.out.println("Day " + day);
        System.out.println("Player Peasants: " + playerPeasants);
        System.out.println("Player Homes: " + playerHome);
        System.out.println("Water Supply: " + waterSupply);
        System.out.println("Rice Supply: " + riceSupply);
        System.out.println("Opponent Peasants: " + opponentPeasants);
        System.out.println("Opponent Homes " + opponentHome);
        System.out.println("Opponent water Supply: " + opponentWaterSupply);
        System.out.println("Opponent rice supply: " + opponentRiceSupply);
        System.out.println("Player's Territory Control: " + calculatePlayerTerritoryControl() + "%");
        System.out.println("Opponent's Territory Control: " + calculateOpponentTerritoryControl() + "%");
        System.out.println();
        System.out.println("Game Board:");
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++)  {
                System.out.printf("%2d",gameBoard[i][j]);
            }
            System.out.println();
        }
    }

    public int calculatePlayerTerritoryControl() /* подсчет количества ячеек на поле со значением "0"
                                                  и вычиления % захваченных территорий */ {
        int playerControl = 0;
        for (int[] row : gameBoard) {
            for (int cell : row) {
                if (cell == 0) {
                    playerControl++;
                }
            }
        }
        return (playerControl * 100) / (WIN_THRESHOLD * 2);
    }

    public int calculateOpponentTerritoryControl() /* подсчет захваченных оппонентом клеток */ {
        int OpponentControl = 0;
        for (int[] row : gameBoard) { // перебор значений в клетках на карте
            for (int cell : row) {
                if (cell == -1) {
                    OpponentControl++;
                }
            }
        }
        return (OpponentControl * 100) / (WIN_THRESHOLD * 2);
    }

    public void playerTurn() /* Ход игрока.*/ {
        System.out.println();
        System.out.println("Your move:\n" +
                "1. Get some water (+" + playerPeasants + ")\n" +
                "2. Pour over the rice (+" + waterSupply + ", water = 0)\n" +
                "3. Build a house (+1, -1 water, -1 rice,  -1 peasants\n" +
                "4. capture the territory\n" +
                "5. save the game");
        int choose = getIntInput("Enter your choice : ");
        switch (choose) {
            case 1:
                collectWater();
                break;
            case 2:
                plantRice();
                break;
            case 3:
                buildHouse();
                break;
            case 4:
                System.out.println("Enter the coordinates of the point to capture (row and column separated by a space):");
                int row = scanner.nextInt();
                int col = scanner.nextInt();
                capturePoint(row - 1, col - 1);
                break;
            case 5:
                System.out.print("Enter the file name for saving the game: ");
                String fileName = scanner.nextLine();
                saveGame(fileName);
                playerTurn();
                break;
            default:
                System.out.println();
                System.out.println("There is no such function");
                playerTurn();
                break;
        }
    }

    public void collectWater() {
        waterSupply += playerPeasants;
    }

    public void plantRice() {
        riceSupply += waterSupply;
        waterSupply -= waterSupply;
    }

    public void buildHouse() {
        if (riceSupply >= 1 && waterSupply >= 1 && playerPeasants >= 1) {
            riceSupply -= 1;
            waterSupply -= 1;
            playerPeasants -= 1;
            playerHome += 1;
        } else {
            System.out.println("Not enough resources to build a house.");
            playerTurn();
        }
    }

    public void capturePoint(int row, int col) /*Метод для захвата точки*/  {
        // Проверка, чтобы не выйти за пределы игровой доски
        if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
            if (gameBoard[row - 1][col - 1] > playerPeasants) /*проверка на наличие ресурсов для захвата*/ {
                System.out.println("Not enough peasants to capture the point.\n" +
                        "");
                playerTurn();
            } else {
                int valueToCapture = gameBoard[row][col];
                // Проверка наличия рядом точек по горизонтали или вертикали со значением 0
                if ((row > 0 && gameBoard[row - 1][col] == 0) ||
                        (row < BOARD_SIZE - 1 && gameBoard[row + 1][col] == 0) ||
                        (col > 0 && gameBoard[row][col - 1] == 0) ||
                        (col < BOARD_SIZE - 1 && gameBoard[row][col + 1] == 0)) {
                    if (valueToCapture != 0 && valueToCapture != -1) {
                        // Захват точки
                        playerPeasants -= valueToCapture;
                        gameBoard[row][col] = 0;
                        System.out.println("The point is captured! There are no resources left: " + playerPeasants);
                    } else {
                        System.out.println("The point has already been captured.");
                        playerTurn();
                    }
                } else {
                    System.out.println("There are no suitable capture points nearby.");
                    playerTurn();
                }
            }
        }
        else{
            System.out.println("Incorrect coordinates of the point.");
            playerTurn();
        }
    }




    public void opponentTurn() /* Ход оппонента.*/ {
        int OpponentChoose;
        if (opponentPeasants >= 5) {
            OpponentChoose = 4;
        }
        else {
            if (opponentWaterSupply >= 1 && opponentRiceSupply >= 1 && opponentPeasants >= 1) {
                OpponentChoose = 3;
            }
            else {
                if (opponentWaterSupply <= 0) {
                    OpponentChoose = 1;
                }
                else {
                    OpponentChoose = 2;
                }
            }
        }
        switch (OpponentChoose) {
            case 1:
                collectOpponentWater();
                break;
            case 2:
                plantOpponentRice();
                break;
            case 3:
                buildOpponentHouse();
                break;
            case 4:
                captureAdjacentPointForOpponent(-1);
                break;
        }
    }

    public void collectOpponentWater() {
        opponentWaterSupply += opponentPeasants;
    }

    public void plantOpponentRice() {
        opponentRiceSupply += opponentWaterSupply;
        opponentWaterSupply = 0;
    }

    public void buildOpponentHouse() {
        opponentRiceSupply -= 1;
        opponentWaterSupply -= 1;
        opponentPeasants -= 1;
        opponentHome += 1;
    }

    public void captureAdjacentPointForOpponent(int valToCap) {
        ArrayList<int[]> neighborPoints = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) /*Проход по всей игровой доске */ {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (gameBoard[i][j] == valToCap) /*Если текущая ячейка содержит значение "-1"*/  {
                    // Сохранение всех соседних точек для оппонента
                    saveNeighborPoints(i, j, neighborPoints);
                }
            }
        }

        // Выбор случайной точки из сохраненных
        if (!neighborPoints.isEmpty()) {
            Random random = new Random();
            int[] randomPoint = neighborPoints.get(random.nextInt(neighborPoints.size()));
            captureNeighborOpponent(randomPoint[0], randomPoint[1]);
        }
    }

    public void saveNeighborPoints(int row, int col, ArrayList<int[]> neighborPoints) {
        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                // Проверка, чтобы не выйти за пределы игровой доски
                if (i >= 0 && i < BOARD_SIZE && j >= 0 && j < BOARD_SIZE) {
                    // Сохранение точек соседей для оппонента
                    if (gameBoard[i][j] != -1 && gameBoard[i][j] != 0) {
                        neighborPoints.add(new int[]{i, j});
                    }
                }
            }
        }
    }
    public void captureNeighborOpponent(int row, int col) /*Проход по окружающим ячейкам (соседям) текущей ячейки*/ {
        // Проверка, чтобы не выйти за пределы игровой доски
        if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
            // Захват точки соседа для оппонента
            opponentPeasants -= gameBoard[row][col];
            gameBoard[row][col] = -1;
            return;  // Выходим из метода
        }
    }

    public void saveGame(String fileName) {
        if (!fileName.endsWith(".ser")) {
            fileName += ".ser";
        }
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
            outputStream.writeObject(this);
            System.out.println("Game saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playGame() /* проверка на победу, ход игрока и оппонента, окончание дня*/ {
        while (calculatePlayerTerritoryControl() < 50 && calculateOpponentTerritoryControl() < 50 && playerPeasants !=0) {
            System.out.println("\n------ Player's Turn ------");
            printGameStatus();
            playerTurn();
            opponentTurn();

            day++;

            riceSupply += playerPeasants;
            playerPeasants += playerHome;

            opponentRiceSupply += opponentPeasants;
            opponentPeasants += opponentHome;
        }
        if (calculatePlayerTerritoryControl() >= 50) {
            System.out.println("Congratulations! You have won the game!");
        } else {
            System.out.println("Game over. Opponent has won.");
        }
    }

    public static boolean getUserChoice() /*интерфейс начала игры*/ {
        while (true) {
            System.out.println("Choose an option: \n" +
                    "1. Start a new game \n" +
                    "2. Load a saved game");
            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    return true;  // Новая игра
                case 2:
                    return false; // Загрузить сохраненную игру
                default:
                    System.out.println("Invalid choice. Please enter 1 or 2.");
            }
        }
    }

    public void startNewGame() /*вызов наччала новой игры*/{
        playGame();
    }

    public void loadSavedGame() /*  загрузку сохраненной игры из файла */ {
        // Логика загрузки сохраненной игры
        String fileName = selectSavedFile();
        if (fileName != null) {
            // Загрузить сохраненную игру из выбранного файла
            System.out.println("Loading saved game from file: " + fileName);
            try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileName))) {
                TextGame loadedGame = (TextGame) inputStream.readObject();
                this.gameBoard = loadedGame.gameBoard;
                this.playerPeasants = loadedGame.playerPeasants;
                this.opponentPeasants = loadedGame.opponentPeasants;
                this.waterSupply = loadedGame.waterSupply;
                this.riceSupply = loadedGame.riceSupply;
                this.playerHome = loadedGame.riceSupply;
                this.opponentWaterSupply = loadedGame.opponentWaterSupply;
                this.opponentRiceSupply = loadedGame.opponentRiceSupply;
                this.opponentHome = loadedGame.opponentHome;
                this.day = loadedGame.day;
                System.out.println("Game loaded successfully.");
            }
            catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("No saved files found. Starting a new game...");
            startNewGame();
        }
    }

    public String selectSavedFile() {
        // Предоставить список сохраненных файлов и запросить выбор пользователя
        File[] savedFiles = getSavedFiles();

        if (savedFiles.length == 0) {
            System.out.println("No saved files found.");
            return null;
        }
        System.out.println("Select a saved file to load:"); // выводит список имеющихся файлов для загрузки
        for (int i = 0; i < savedFiles.length; i++) {
            System.out.println((i + 1) + ". " + savedFiles[i].getName());
        }
        int choice = getIntInput("Enter the number of the file to load (1-" + savedFiles.length + "): ");
        if (choice >= 1 && choice <= savedFiles.length) {
            return savedFiles[choice - 1].getName();
        } else {
            System.out.println("Invalid choice. Loading the first saved file.");
            return savedFiles[0].getName();
        }
    }

    public File[] getSavedFiles() {
        String parentDirectory = System.getProperty("user.dir"); // Получение текущего рабочего каталога
        File[] savedFiles = new File(parentDirectory).listFiles((dir, name) -> name.endsWith(".ser"));

        /*System.out.println("где смотрит файл сохранения " + parentDirectory);
        System.out.println("что нашел: " + Arrays.toString(savedFiles)); */

        return savedFiles;
    }

    public static void manual() {
        System.out.println("1. You start in the lower right corner, and your opponent starts in the upper left corner.\n" +
                "2. Your territory is marked as \"0\".\n" +
                "3. The opponent's territory is marked as \"-1\".\n" +
                "4. At the end of the day, rice is added according to the number of your peasants,\n" +
                "then the peasants in the number of your houses.\n" +
                "5. To capture a territory, it is required to spend the number of peasants specified in this territory\n" +
                "6. The winner is the one who first captures 50% of the territory.");
        System.out.println();
    }



    public static void main(String[] args) {
        System.out.println();
        TextGame textGame = new TextGame();
        manual();
        // Предоставить пользователю выбор между началом новой игры и загрузкой сохраненной
        boolean isNewGame = getUserChoice();

        if (isNewGame) {
            // Начать новую игру
            textGame.startNewGame();
        } else {
            // Загрузить сохраненную игру
            textGame.loadSavedGame();
            textGame.playGame();
        }
    }
}
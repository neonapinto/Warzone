package controller;

import model.GameController;
import model.GameMap;
import model.GamePhase;
import utils.MapReader;
import utils.MapValidation;
import utils.ValidationException;
import utils.logger.LogEntryBuffer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * This class is used to create map using game console commands.
 *
 * @author Prathika Suvarna
 * @author Neona Pinto
 * @author Dhananjay Narayan
 * @author Surya Manian
 * @author Madhuvanthi Hemanathan
 * @version 1.0.0
 */
public class MapEditor implements GameController {
    /**
     * A data member for scanner
     */
    private final Scanner SCANNER = new Scanner(System.in);
    /**
     * A data member that stores the list of commands for mapeditor as list
     */
    private final List<String> CLI_COMMANDS = Arrays.asList("editcontinent", "editcountry", "editneighbor", "showmap", "savemap", "editmap", "validatemap");
    GameMap d_GameMap;
    GamePhase d_NextState = GamePhase.StartUp;
    private LogEntryBuffer d_Logger = LogEntryBuffer.getInstance();

    /**
     * This is the default constructor
     */
    public MapEditor() {
        this.d_GameMap = GameMap.getInstance();
    }

    /**
     * The start method of MapEditor phase that handles creation, validation
     * save of map from console commands.
     *
     * @param p_GamePhase Parameter of the enum GamePhase is passed
     * @throws ValidationException when validation fails
     */
    @Override
    public GamePhase start(GamePhase p_GamePhase) throws ValidationException, IOException {
        d_Logger.clear();
        d_Logger.log("/************************************ You are in MAP EDITOR PHASE *******************************/");
        while (true) {
            d_Logger.log("Enter your map operation:" + "\n" + "1. Enter help to view the set of commands" + "\n" + "2. Enter exit to end map creation and save phase");
            d_Logger.log("-----------------------------------------------------------------------------------------");
            String l_Input = SCANNER.nextLine();
            List<String> l_InputList;
            if (l_Input.contains("-")) {
                l_InputList = Arrays.stream(l_Input.split("-"))
                        .filter(s -> !s.isEmpty())
                        .map(String::trim)
                        .collect(Collectors.toList());
            } else {
                l_InputList = Arrays.stream(l_Input.split(" ")).collect(Collectors.toList());
            }

            if (!inputValidator(l_InputList)) {
                if (l_Input.startsWith("exit")) {
                    l_InputList.add(0, "exit");
                } else {
                    l_InputList.clear();
                    // if not available in command list forcing to call help
                    l_InputList.add("help");
                    l_InputList.add("dummy");
                }
            }

            /**
             * Handle editcontinent command from console
             */

            String l_MainCommand = l_InputList.get(0);
            l_InputList.remove(l_MainCommand);
            for (String l_Command : l_InputList) {
                String[] l_CommandArray = l_Command.split(" ");
                switch (l_MainCommand.toLowerCase()) {
                    case "editcontinent": {
                        if (l_CommandArray.length > 0) {
                            switch (l_CommandArray[0]) {
                                case "add": {
                                    if (l_CommandArray.length == 3) {
                                        d_GameMap.addContinent(l_CommandArray[1], l_CommandArray[2]);
                                    } else {
                                        throw new ValidationException();
                                    }
                                    break;
                                }
                                case "remove": {
                                    if (l_CommandArray.length == 2) {
                                        d_GameMap.removeContinent(l_CommandArray[1]);
                                    } else {
                                        throw new ValidationException();
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                    }

                    /**
                     * Handle editcountry command from console
                     */

                    case "editcountry": {
                        switch (l_CommandArray[0]) {
                            case "add": {
                                if (l_CommandArray.length == 3) {
                                    d_GameMap.addCountry(l_CommandArray[1], l_CommandArray[2]);
                                } else {
                                    throw new ValidationException();
                                }
                                break;
                            }
                            case "remove": {
                                if (l_CommandArray.length == 2) {
                                    d_GameMap.removeCountry(l_CommandArray[1]);
                                } else {
                                    throw new ValidationException();
                                }
                                break;
                            }
                        }
                        break;
                    }

                    /**
                     * Handle editneighbor command from console
                     */

                    case "editneighbor": {
                        switch (l_CommandArray[0]) {
                            case "add": {
                                if (l_CommandArray.length == 3) {
                                    d_GameMap.addNeighbor(l_CommandArray[1], l_CommandArray[2]);
                                } else {
                                    throw new ValidationException();
                                }
                                break;
                            }
                            case "remove": {
                                if (l_CommandArray.length == 3) {
                                    d_GameMap.removeNeighbor(l_CommandArray[1], l_CommandArray[2]);
                                } else {
                                    throw new ValidationException();
                                }
                                break;
                            }
                        }
                        break;
                    }


                    // Handle showmap command from console
                    case "showmap": {
                        d_GameMap.showMap();
                        break;
                    }
                    //Handle validatemap command from console
                    case "validatemap": {
                        if (MapValidation.validateMap(d_GameMap, 0)) {
                            d_Logger.log("Validation successful");
                        } else {
                            d_Logger.log("Validation failed");
                        }
                        break;
                    }


                    //Handle savemap command from console
                    case "savemap": {
                        if (l_CommandArray.length == 1) {
                            d_GameMap.setName(l_CommandArray[0]);
                            d_Logger.log(" Which format do you want to save the file? Type the number.");
                            d_Logger.log("1. Domination map \n2. Conquest map");
                            Scanner l_Scanner = new Scanner(System.in);
                            String l_UserInput = l_Scanner.nextLine();
                            if (l_UserInput.equals("1")){
                                d_GameMap.saveMap(false);
                                d_Logger.log("The loaded file is of the format Domination map");
                            }
                            else if (l_UserInput.equals("2")) {
                                d_GameMap.saveMap(true);
                                d_Logger.log("The loaded file is of the format Conquest map");
                            }
                            else
                                d_Logger.log("Please enter the right value");

                        }
                        break;
                    }


                    //Handle editmap command from console
                    case "editmap": {
                        if (l_CommandArray.length == 1) {
                            MapReader.readMap(d_GameMap, l_CommandArray[0]);
                        }
                        break;
                    }


                    //To exit the map creation phase type "exit"
                    case "exit": {
                        d_GameMap.flushGameMap();
                        d_GameMap.setGamePhase(d_NextState);
                        return p_GamePhase.nextState(d_NextState);
                    }
                    //Print the commands for help
                    default: {
                        d_Logger.log("List of user map creation commands from console:");
                        d_Logger.log("To add or remove a continent : editcontinent -add continentID continentvalue -remove continentID");
                        d_Logger.log("To add or remove a country : editcountry -add countryID continentID -remove countryID");
                        d_Logger.log("To add or remove a neighbor to a country : editneighbor -add countryID neighborcountryID -remove countryID neighborcountryID");
                        d_Logger.log("-----------------------------------------------------------------------------------------");
                        d_Logger.log("Map Commands(Edit/Save)");
                        d_Logger.log("To edit map: editmap filename");
                        d_Logger.log("To save map: savemap filename");
                        d_Logger.log("-----------------------------------------------------------------------------------------");
                        d_Logger.log("Additional map commands:");
                        d_Logger.log("To show the map: showmap");
                        d_Logger.log("To validate map: validatemap");
                        d_Logger.log("-----------------------------------------------------------------------------------------");

                    }
                }
            }
        }
    }

    /**
     * This method validates to check if the current cli command is executable
     * in the current phase
     *
     * @param p_InputList the command list from console
     * @return true if command is executable else false
     */
    public boolean inputValidator(List<String> p_InputList) {
        if (p_InputList.size() > 0) {
            String l_MainCommand = p_InputList.get(0);
            if (p_InputList.size() == 1) {
                p_InputList.add("dummy");
            }
            return CLI_COMMANDS.contains(l_MainCommand.toLowerCase());
        }
        return false;
    }

}

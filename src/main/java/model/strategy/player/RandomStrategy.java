package model.strategy.player;

import controller.IssueOrder;
import model.Card;
import model.Country;
import model.GameMap;
import model.Player;
import model.order.*;
import utils.logger.LogEntryBuffer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Random Strategy class, taking random commands for tournament mode.
 *
 * @author Neona Pinto
 * @version 1.0.0
 */
public class RandomStrategy extends PlayerStrategy implements Serializable {
    /**
     * Random variable
     */
    private static final Random d_Random = new Random();
    /**
     * GameMap instance
     */
    private static GameMap d_GameMap;
    /**
     * Logger Observable
     */
    private LogEntryBuffer d_Logger = LogEntryBuffer.getInstance();

    /**
     * Get a random player other than itself
     *
     * @param p_Player current player
     * @return Random Player
     */
    protected Player getRandomPlayer(Player p_Player) {
        List<Country> l_Enemies = d_Player.getCapturedCountries().stream()
                .flatMap(country -> country.getNeighbors().stream())
                .filter(country -> !country.getPlayer().getName().equals(d_Player.getName()))
                .collect(Collectors.toList());
        if (l_Enemies.size() > 0) {
            int l_Random = d_Random.nextInt(l_Enemies.size());
            return l_Enemies.get(l_Random).getPlayer();
        }
        return null;
    }

    /**
     * Get Random Country not belonging to the player
     *
     * @param p_Player current player
     * @return Random country
     */
    protected Country getRandomUnconqueredCountry(Player p_Player) {
        Country l_RandomCountry = null;
        if (d_GameMap.getCountries().size() > 0 && p_Player.getCapturedCountries().size() < d_GameMap.getCountries().size()) {
            int l_Index = d_Random.nextInt(d_GameMap.getCountries().size());
            l_RandomCountry = (Country) d_GameMap.getCountries().values().toArray()[l_Index];
            while (l_RandomCountry.getPlayer().equals(p_Player)) {
                l_Index = d_Random.nextInt(d_GameMap.getCountries().size());
                l_RandomCountry = (Country) d_GameMap.getCountries().values().toArray()[l_Index];
            }
        }
        return l_RandomCountry;
    }

    /**
     * Random country belonging to the player
     *
     * @param p_Player current player
     * @return random country
     */
    protected Country getRandomConqueredCountry(Player p_Player) {
        if (p_Player.getCapturedCountries().size() > 0) {
            int l_Index = d_Random.nextInt(p_Player.getCapturedCountries().size());
            return p_Player.getCapturedCountries().get(l_Index);
        }
        return null;
    }

    /**
     * Get the random neighbor of the country
     *
     * @param p_CurrentCountry current country
     * @return random neighbor
     */
    protected Country getRandomNeighbor(Country p_CurrentCountry) {
        if (Objects.isNull(p_CurrentCountry) || p_CurrentCountry.getNeighbors().size() == 0) {
            return null;
        }
        int l_Index = d_Random.nextInt(p_CurrentCountry.getNeighbors().size());
        return (Country) p_CurrentCountry.getNeighbors().toArray()[l_Index];
    }

    /**
     * Create the orders in random fashion
     *
     * @return command, the orders on creation
     */
    public String createCommand() {
        d_GameMap = GameMap.getInstance();
        d_Player = d_GameMap.getCurrentPlayer();
        Order l_Order = null;
        List<String> l_Commands = new ArrayList<>();
        String[] l_CommandsArr;
        //check if player can still play
        int l_Random = d_Random.nextInt(11);
        Country l_RandomCountry = getRandomConqueredCountry(d_Player);
        switch (l_Random) {
            case 0:
            case 1:
            case 2:
            case 3:
                if (Objects.nonNull(l_RandomCountry) && d_Player.getReinforcementArmies() > 0) {
                    l_Commands.add(0, "deploy");
                    l_Commands.add(1, l_RandomCountry.getName());
                    l_Commands.add(2, String.valueOf(d_Random.nextInt(d_Player.getReinforcementArmies()) + 1));
                    l_CommandsArr = l_Commands.toArray(new String[l_Commands.size()]);
                    l_Order = new DeployOrder();
                    l_Order.setOrderInfo(OrderCreater.GenerateDeployOrderInfo(l_CommandsArr, d_Player));
                    IssueOrder.Commands = l_Order.getOrderInfo().getCommand();
                    d_Logger.log(String.format("%s issuing new command: %s", d_Player.getName(), IssueOrder.Commands));
                    d_Player.issueOrder();
                }
                break;
            case 4:
            case 5:
            case 6:
                Country l_RandomNeighbor = getRandomNeighbor(l_RandomCountry);
                if (Objects.nonNull(l_RandomCountry) && Objects.nonNull(l_RandomNeighbor)) {
                    if (l_RandomCountry.getArmies() > 0) {
                        l_Commands.add(0, "advance");
                        l_Commands.add(1, l_RandomCountry.getName());
                        l_Commands.add(2, l_RandomNeighbor.getName());
                        l_Commands.add(3, String.valueOf(d_Random.nextInt(l_RandomCountry.getArmies())));
                        l_CommandsArr = l_Commands.toArray(new String[l_Commands.size()]);
                        l_Order = new AdvanceOrder();
                        l_Order.setOrderInfo(OrderCreater.GenerateAdvanceOrderInfo(l_CommandsArr, d_Player));
                        IssueOrder.Commands = l_Order.getOrderInfo().getCommand();
                        d_Logger.log(String.format("%s issuing new command: %s", d_Player.getName(), IssueOrder.Commands));
                        d_Player.issueOrder();
                        return "pass";
                    }
                    break;
                }
            case 7:
            case 8:
                if (d_Player.getPlayerCards().size() <= 0) {
                    break;
                }
                int l_RandomCardIdx = d_Random.nextInt(d_Player.getPlayerCards().size());
                Card l_Card = d_Player.getPlayerCards().get(l_RandomCardIdx);
                if (cardAttack(l_Card, l_RandomCountry)) {
                    return "pass";
                }
            default:
                return "pass";
        }
        return "";
    }

    private boolean cardAttack(Card l_Card, Country l_RandomCountry) {
        switch (l_Card.getCardType()) {
            case BLOCKADE:
                if (Objects.nonNull(l_RandomCountry)) {
                    List<String> l_Commands = new ArrayList<>();
                    l_Commands.add(0, "blockade");
                    l_Commands.add(1, l_RandomCountry.getName());
                    String[] l_CommandsArr = l_Commands.toArray(new String[l_Commands.size()]);
                    Order l_Order = new BlockadeOrder();
                    l_Order.setOrderInfo(OrderCreater.GenerateBlockadeOrderInfo(l_CommandsArr, d_Player));
                    IssueOrder.Commands = l_Order.getOrderInfo().getCommand();
                    d_Logger.log(String.format("%s issuing new command: %s", d_Player.getName(), IssueOrder.Commands));
                    d_Player.issueOrder();
                    return true;
                }
            case BOMB:
                Country l_RandomUnconqueredCountry = getRandomUnconqueredCountry(d_Player);
                if (Objects.nonNull(l_RandomUnconqueredCountry)) {
                    List<String> l_Commands = new ArrayList<>();
                    l_Commands.add(0, "bomb");
                    l_Commands.add(1, l_RandomUnconqueredCountry.getName());
                    String[] l_CommandsArr = l_Commands.toArray(new String[l_Commands.size()]);
                    Order l_Order = new BombOrder();
                    l_Order.setOrderInfo(OrderCreater.GenerateBombOrderInfo(l_CommandsArr, d_Player));
                    IssueOrder.Commands = l_Order.getOrderInfo().getCommand();
                    d_Logger.log(String.format("%s issuing new command: %s", d_Player.getName(), IssueOrder.Commands));
                    d_Player.issueOrder();
                    return true;
                }
            case AIRLIFT: {
                List<String> l_Commands = new ArrayList<>();
                Country l_FromCountry = getRandomConqueredCountry(d_Player);
                Country l_ToCountry = getRandomConqueredCountry(d_Player);
                if (Objects.nonNull(l_FromCountry) && Objects.nonNull(l_ToCountry)) {
                    l_Commands.add(0, "airlift");
                    l_Commands.add(1, l_FromCountry.getName());
                    l_Commands.add(2, l_ToCountry.getName());
                    l_Commands.add(3, String.valueOf(d_Random.nextInt(10)));
                    String[] l_CommandsArr = l_Commands.toArray(new String[l_Commands.size()]);
                    Order l_Order = new AirliftOrder();
                    l_Order.setOrderInfo(OrderCreater.GenerateAirliftOrderInfo(l_CommandsArr, d_Player));
                    IssueOrder.Commands = l_Order.getOrderInfo().getCommand();
                    d_Logger.log(String.format("%s issuing new command: %s", d_Player.getName(), IssueOrder.Commands));
                    d_Player.issueOrder();
                    return true;
                }
            }
            case DIPLOMACY: {
                Player l_RandomPlayer = getRandomPlayer(d_Player);
                List<String> l_Commands = new ArrayList<>();
                if (Objects.nonNull(l_RandomPlayer)) {
                    l_Commands.add(0, "negotiate");
                    l_Commands.add(1, l_RandomPlayer.getName());
                    String[] l_CommandsArr = l_Commands.toArray(new String[l_Commands.size()]);
                    Order l_Order = new NegotiateOrder();
                    l_Order.setOrderInfo(OrderCreater.GenerateNegotiateOrderInfo(l_CommandsArr, d_Player));
                    IssueOrder.Commands = l_Order.getOrderInfo().getCommand();
                    d_Logger.log(String.format("%s issuing new command: %s", d_Player.getName(), IssueOrder.Commands));
                    d_Player.issueOrder();
                    return true;
                }
            }
        }
        return false;
    }
}

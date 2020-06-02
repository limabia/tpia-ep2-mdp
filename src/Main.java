package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;

import src.MDPAction;

public class Main {
    static final String file_prefix = "navigation_";
    static final String file_format = ".net";
    static final String fixedGoalInitialState = "FixedGoalInitialState";
    static final String randomGoalInitialState = "RandomGoalInitialState";
    
    // Program must be run from top level folder (tpia-ep2-mdp) with command: java src/Main.java [-f | -r] [1-10]
    public static void main(String[] args) throws Exception, java.io.IOException {
        /**
         * -f navigation_number
         * -r navigation_number
         * 
         * -f -> fixed
         * -r -> random
         * navigation_number goes from 1 to 10
         */

        FileReader file;
        BufferedReader br;
        Problem problem;
        switch(args[0]) {
            case "-f":
                file = getFileReader(args[1], fixedGoalInitialState);
                br = new BufferedReader(file);
                problem = createProblem(br);
                file.close();
                break;
            case "-r":
                file = getFileReader(args[1], randomGoalInitialState);
                br = new BufferedReader(file);
                problem = createProblem(br);
                file.close();
                break;
            default:
                throw new IllegalArgumentException("Parameter not recognized.");
        }

        //executa algoritmos
    }

    public static FileReader getFileReader(String fileNumber, String folder) throws FileNotFoundException{
        String absolutePath = new File("").getAbsolutePath();
        return new FileReader(absolutePath+"/files/"+folder+"/"+file_prefix+fileNumber+file_format);
    }

    public static Problem createProblem( BufferedReader br ) throws IOException {
        Problem problem = new Problem();
        problem.actions = new HashMap<>();
        Map<String, List<SimpleEntry<String, Double>>> costs = new HashMap<>();

        long initTime = System.currentTimeMillis();

        while ( br.ready() ) {
            String line = br.readLine();
            
            switch( line ) {
                case "states":
                    line = br.readLine();
                    line = line.trim();
                    problem.states = line.split(", ");
                    break;

                case "cost":
                    line = br.readLine();
                    line = line.trim();
                    while( !line.equals("endcost") ) {
                        String[] cost_line = line.split(" ");

                        String currentState = cost_line[0];
                        String actionName = cost_line[1];
                        double cost = Double.parseDouble(cost_line[2]);

                        if ( costs.containsKey(currentState) ) {
                            List<SimpleEntry<String, Double>> costs_aux = costs.get(currentState);
                            costs_aux.add(new SimpleEntry<>(actionName, cost));
                            costs.replace(currentState, costs_aux);
                        }
                        else {
                            List<SimpleEntry<String, Double>> costs_aux = new ArrayList<>();
                            costs_aux.add(new SimpleEntry<>(actionName, cost));
                            costs.put(currentState, costs_aux);
                        }

                        line = br.readLine();
                        line = line.trim();
                    }
                    break;

                case "initialstate":
                    line = br.readLine();
                    line = line.trim();
                    while( !line.equals("endinitialstate") ) {

                        problem.initialState = line;

                        line = br.readLine();
                    }
                    break;
                
                case "goalstate":
                    line = br.readLine();
                    line = line.trim();
                    while( !line.equals("endgoalstate") ) {

                        problem.goalState = line;

                        line = br.readLine();
                    }
                    break;

                default:
                    if ( !line.equals("") ){
                        if( line.contains("action") ) {
                            line = line.trim();
                            
                            String[] action = line.split(" ");
                            String actionName = action[1];

                            line = br.readLine();
                            while( !line.equals("endaction") ) {
                                line = line.trim();

                                action = line.split(" ");
                                MDPAction mdpAction = new MDPAction(actionName, action[0],
                                    action[1], Double.parseDouble(action[2]) , Double.parseDouble(action[3]) 
                                );

                                if ( problem.actions.containsKey(mdpAction.currentState) ) {
                                    List<MDPAction> currentStateAction = problem.actions.get(mdpAction.currentState);
                                    currentStateAction.add(mdpAction);
                                    problem.actions.replace(mdpAction.currentState, currentStateAction);
                                }
                                else {
                                    List<MDPAction> mdpActions = new ArrayList<>();
                                    mdpActions.add(mdpAction);
                                    problem.actions.put(mdpAction.currentState, mdpActions);
                                }
                                
                                line = br.readLine();
                            }
                        }
                    }
                    break;
            }
        }
        
        for ( String state : problem.states ) {
            
            List<MDPAction> actions = problem.actions.get(state);
            List<SimpleEntry<String,Double>> actions_costs = costs.get(state);

            if ( actions_costs != null && actions != null ) {
                for (MDPAction action : actions) {
                    for (SimpleEntry<String,Double> simpleEntry : actions_costs) {
                        if ( simpleEntry.getKey().equals(action.actionName) ) {
                            action.cost = simpleEntry.getValue();
                        }
                    }
                }    

                problem.actions.replace(state, actions);
            }
            else {
                // se um dos dois eh nulo
                if ( ( actions != null && actions_costs == null ) 
                        || ( ( actions == null && actions_costs != null ) ) ) {
                    // verifica se as acoes nao sao para o mesmo estado
                    for ( MDPAction action : actions ) {
                        if ( !action.currentState.equals(action.successorState) ) {
                            String message = "Invalid state '" + state + "' actions join, ";
                            if ( actions == null ) {
                                message += "doesn't has actions.";
                            }
                            if ( actions_costs == null ) {
                                message += "doesn't has costs for actions.";
                            }
                            
                            throw new NullPointerException(message);
                        }
                    }
                }
            }
        }

        long finishTime = System.currentTimeMillis();
        long diff = finishTime - initTime;
        System.out.println("Parsing time: " + diff + "ms");

        return problem;
    }
}
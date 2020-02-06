package cellsociety.Controller;

import cellsociety.Model.ArrayGrid;
import cellsociety.Model.Grid;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.scene.paint.Color;

public class Wator extends Simulation{

  private int chronon = 0;
  int[] rDelta = {0,0,1,-1};
  int[] cDelta = {1,-1,0,0};
  int[][] sharkEnergy;
  private int empty = 0;
  private int fish = 1;
  private int shark = 2;
  private int shark_lives = 3;
  Random rand = new Random();

  @Override
  public void loadSimulationContents(File file) {
    List<String> cellTypes = List.of("fish", "shark");

    List<String> xmlvals = new ArrayList<String>();
    xmlvals.addAll(List.of("title", "author", "simulation", "width", "height","default"));
    for (String celltype : cellTypes) {
      xmlvals.addAll(List.of("num"+celltype, "state"+celltype,celltype));
    }
    XMLParser parser = new XMLParser("config");
    Map<String, String> configuration = parser.getInfo(file, xmlvals);
    System.out.println(configuration);

    SIMULATION_NAME = configuration.get("simulation");
    GRID_WIDTH = Integer.parseInt(configuration.get("width"));
    GRID_HEIGHT = Integer.parseInt(configuration.get("height"));

    simulationGrid = new ArrayGrid(GRID_WIDTH);
    sharkEnergy = new int[GRID_WIDTH][GRID_WIDTH];
    initializeGrid(cellTypes, configuration);

    initializeColorMap();
    createSharkEnergyGrid();
  }

  private void createSharkEnergyGrid() {
    for (int r = 0; r < simulationGrid.getSize(); r++) {
      for (int c = 0; c < simulationGrid.getSize(); c++) {
        if (simulationGrid.getCurrentState(r, c) == shark) {
          sharkEnergy[r][c] = shark_lives;
        }
      }
    }
  }

  @Override
  public void updateGrid() {
    chronon++;

    for (int r = 0; r < simulationGrid.getSize(); r++) {
      for (int c = 0; c < simulationGrid.getSize(); c++) {
        simulationGrid.checkNeighbors(r, c, false, false);
        if (simulationGrid.getCurrentState(r, c) == shark) { // Must use current state because sharks can move during updates
          if (sharkEnergy[r][c] <= 0) {
            simulationGrid.updateCell(r, c, empty);
          } else {
            sharkGoesTo(r, c);
          }
        } else if (simulationGrid.getCurrentState(r, c) == fish) { // Must use current state because fish can move during updates
          fishGoesTo(r, c);
        }
      }
    }

    for (int r = 0; r < simulationGrid.getSize(); r++) {
      for (int c = 0; c < simulationGrid.getSize(); c++) {
        if (sharkEnergy[r][c] != 0) {
          sharkEnergy[r][c]--;
        }
      }
    }

  }

  private void sharkGoesTo(int r, int c) {
    boolean fisheaten = false; // if fish not eaten after forst loop, shark has moved
    int[] neighbors = simulationGrid.checkNeighbors(r, c, false, false);

    // Below determines if there are any fish around the shark, if they are, they are eaten
    for (int i = 0; i < neighbors.length && i < 4; i++) {
      if (neighbors[i] == fish) { // take first neighbor that is fish
        if (simulationGrid.inBounds(r + rDelta[i], c + cDelta[i])) {
          sharkEnergy[r + rDelta[i]][c + cDelta[i]] = sharkEnergy[r][c]++;
          sharkEnergy[r][c] = 0;
          simulationGrid.updateCell(r + rDelta[i], c + cDelta[i], shark);
          simulationGrid.updateCell(r, c, empty);
          fisheaten=true;
          if (chronon % 5 == 0) {
            sharkEnergy[r][c] = shark_lives;
            simulationGrid.updateCell(r,c, shark);
          }
          break;
        }
      }
    }
    // If the shark did not move to eat the fish, and a nearby location is empty, move
    if (!fisheaten) {
      for (int i = 0; i < neighbors.length && i < 4; i++) {
        if (neighbors[i] == empty) {
          if (simulationGrid.inBounds(r + rDelta[i], c + cDelta[i])) {
            sharkEnergy[r + rDelta[i]][c + cDelta[i]] = sharkEnergy[r][c];
            sharkEnergy[r][c] = 0;
            simulationGrid.updateCell(r + rDelta[i], c + cDelta[i], shark);
            simulationGrid.updateCell(r, c, empty);
            if (chronon % 5 == 0) {
              sharkEnergy[r][c] = shark_lives;
              simulationGrid.updateCell(r,c, shark);
            }
            break;
          }
        }
      }
    }
  }

  private void fishGoesTo(int r, int c) {
    int[] neighbors = simulationGrid.checkNeighbors(r, c, false, false);

    for (int i = 0; i < neighbors.length && i < 4; i++) {
      if (neighbors[i] == empty) {
        if (simulationGrid.inBounds(r + rDelta[i], c + cDelta[i])) {
          simulationGrid.updateCell(r + rDelta[i], c + cDelta[i], fish);
          if (chronon % 5 != 0) { // Put fish in new spot
            simulationGrid.updateCell(r, c, empty);
          }
          break;
        }
      }
    }

  }

  @Override
  public int getSimulationCols() {
    return GRID_WIDTH;
  }

  @Override
  protected void initializeColorMap() {
    cellColorMap = new HashMap<>();
    cellColorMap.put(0, Color.BLACK);
    cellColorMap.put(1, Color.GREEN);
    cellColorMap.put(2, Color.BLUE);
  }

  @Override
  public Map<Integer, Color> getCellColorMap() {
    return cellColorMap;
  }

}

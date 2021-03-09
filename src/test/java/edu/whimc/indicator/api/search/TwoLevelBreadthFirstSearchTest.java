package edu.whimc.indicator.api.search;

import com.google.common.collect.Lists;
import edu.whimc.indicator.util.Printable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.junit.jupiter.api.Test;

import java.util.*;

class TwoLevelBreadthFirstSearchTest {

  Domain domain1 = new Domain("d1");
  Domain domain2 = new Domain("d2");

  static int boardSize = 10;
  static Cell[][] board1 = new Cell[boardSize][boardSize];
  static Cell[][] board2 = new Cell[boardSize][boardSize];


  @Test
  void findPath() {

    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        board1[i][j] = new Cell(i, j, domain1);
        board2[i][j] = new Cell(i, j, domain2);
      }
    }

    for (int i = 0; i < boardSize - 1; i++) {
      board1[2][i] = null;
      board1[5][i+1] = null;
      board1[8][i] = null;
      board2[i][2] = null;
      board2[i+1][5] = null;
      board2[i][8] = null;

//      board1[0][1] = null;
//      board1[1][0] = null;
//      board1[1][1] = null;
    }

    TwoLevelBreadthFirstSearch<Cell, Domain> bfs = new TwoLevelBreadthFirstSearch<>();
    bfs.registerMode(new StepMode());
    List<Link<Cell, Domain>> links = Lists.newArrayList(new Link<>(board1[boardSize - 1][0], board2[0][boardSize - 1]));
    links.forEach(bfs::registerLink);

    Cell origin = new Cell(0, 0, domain1);
    Cell destination = new Cell(0, 0, domain2);
    List<Cell> path = bfs.findPath(origin, destination);

    // Print out our answers
    char[][] printer1 = new char[boardSize][boardSize];
    char[][] printer2 = new char[boardSize][boardSize];

    // Clear board
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        if (board1[i][j] == null) {
          printer1[i][j] = '#';
        } else {
          printer1[i][j] = '.';
        }
        if (board2[i][j] == null) {
          printer2[i][j] = '#';
        } else {
          printer2[i][j] = '.';
        }
      }
    }

    // Put in path
    if (path != null) {
      for (Cell cell : path) {
        if (cell.domain.equals(domain1)) {
          printer1[cell.x][cell.y] = 'O';
        }
        if (cell.domain.equals(domain2)) {
          printer2[cell.x][cell.y] = 'O';
        }
      }
    }

    // Put in origin and destination
    printer1[origin.x][origin.y] = 'A';
    printer2[destination.x][destination.y] = 'B';

    // Put in links
    for (int i = 0; i < links.size(); i++) {
      if (links.get(i).getOrigin().getDomain().equals(domain1)) {
        printer1[links.get(i).getOrigin().x][links.get(i).getOrigin().y] = Character.forDigit(i, 10);
      }
      if (links.get(i).getOrigin().getDomain().equals(domain2)) {
        printer2[links.get(i).getOrigin().x][links.get(i).getOrigin().y] = Character.forDigit(i, 10);
      }
      if (links.get(i).getDestination().getDomain().equals(domain1)) {
        printer1[links.get(i).getDestination().x][links.get(i).getDestination().y] = Character.forDigit(i, 10);
      }
      if (links.get(i).getDestination().getDomain().equals(domain2)) {
        printer2[links.get(i).getDestination().x][links.get(i).getDestination().y] = Character.forDigit(i, 10);
      }
    }

    // Print board1
    System.out.println("Board 1:");
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        System.out.print(printer1[i][j]);
      }
      System.out.print('\n');
    }

    // Print board2
    System.out.println("Board 2:");
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        System.out.print(printer2[i][j]);
      }
      System.out.print('\n');
    }
  }

  public static class Cell implements Locatable<Cell, Domain>, Printable {

    private final int x;
    private final int y;
    private final Domain domain;

    Cell(int x, int y, Domain domain) {
      this.x = x;
      this.y = y;
      this.domain = domain;
    }

    @Override
    public int distanceTo(Cell other) {
      return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    @Override
    public Domain getDomain() {
      return this.domain;
    }

    @Override
    public void print() {
      System.out.printf("(%d, %d, %s)", x, y, domain.getName());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Cell cell = (Cell) o;
      return x == cell.x && y == cell.y && Objects.equals(domain, cell.domain);
    }

    @Override
    public int hashCode() {
      return Objects.hash(x, y, domain);
    }
  }

  @AllArgsConstructor
  public static class Domain {
    @NonNull @Getter
    private final String name;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Domain domain = (Domain) o;
      return name.equals(domain.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name);
    }
  }

  public class StepMode implements Mode<Cell, Domain> {

    @Override
    public Set<Cell> getDestinations(Cell origin) {
      Set<Cell> set = new HashSet<>();
      for (int i = -1; i <= 1; i++) {
        for (int j = -1; j <= 1; j++) {
          if (i == 0 && j == 0) continue;
          if (origin.x + i < 0) continue;
          if (origin.x + i >= boardSize) continue;
          if (origin.y + j < 0) continue;
          if (origin.y + j >= boardSize) continue;
          Cell adding = null;
          if (origin.getDomain().equals(domain1)) {
            adding = board1[origin.x + i][origin.y + j];
          }
          if (origin.getDomain().equals(domain2)) {
            adding = board2[origin.x + i][origin.y + j];
          }
          if (adding != null) {
            set.add(adding);
          }
        }
      }
      return set;
    }
  }
}
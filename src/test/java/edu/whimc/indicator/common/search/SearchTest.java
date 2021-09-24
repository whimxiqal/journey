package edu.whimc.indicator.common.search;

import com.google.common.collect.Lists;
import edu.whimc.indicator.common.cache.TrailCache;
import edu.whimc.indicator.common.navigation.*;
import edu.whimc.indicator.common.navigation.ModeType;
import edu.whimc.indicator.common.search.tracker.BlankSearchTracker;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.junit.jupiter.api.Test;

import java.util.*;

class SearchTest {

  Domain domain1 = new Domain("d1");
  Domain domain2 = new Domain("d2");

  static int boardSize = 12;
  static Point3D[][] board1 = new Point3D[boardSize][boardSize];
  static Point3D[][] board2 = new Point3D[boardSize][boardSize];


  @Test
  void findPath() {

    // Initialize domains to be complete.getY() free
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        board1[i][j] = new Point3D(i, j, domain1);
        board2[i][j] = new Point3D(i, j, domain2);
      }
    }

    // Add barriers
    board1[2][3] = null;
    board1[3][4] = null;
    board1[4][5] = null;
    board1[5][5] = null;
    board1[6][5] = null;
    board1[7][5] = null;
    board1[8][5] = null;
    board1[9][4] = null;
    board1[10][3] = null;
    board1[9][2] = null;
    board1[8][1] = null;
    board1[7][1] = null;
    board1[6][1] = null;
    board1[5][1] = null;
    board1[4][1] = null;
    board1[3][1] = null;
    board1[2][2] = null;

    board2[3][2] = null;
    board2[4][3] = null;
    board2[5][4] = null;
    board2[5][5] = null;
    board2[5][6] = null;
    board2[5][7] = null;
    board2[5][8] = null;
    board2[4][9] = null;
    board2[3][10] = null;

    // Printers for our answers
    char[][] printer1 = new char[boardSize][boardSize];
    char[][] printer2 = new char[boardSize][boardSize];

    // Set up parameters for search
    TrailCache<Point3D, Domain> trailCache = new TrailCache<>();
    Search<Point3D, Domain> bfs = new Search<>(trailCache);
    Point3D origin = board1[4][4];
    Point3D destination = board1[4][8];
    List<Link<Point3D, Domain>> links = Lists.newLinkedList();
    links.add(new TestLink(board1[8][4], board2[3][6]));
    links.add(new TestLink(board2[7][7], board1[8][8]));
    links.forEach(bfs::registerLink);
    bfs.setTracker(new TestSearchTracker(printer1, printer2, links));

    // Clear printer board
    clearPrinters(board1, board2, printer1, printer2, origin, destination, links, boardSize);

    bfs.registerMode(new StepMode());

    // Solve path
    bfs.search(origin, destination);

    // Put in path
//    if (path != null) {
//      path.getAllSteps().stream().map(Step::getLocatable).forEach(cell -> {
//        if (cell.domain.equals(domain1)) {
//          printer1[cell.getX()][cell.getY()] = 'O';
//        }
//        if (cell.domain.equals(domain2)) {
//          printer2[cell.getX()][cell.getY()] = 'O';
//        }
//      });
//    }

    // Print boards
    System.out.println("Board 1:");
    printPrinter(printer1, boardSize);
    System.out.println("Board 2:");
    printPrinter(printer2, boardSize);
  }

  private void clearPrinters(Point3D[][] board1, Point3D[][] board2,
                             char[][] printer1, char[][] printer2,
                             Point3D origin, Point3D destination,
                             List<Link<Point3D, Domain>> links,
                             int boardSize) {
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        if (board1[i][j] == null) {
          printer1[i][j] = '#';
        } else {
          printer1[i][j] = ' ';
        }
        if (board2[i][j] == null) {
          printer2[i][j] = '#';
        } else {
          printer2[i][j] = ' ';
        }
      }
    }

    // Put in origin and destination
    if (origin.getDomain().equals(domain1)) {
      printer1[origin.getX()][origin.getY()] = 'A';
    } else {
      printer2[origin.getX()][origin.getY()] = 'A';
    }
    if (origin.getDomain().equals(domain1)) {
      printer1[destination.getX()][destination.getY()] = 'B';
    } else {
      printer2[destination.getX()][destination.getY()] = 'B';
    }

    // Put in links
    for (int i = 0; i < links.size(); i++) {
      if (links.get(i).getOrigin().getDomain().equals(domain1)) {
        printer1[links.get(i).getOrigin().getX()][links.get(i).getOrigin().getY()] = Character.forDigit(i, 10);
      }
      if (links.get(i).getOrigin().getDomain().equals(domain2)) {
        printer2[links.get(i).getOrigin().getX()][links.get(i).getOrigin().getY()] = Character.forDigit(i, 10);
      }
      if (links.get(i).getDestination().getDomain().equals(domain1)) {
        printer1[links.get(i).getDestination().getX()][links.get(i).getDestination().getY()] = Character.forDigit(i, 10);
      }
      if (links.get(i).getDestination().getDomain().equals(domain2)) {
        printer2[links.get(i).getDestination().getX()][links.get(i).getDestination().getY()] = Character.forDigit(i, 10);
      }
    }
  }

  private void printPrinter(char[][] printer, int boardSize) {
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        System.out.print(printer[i][j]);
      }
      System.out.print('\n');
    }
  }

  public static class Point3D extends Cell<Point3D, Domain> {

    private final Domain domain;

    public Point3D(int x, int y, Domain domain) {
      super(x, y, 0, domain.getName(), name -> domain);
      this.domain = domain;
    }

    @Override
    public double distanceToSquared(Point3D other) {
      return this.getX() * other.getX() + this.getY() * other.getY() + this.z * other.z;
    }

    public String print() {
      return String.format("(%d, %d, %d, %s)", x, y, z, domain.getName());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Point3D that = (Point3D) o;
      return this.x == that.x && this.y == that.y && this.z == that.z && this.getDomain().equals(that.getDomain());
    }

    @Override
    public int hashCode() {
      return Objects.hash(x, y, domain);
    }
  }

  public static class TestLink implements Link<Point3D, Domain> {

    private final Point3D origin;
    private final Point3D destination;

    public TestLink(Point3D origin, Point3D destination) {
      this.origin = origin;
      this.destination = destination;
    }

    @Override
    public Point3D getOrigin() {
      return origin;
    }

    @Override
    public Point3D getDestination() {
      return destination;
    }

    @Override
    public Completion<Point3D, Domain> getCompletion() {
      return loc -> loc.equals(destination);
    }

    @Override
    public double weight() {
      return 0;
    }

    @Override
    public boolean verify() {
      return true;
    }
  }

  @AllArgsConstructor
  public static class Domain {
    @NonNull
    @Getter
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

  public class StepMode extends Mode<Point3D, Domain> {

    @Override
    public void collectDestinations(Point3D origin) {
      for (int i = -1; i <= 1; i++) {
        for (int j = -1; j <= 1; j++) {
          if (i == 0 && j == 0) continue;
          if (origin.getX() + i < 0) continue;
          if (origin.getX() + i >= boardSize) continue;
          if (origin.getY() + j < 0) continue;
          if (origin.getY() + j >= boardSize) continue;
          Point3D adding = null;
          if (origin.getDomain().equals(domain1)) {
            // Make sure we can't go diagonal.getY() if adjacent borders won't allow such a move
            if (i * i * j * j == 1 && board1[origin.getX() + i][origin.getY()] == null && board1[origin.getX()][origin.getY() + j] == null)
              continue;
            adding = board1[origin.getX() + i][origin.getY() + j];
          }
          if (origin.getDomain().equals(domain2)) {
            // Make sure we can't go diagonal.getY() if adjacent borders won't allow such a move
            if (i * i * j * j == 1 && board2[origin.getX() + i][origin.getY()] == null && board2[origin.getX()][origin.getY() + j] == null)
              continue;
            adding = board2[origin.getX() + i][origin.getY() + j];
          }
          if (adding != null) {
            accept(adding, origin.distanceTo(adding));
          }
        }
      }
    }

    @Override
    public ModeType getType() {
      return ModeType.WALK;
    }
  }

  @AllArgsConstructor
  public class TestSearchTracker extends BlankSearchTracker<Point3D, Domain> {

    private final char[][] printer1;
    private final char[][] printer2;
    private final List<Link<Point3D, Domain>> links;

    @Override
    public void trailSearchVisitation(Step<Point3D, Domain> step) {
      Point3D cell = step.getLocatable();
      System.out.printf("Distance to destination: %f\n", cell.distanceTo(links.get(1).getOrigin()));
      if (cell.getDomain().equals(domain1)) {
        printer1[cell.getX()][cell.getY()] = '+';
      }
      if (cell.getDomain().equals(domain2)) {
        printer2[cell.getX()][cell.getY()] = '+';
      }
      // Print boards
      System.out.println("Board 1:");
      printPrinter(printer1, boardSize);
      System.out.println("Board 2:");
      printPrinter(printer2, boardSize);
    }

    @Override
    public void trailSearchStep(Step<Point3D, Domain> step) {
      Point3D cell = step.getLocatable();
      if (cell.getDomain().equals(domain1)) {
        printer1[cell.getX()][cell.getY()] = '.';
      }
      if (cell.getDomain().equals(domain2)) {
        printer2[cell.getX()][cell.getY()] = '.';
      }
      // Print boards
      System.out.println("Board 1:");
      printPrinter(printer1, boardSize);
      System.out.println("Board 2:");
      printPrinter(printer2, boardSize);
    }

    @Override
    public void completeTrailSearch(Point3D origin, Point3D destination, double distance) {
      clearPrinters(board1, board2, printer1, printer2, origin, destination, links, boardSize);
    }

    @Override
    public void memoryCapacityReached(Point3D origin, Point3D destination) {

    }
  }
}
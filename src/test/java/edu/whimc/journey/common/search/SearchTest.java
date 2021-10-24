package edu.whimc.journey.common.search;

import com.google.common.collect.Lists;
import edu.whimc.journey.common.JourneyCommon;
import edu.whimc.journey.common.cache.PathCache;
import edu.whimc.journey.common.navigation.Cell;
import edu.whimc.journey.common.navigation.Itinerary;
import edu.whimc.journey.common.navigation.Port;
import edu.whimc.journey.common.navigation.Mode;
import edu.whimc.journey.common.navigation.ModeType;
import edu.whimc.journey.common.search.event.FoundSolutionEvent;
import edu.whimc.journey.common.search.event.SearchDispatcher;
import edu.whimc.journey.common.search.event.SearchEvent;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SearchTest {

  static boolean print = true;

  static int boardSize = 12;
  static Point3D[][] board1 = new Point3D[boardSize][boardSize];
  static Point3D[][] board2 = new Point3D[boardSize][boardSize];
  Domain domain1 = new Domain("d1");
  Domain domain2 = new Domain("d2");

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

    // Set up JourneyCommon
    SearchDispatcher<Point3D, Domain, Runnable> dispatcher = new SearchDispatcher<>(Runnable::run);
    JourneyCommon.setSearchEventDispatcher(dispatcher);
    JourneyCommon.setPathCache(new PathCache<Point3D, Domain>());

    // Prepare variable to store if a solution has been found during the search
    AtomicBoolean solved = new AtomicBoolean(false);
    AtomicReference<Itinerary<Point3D, Domain>> solution = new AtomicReference<>();

    // Set up listeners for search event
    dispatcher.<FoundSolutionEvent<Point3D, Domain>>registerEvent(event -> () -> {
      solved.set(true);
      solution.set(event.getItinerary());
    }, SearchEvent.EventType.FOUND_SOLUTION);

    // Set up parameters for search
    ReverseSearchSession<Point3D, Domain> session = new TestSearchSession(UUID.randomUUID(),
        SearchSession.Caller.OTHER);
    List<Port<Point3D, Domain>> links = Lists.newLinkedList();
    links.add(new TestLink(board1[8][4], board2[3][6]));
    links.add(new TestLink(board2[7][7], board1[8][8]));
    links.forEach(session::registerLeap);

    Point3D origin = board1[4][4];
    Point3D destination = board1[4][8];

    // Printers for our answers
    char[][] printer1 = new char[boardSize][boardSize];
    char[][] printer2 = new char[boardSize][boardSize];

    // Clear printer board
    clearPrinters(board1, board2, printer1, printer2, origin, destination, links, boardSize);

    session.registerMode(new StepMode());

    // Solve path
    Thread thread = new Thread(() -> {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      session.cancel();
    });
    thread.start();
    session.search(origin, destination);

    Assertions.assertTrue(solved.get());

    if (print) {
      // Put in path
      solution.get().getSteps().forEach(step -> {
        char[][] printer;
        if (step.getLocatable().getDomain().equals(domain1)) {
          printer = printer1;
        } else {
          printer = printer2;
        }

        if (printer[step.getLocatable().getX()][step.getLocatable().getY()] == ' ') {
          printer[step.getLocatable().getX()][step.getLocatable().getY()] = '+';
        }
      });

      // Print boards
      System.out.println("Board 1:");
      printPrinter(printer1, boardSize);
      System.out.println("Board 2:");
      printPrinter(printer2, boardSize);
    }

  }

  private void clearPrinters(Point3D[][] board1, Point3D[][] board2,
                             char[][] printer1, char[][] printer2,
                             Point3D origin, Point3D destination,
                             List<Port<Point3D, Domain>> links,
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
        printer1
            [links.get(i).getOrigin().getX()]
            [links.get(i).getOrigin().getY()] = Character.forDigit(i, 10);
      }
      if (links.get(i).getOrigin().getDomain().equals(domain2)) {
        printer2
            [links.get(i).getOrigin().getX()]
            [links.get(i).getOrigin().getY()] = Character.forDigit(i, 10);
      }
      if (links.get(i).getDestination().getDomain().equals(domain1)) {
        printer1
            [links.get(i).getDestination().getX()]
            [links.get(i).getDestination().getY()] = Character.forDigit(i, 10);
      }
      if (links.get(i).getDestination().getDomain().equals(domain2)) {
        printer2
            [links.get(i).getDestination().getX()]
            [links.get(i).getDestination().getY()] = Character.forDigit(i, 10);
      }
    }
  }

  private void printPrinter(char[][] printer, int boardSize) {
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        System.out.print(printer[j][i]);
      }
      System.out.print('\n');
    }
  }

  static class TestSearchSession extends ReverseSearchSession<Point3D, Domain> {

    public TestSearchSession(UUID callerId, Caller callerType) {
      super(callerId, callerType);
    }

  }

  public static class Point3D extends Cell<Point3D, Domain> {

    private final Domain domain;

    public Point3D(int x, int y, Domain domain) {
      super(x, y, 0, domain.name(), name -> domain);
      this.domain = domain;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Point3D that = (Point3D) o;
      return this.coordinateX == that.coordinateX
          && this.coordinateY == that.coordinateY
          && this.coordinateZ == that.coordinateZ && this.getDomain().equals(that.getDomain());
    }

    @Override
    public int hashCode() {
      return Objects.hash(coordinateX, coordinateY, domain);
    }

    @Override
    public String toString() {
      return String.format("(%d, %d, %d, %s)", coordinateX, coordinateY, coordinateZ, domain.name());
    }

    @Override
    public double distanceToSquared(Point3D other) {
      return (getX() - other.getX()) * (getX() - other.getX())
          + (getY() - other.getY()) * (getY() - other.getY())
          + (getZ() - other.getZ()) * (getZ() - other.getZ());
    }
  }

  public static class TestLink extends Port<Point3D, Domain> {

    public TestLink(Point3D origin, Point3D destination) {
      super(origin, destination, ModeType.LEAP, 1);
    }

    @Override
    public String toString() {
      return String.format("Port: {Origin: %s, Destination: %s}", getOrigin(), getDestination());
    }
  }

  public record Domain(@NonNull @Getter String name) { }

  public class StepMode extends Mode<Point3D, Domain> {

    @Override
    public void collectDestinations(Point3D origin) {
      for (int i = -1; i <= 1; i++) {
        for (int j = -1; j <= 1; j++) {
          if (i == 0 && j == 0) {
            continue;
          }
          if (origin.getX() + i < 0) {
            continue;
          }
          if (origin.getX() + i >= boardSize) {
            continue;
          }
          if (origin.getY() + j < 0) {
            continue;
          }
          if (origin.getY() + j >= boardSize) {
            continue;
          }
          Point3D adding = null;
          if (origin.getDomain().equals(domain1)) {
            // Make sure we can't go diagonal.getY() if adjacent borders won't allow such a move
            if (i * i * j * j == 1
                && board1[origin.getX() + i][origin.getY()] == null
                && board1[origin.getX()][origin.getY() + j] == null) {
              continue;
            }
            adding = board1[origin.getX() + i][origin.getY() + j];
          }
          if (origin.getDomain().equals(domain2)) {
            // Make sure we can't go diagonal.getY() if adjacent borders won't allow such a move
            if (i * i * j * j == 1
                && board2[origin.getX() + i][origin.getY()] == null
                && board2[origin.getX()][origin.getY() + j] == null) {
              continue;
            }
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

}
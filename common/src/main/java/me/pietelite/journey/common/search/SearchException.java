package me.pietelite.journey.common.search;

public class SearchException extends RuntimeException {

  public SearchException() {
    super();
  }

  public SearchException(String message) {
    super(message);
  }

  public SearchException(Throwable throwable) {
    super(throwable);
  }

}

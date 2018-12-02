package parsertests;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestRunner {
  static public void main(String args[]) {
    Result result = JUnitCore.runClasses(TestSerializer.class);
    for (Failure fail : result.getFailures()) {
      System.out.println("------------");
      System.out.println(fail.getDescription());
      System.out.println(fail.getMessage());
      System.out.println("------------");
    }
    System.out.println("War successful: " + result.wasSuccessful());
  }
}

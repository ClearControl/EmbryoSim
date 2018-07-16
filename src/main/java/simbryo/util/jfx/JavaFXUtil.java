package simbryo.util.jfx;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;

import com.sun.javafx.application.PlatformImpl;

/**
 * Utility classes for JavaFX
 *
 * @author royer
 */
public class JavaFXUtil
{
  /**
   * Initializes JavaFX
   */
  public static void init()
  {
    PlatformImpl.startup(() -> {
    });
  }

  private static class Reference
  {
    public Object mReference;
  }

  /**
   * Runs the given code on the JavaFX thread and wait for the task to complete.
   * 
   * @param pCallable
   *          code to run on the JavaFX thread.
   * @return result;
   */
  @SuppressWarnings("unchecked")
  public static <T> T runAndWait(Callable<T> pCallable)
  {
    init();

    CountDownLatch lCountDownLatch = new CountDownLatch(1);

    Reference lResult = new Reference();

    Platform.runLater(() -> {
      try
      {
        lResult.mReference = pCallable.call();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      lCountDownLatch.countDown();
    });

    try
    {
      lCountDownLatch.await();
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }

    return (T) lResult.mReference;
  }
}

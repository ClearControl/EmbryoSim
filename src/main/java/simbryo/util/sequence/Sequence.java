package simbryo.util.sequence;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Handles execution of tasks in a linear timeline.
 *
 * @author royer
 */
public class Sequence implements Serializable
{
  private static final long serialVersionUID = 1L;

  private volatile double mTime = 0;

  private HashSet<Double> mEventTriggeredSet = new HashSet<>();

  /**
   * increments by a single step.
   * 
   * @param pDeltaTime
   *          step delta time
   */
  public void step(float pDeltaTime)
  {
    setTime(getTime() + pDeltaTime);
  }

  /**
   * Runs runnable if time within given interval [begin,end[
   * 
   * @param pBegin
   *          beginning of interval
   * @param pEnd
   *          end of interval
   * @param pRunnable
   *          runnable
   */
  public void run(double pBegin, double pEnd, Runnable pRunnable)
  {
    if (getTime() >= pBegin && getTime() < pEnd)
      pRunnable.run();
  }

  /**
   * Runs runnable if we are past the event time. Runnable is run only once.
   * 
   * @param pEventTime
   *          event time
   * @param pRunnable
   *          runnable to execute once at event.
   */
  public void run(double pEventTime, Runnable pRunnable)
  {
    if (getTime() >= pEventTime
        && !mEventTriggeredSet.contains(pEventTime))
    {
      pRunnable.run();
      mEventTriggeredSet.add(pEventTime);
    }

  }

  /**
   * Returns the time
   * 
   * @return time
   */
  public double getTime()
  {
    return mTime;
  }

  /**
   * Sets the time
   * 
   * @param pTime
   *          time
   */
  public void setTime(double pTime)
  {
    mTime = pTime;
  }

}

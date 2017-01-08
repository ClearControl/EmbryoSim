package simbryo.particles.viewer.two;

import java.util.concurrent.CountDownLatch;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import simbryo.particles.ParticleSystem;

public class ParticleViewerCanvas extends Canvas
{

  private CountDownLatch mCountDownLatch;
  private volatile boolean mDisplayGrid, mDisplayElapsedTime,
      mDisplayRadius = true;

  private volatile long mLastUpdateTimeInNanos = System.nanoTime();
  private volatile float mElapsedTime;
  private float[] mPositions;
  private float[] mVelocities;
  private float[] mRadiis;

  public ParticleViewerCanvas()
  {
    this(512, 512);
  }

  public ParticleViewerCanvas(double pWidth, double pHeight)
  {
    super(pWidth, pHeight);
  }

  public void updateDisplay(ParticleSystem pParticleSystem,
                            boolean pBlocking)
  {
    long lNow = System.nanoTime();
    float lNewElapsedTime =
                          (float) (1e-6
                                   * (lNow - mLastUpdateTimeInNanos));

    mLastUpdateTimeInNanos = lNow;

    mElapsedTime = (float) (0.99 * mElapsedTime
                            + 0.01 * lNewElapsedTime);

    if (pBlocking)
    {
      try
      {
        if (mCountDownLatch != null)
          mCountDownLatch.await();
      }
      catch (InterruptedException e)
      {
      }
    }
    mCountDownLatch = new CountDownLatch(1);

    final int lDimension = pParticleSystem.getDimension();
    final int lNumberOfParticles =
                                 pParticleSystem.getNumberOfParticles();

    if (mPositions == null
        || mPositions.length != lNumberOfParticles * lDimension)
    {
      mPositions = new float[lNumberOfParticles * lDimension];
      mVelocities = new float[lNumberOfParticles * lDimension];
      mRadiis = new float[lNumberOfParticles];
    }

    pParticleSystem.copyPositions(mPositions);
    pParticleSystem.copyVelocities(mVelocities);
    pParticleSystem.copyRadii(mRadiis);

    Platform.runLater(() -> {

      GraphicsContext gc = getGraphicsContext2D();

      final double lWidth = getWidth();
      final double lHeight = getHeight();
      final double lMinWidthHeight = getHeight();

      gc.setFill(Color.BLACK);
      gc.fillRect(0, 0, lWidth, lHeight);

      for (int id =
                  0, i =
                       0; id < lNumberOfParticles; id++, i +=
                                                           lDimension)
      {
        float x = mPositions[i + 0];
        float y = mPositions[i + 1];
        float r = mRadiis[id];
        float vx = mVelocities[i + 0];
        float vy = mVelocities[i + 1];
        float v = vx * vx + vy * vy;

        double lScreenX = lWidth * (x - r);
        double lScreenY = lHeight * (y - r);
        double lRadius = mDisplayRadius ? lMinWidthHeight * r
                                        : lMinWidthHeight * 0.005f;

        double lBrightness = 0.7 + 1000000 * v;
        // System.out.println(lBrightness);

        double lSaturation = 0.5 - 1000000 * v;

        lBrightness = lBrightness > 1 ? 1 : lBrightness;
        lSaturation =
                    lSaturation > 1 ? 1
                                    : (lSaturation < 0 ? 0
                                                       : lSaturation);

        /*if (id == 0)
          gc.setFill(Color.WHITE);
        else/**/
        gc.setFill(Color.hsb((223 * id) % 359,
                             lSaturation,
                             lBrightness));

        gc.fillOval(lScreenX, lScreenY, 2 * lRadius, 2 * lRadius);
        // gc.fillText(""+id, lScreenX, lScreenY);

      }

      if (isDisplayGrid())
      {
        int[] lGridSize = pParticleSystem.getGridDimensions();

        final int lGridWidth = lGridSize[0];
        final int lGridHeight = lGridSize[1];

        for (int x = 0; x < lGridWidth; x++)
        {
          final double sx = (lWidth * x) / lGridWidth;
          gc.strokeLine(sx, 0, sx, lHeight);
        }

        for (int y = 0; y < lGridHeight; y++)
        {
          final double sy = (lHeight * y) / lGridHeight;
          gc.strokeLine(0, sy, lWidth, sy);
        }
      }
      /**/

      if (isDisplayElapsedTime())
      {
        gc.setFill(Color.WHITE);
        String lFrameRateText =
                              String.format("%.3f ms", mElapsedTime);
        gc.fillText(lFrameRateText, 10, 20);
      }

      gc.beginPath();
      gc.rect(0, 0, lWidth, lHeight);
      gc.setStroke(Color.RED);
      gc.stroke();/**/
      gc.closePath();

      mCountDownLatch.countDown();

    });

  }

  public boolean isDisplayGrid()
  {
    return mDisplayGrid;
  }

  public void setDisplayGrid(boolean pDisplayGrid)
  {
    mDisplayGrid = pDisplayGrid;
  }

  public boolean isDisplayElapsedTime()
  {
    return mDisplayElapsedTime;
  }

  public void setDisplayElapsedTime(boolean pDisplayElapsedTime)
  {
    mDisplayElapsedTime = pDisplayElapsedTime;
  }

  public void setDisplayRadius(boolean pDisplayRadius)
  {
    mDisplayRadius = pDisplayRadius;

  }

}
package simbryo.synthoscopy.phantom;

import java.util.Arrays;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.enums.ImageChannelDataType;
import simbryo.dynamics.tissue.TissueDynamicsInterface;
import simbryo.particles.neighborhood.NeighborhoodGrid;
import simbryo.synthoscopy.ClearCLSynthoscopyBase;

/**
 * Base class providing common fields and methods for all classes implementing
 * the phantom renderer interface
 * 
 * @author royer
 */
public abstract class PhantomRendererBase extends
                                          ClearCLSynthoscopyBase
                                          implements
                                          PhantomRendererInterface<ClearCLImage>
{
  private final TissueDynamicsInterface mTissue;
  protected boolean[] mPlaneAlreadyDrawnTable;

  private volatile float mSignalIntensity = 1,
      mNoiseOverSignalRatio = 0f;
  private volatile int mStartZ, mEndZ;

  protected ClearCLKernel mRenderKernel;
  private final long mLocalSizeX, mLocalSizeY, mLocalSizeZ;

  protected NeighborhoodGrid mNeighborhoodGrid;

  /**
   * Instantiates a Phantom renderer for a given OpenCL device, tissue dynamics,
   * and stack dimensions.
   * 
   * @param pContext
   *          OpenCL context to use.
   * @param pMaxParticlesPerGridCell
   *          max number of articles per cell
   * @param pTissueDynamics
   *          tissue dynamics object
   * @param pStackDimensions
   *          stack dimensions
   */
  public PhantomRendererBase(final ClearCLContext pContext,
                             int pMaxParticlesPerGridCell,
                             final TissueDynamicsInterface pTissueDynamics,
                             final long... pStackDimensions)
  {
    super(pContext,
          true,
          ImageChannelDataType.Float,
          pStackDimensions);
    mTissue = pTissueDynamics;
    mPlaneAlreadyDrawnTable =
                            new boolean[Math.toIntExact(getDepth())];

    mContext = pContext;

    int[] lGridDimensions =
                          PhantomRendererUtils.getOptimalGridDimensions(pContext.getDevice(),
                                                                        pStackDimensions);

    mNeighborhoodGrid = new NeighborhoodGrid(pMaxParticlesPerGridCell,
                                             lGridDimensions);

    mLocalSizeX = getWidth() / lGridDimensions[0];
    mLocalSizeY = getHeight() / lGridDimensions[1];
    mLocalSizeZ = getDepth() / lGridDimensions[2];

    setBeginZ(0);
    setEndZ((int) (getDepth() - 1));

    clear(false);
  }

  @Override
  public TissueDynamicsInterface getTissue()
  {
    return mTissue;
  }

  @Override
  public void clear(boolean pWaitToFinish)
  {
    super.clear(pWaitToFinish);
    invalidateAll();
  }

  @Override
  public void requestUpdate()
  {
    super.requestUpdate();
    invalidateAll();
  }

  protected void invalidateAll()
  {
    Arrays.fill(mPlaneAlreadyDrawnTable, false);
  }

  protected void invalidate(int pZ)
  {
    mPlaneAlreadyDrawnTable[pZ] = false;
  }

  protected boolean isValid(int pZPlaneIndexBegin,
                            int pZPlaneIndexEnd)
  {
    for (int i = pZPlaneIndexBegin; i < pZPlaneIndexEnd; i++)
      if (!mPlaneAlreadyDrawnTable[i])
        return false;
    return true;
  }

  @Override
  public void render(boolean pWaitToFinish)
  {
    render(getBeginZ(), getEndZ(), pWaitToFinish);
  }

  @Override
  public void render(int pZPlaneIndexBegin,
                     int pZPlaneIndexEnd,
                     boolean pWaitToFinish)
  {
    renderAndCount(pZPlaneIndexBegin, pZPlaneIndexEnd, pWaitToFinish);
  }

  /**
   * Renders in a cache-aware manner
   * 
   * @param pZPlaneIndexBegin
   *          begin z index
   * @param pZPlaneIndexEnd
   *          end z index
   * @param pWaitToFinish
   *          true -> wait to finish
   * @return number of planes rendered
   */
  public int renderAndCount(int pZPlaneIndexBegin,
                            int pZPlaneIndexEnd,
                            boolean pWaitToFinish)
  {
    if (!isUpdateNeeded()
        && isValid(pZPlaneIndexBegin, pZPlaneIndexEnd))
      return 0;

    int lCounter = 0;

    int zi = pZPlaneIndexBegin;

    while (zi < pZPlaneIndexEnd)
    {
      while (zi < pZPlaneIndexEnd && mPlaneAlreadyDrawnTable[zi])
        zi++;

      int zj = zi;
      while (zj < pZPlaneIndexEnd && !mPlaneAlreadyDrawnTable[zj])
        zj++;
      renderInternal(zi, zj, false);

      for (int zk = zi; zk < zj; zk++)
      {
        mPlaneAlreadyDrawnTable[zk] = true;
        lCounter++;
      }
      zi = zj;
    }

    if (pWaitToFinish)
      mContext.getDefaultQueue().waitToFinish();

    super.render(pWaitToFinish);

    return lCounter;
  }

  protected void renderInternal(int pZPlaneIndexBegin,
                                int pZPlaneIndexEnd,
                                boolean pWaitToFinish)
  {

    // First we snap the rendering z bounds to the grid cell boundaries:
    pZPlaneIndexBegin =
                      (int) (Math.floor((float) pZPlaneIndexBegin
                                        / mLocalSizeZ)
                             * mLocalSizeZ);
    pZPlaneIndexEnd = (int) (Math.ceil((float) pZPlaneIndexEnd
                                       / mLocalSizeZ)
                             * mLocalSizeZ);

    // Now we can render a possibly slightly larger chunck of the stack:

    if (pZPlaneIndexEnd == pZPlaneIndexBegin)
      pZPlaneIndexEnd += mLocalSizeZ;

    // System.out.println("pZPlaneIndexBegin=" + pZPlaneIndexBegin);
    // System.out.println("pZPlaneIndexEnd =" + pZPlaneIndexEnd);
    mRenderKernel.setGlobalOffsets(0, 0, pZPlaneIndexBegin);
    mRenderKernel.setGlobalSizes(getWidth(),
                                 getHeight(),
                                 pZPlaneIndexEnd - pZPlaneIndexBegin);
    mRenderKernel.setLocalSizes(mLocalSizeX,
                                mLocalSizeY,
                                mLocalSizeZ);
    mRenderKernel.setOptionalArgument("intensity",
                                      getSignalIntensity());
    mRenderKernel.setOptionalArgument("timeindex",
                                      (int) getTissue().getTimeStepIndex());

    mRenderKernel.run(pWaitToFinish);
    for (int z = pZPlaneIndexBegin; z < pZPlaneIndexEnd; z++)
      mPlaneAlreadyDrawnTable[z] = true;

  }

  @Override
  public float getSignalIntensity()
  {
    return mSignalIntensity;
  }

  @Override
  public void setSignalIntensity(float pSignalIntensity)
  {
    if (mSignalIntensity != pSignalIntensity)
    {
      mSignalIntensity = pSignalIntensity;
      requestUpdate();
    }
  }

  @Override
  public float getNoiseOverSignalRatio()
  {
    return mNoiseOverSignalRatio;
  }

  @Override
  public void setNoiseOverSignalRatio(float pNoiseOverSignalRatio)
  {
    if (mNoiseOverSignalRatio != pNoiseOverSignalRatio)
    {
      mNoiseOverSignalRatio = pNoiseOverSignalRatio;
      requestUpdate();
    }
  }

  @Override
  public int getBeginZ()
  {
    return mStartZ;
  }

  @Override
  public void setBeginZ(int pStartZ)
  {
    if (mStartZ != pStartZ)
    {
      mStartZ = pStartZ;
      requestUpdate();
    }
  }

  @Override
  public int getEndZ()
  {
    return mEndZ;
  }

  @Override
  public void setEndZ(int pEndZ)
  {
    if (mEndZ != pEndZ)
    {
      mEndZ = pEndZ;
      requestUpdate();
    }
  }

  @Override
  public void close()
  {
    if (mRenderKernel != null)
      mRenderKernel.close();
    super.close();
  }

}

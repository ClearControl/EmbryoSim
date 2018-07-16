package simbryo.synthoscopy.phantom.fluo;

import java.io.IOException;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.ClearCLProgram;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.KernelAccessType;
import clearcl.util.ElapsedTime;
import coremem.enums.NativeTypeEnum;
import coremem.offheap.OffHeapMemory;
import coremem.util.Size;
import simbryo.dynamics.tissue.TissueDynamics;
import simbryo.dynamics.tissue.TissueDynamicsInterface;
import simbryo.dynamics.tissue.cellprop.HasPolarity;
import simbryo.synthoscopy.phantom.PhantomRendererBase;
import simbryo.synthoscopy.phantom.PhantomRendererInterface;
import simbryo.textures.noise.FractalNoise;
import simbryo.textures.noise.SimplexNoise;

/**
 * This renders histone fluorescence for the nuclei.
 *
 * @author royer
 */
public abstract class HistoneFluorescence extends PhantomRendererBase
                                          implements
                                          PhantomRendererInterface<ClearCLImage>
{
  private static final int cNoiseDim = 32;
  private ClearCLBuffer mNeighboorsBuffer, mPositionsBuffer,
      mPolaritiesBuffer, mRadiiBuffer;
  private OffHeapMemory mNeighboorsMemory, mPositionsMemory,
      mPolaritiesMemory, mRadiiMemory;
  private ClearCLImage mPerlinNoiseImage;

  private float mNucleiRadius, mNucleiSharpness, mNucleiRoughness,
      mNucleiTextureContrast;
  private boolean mHasPolarity;

  /**
   * Instantiates a histone fluorescence renderer for a given OpenCL context,
   * tissue dynamics, and stack dimensions.
   * 
   * @param pContext
   *          OpenCL context
   * @param pTissueDynamics
   *          tissue dynamics
   * @param pStackDimensions
   *          stack dimensions
   * @throws IOException
   *           thrown in case kernel code cannot be read.
   */
  public HistoneFluorescence(ClearCLContext pContext,
                             TissueDynamics pTissueDynamics,
                             long... pStackDimensions) throws IOException
  {
    this(pContext,
         pTissueDynamics,
         16,
         0.004f,
         0.95f,
         0.5f,
         0.75f,
         1e-2f,
         pStackDimensions);
  }

  /**
   * Instantiates a histone fluorescence renderer for a given OpenCL context,
   * tissue dynamics, and stack dimensions.
   * 
   * @param pContext
   *          OpenCL context
   * @param pTissueDynamics
   *          tissue dynamics
   * @param pMaxParticlesPerGridCell
   *          max number of particles/nuclei/cells per work group. This depends
   *          on the density of the cells and on the capabilities of the OpenCL
   *          device.
   * @param pNucleiRadius
   *          nuclei radius
   * @param pNucleiSharpness
   *          nuclei sharpness
   * @param pNucleiRoughness
   *          nuclei roughness
   * @param pNucleiTextureContrast
   *          nuclei texture contrast
   * @param pNoiseOverSignalRatio
   *          noise over signal ratio
   * @param pStackDimensions
   *          stack dimensions
   * @throws IOException
   *           thrown in OpenCL kernels cannot be read.
   */
  public HistoneFluorescence(ClearCLContext pContext,
                             TissueDynamicsInterface pTissueDynamics,
                             int pMaxParticlesPerGridCell,
                             float pNucleiRadius,
                             float pNucleiSharpness,
                             float pNucleiRoughness,
                             float pNucleiTextureContrast,
                             float pNoiseOverSignalRatio,
                             long... pStackDimensions) throws IOException
  {
    super(pContext,
          pMaxParticlesPerGridCell,
          pTissueDynamics,
          pStackDimensions);

    mHasPolarity = pTissueDynamics instanceof HasPolarity;
    mNucleiRadius = pNucleiRadius;
    mNucleiSharpness = pNucleiSharpness;
    mNucleiRoughness = pNucleiRoughness;
    mNucleiTextureContrast = pNucleiTextureContrast;
    setNoiseOverSignalRatio(pNoiseOverSignalRatio);

    final int lMaxParticlesPerGridCell =
                                       mNeighborhoodGrid.getMaxParticlesPerGridCell();

    setupNoiseBuffers(mContext);

    setupProgramAndKernel(lMaxParticlesPerGridCell);

    setupBuffersAndImages(pTissueDynamics, lMaxParticlesPerGridCell);

    mRenderKernel.setArgument("image", mImage);
    mRenderKernel.setArgument("neighboors", mNeighboorsBuffer);
    mRenderKernel.setArgument("positions", mPositionsBuffer);
    mRenderKernel.setArgument("polarities", mPolaritiesBuffer);
    mRenderKernel.setArgument("radii", mRadiiBuffer);
    mRenderKernel.setArgument("perlin", mPerlinNoiseImage);

  }

  private void setupBuffersAndImages(TissueDynamicsInterface pTissueDynamics,
                                     final int lMaxParticlesPerGridCell)
  {
    final int lDimension = getTissue().getDimension();

    final int lNeighboorsArrayLength = mNeighborhoodGrid.getVolume()
                                       * lMaxParticlesPerGridCell;

    mNeighboorsBuffer =
                      mContext.createBuffer(HostAccessType.WriteOnly,
                                            KernelAccessType.ReadOnly,
                                            NativeTypeEnum.Int,
                                            lNeighboorsArrayLength);

    mPositionsBuffer =
                     mContext.createBuffer(HostAccessType.WriteOnly,
                                           KernelAccessType.ReadOnly,
                                           NativeTypeEnum.Float,
                                           lDimension * pTissueDynamics.getMaxNumberOfParticles());

    mPolaritiesBuffer =
                      mContext.createBuffer(HostAccessType.WriteOnly,
                                            KernelAccessType.ReadOnly,
                                            NativeTypeEnum.Float,
                                            lDimension * pTissueDynamics.getMaxNumberOfParticles());
    mPolaritiesBuffer.fill((byte) 1, true);

    mRadiiBuffer =
                 mContext.createBuffer(HostAccessType.WriteOnly,
                                       KernelAccessType.ReadOnly,
                                       NativeTypeEnum.Float,
                                       pTissueDynamics.getMaxNumberOfParticles());

    mNeighboorsMemory =
                      OffHeapMemory.allocateInts(lNeighboorsArrayLength);

    mPositionsMemory =
                     OffHeapMemory.allocateFloats(lDimension
                                                  * pTissueDynamics.getMaxNumberOfParticles());

    mPolaritiesMemory =
                      OffHeapMemory.allocateFloats(lDimension
                                                   * pTissueDynamics.getMaxNumberOfParticles());
    mRadiiMemory =
                 OffHeapMemory.allocateFloats(pTissueDynamics.getMaxNumberOfParticles());
  }

  protected void setupProgramAndKernel(final int pMaxParticlesPerGridCell) throws IOException
  {
    ClearCLProgram lProgram = mContext.createProgram();

    addAutoFluoFunctionSourceCode(lProgram);
    lProgram.addSource(HistoneFluorescence.class,
                       "kernel/HistoneFluoRender.cl");

    lProgram.addDefine("MAXNEI", pMaxParticlesPerGridCell);
    lProgram.addDefine("NOISEDIM", cNoiseDim);

    lProgram.addDefine("NOISERATIO", getNoiseOverSignalRatio());
    lProgram.addDefine("NUCLEIRADIUS", getNucleiRadius());
    lProgram.addDefine("NUCLEISHARPNESS", getNucleiSharpness());
    lProgram.addDefine("NUCLEIROUGHNESS", getNucleiRoughness());
    lProgram.addDefine("NUCLEITEXTURECONTRAST",
                       getNucleiTextureContrast());

    lProgram.addBuildOptionAllMathOpt();
    lProgram.buildAndLog();
    // System.out.println(lProgram.getSourceCode());

    mRenderKernel = lProgram.createKernel("hisrender");
  }

  /**
   * This function must be implemented by derived classes. It adds the souce
   * code to the autofluo function to the rendering kernel. this functions is
   * responsible for rendering the background fluorescence.
   * 
   * @param pClearCLProgram
   *          program to add source to.
   * @throws IOException
   *           thrown if source code canot be read.
   */
  public abstract void addAutoFluoFunctionSourceCode(ClearCLProgram pClearCLProgram) throws IOException;

  private void setupNoiseBuffers(ClearCLContext pContext) throws IOException
  {
    SimplexNoise lSimplexNoise = new SimplexNoise(3);
    FractalNoise lFractalNoise = new FractalNoise(lSimplexNoise,
                                                  1f,
                                                  0.5f,
                                                  0.25f,
                                                  0.125f,
                                                  0.0625f);
    // lFractalNoise.setScales(1000f);

    float[] lTexture = lFractalNoise.generateTexture(cNoiseDim,
                                                     cNoiseDim,
                                                     cNoiseDim);

    mPerlinNoiseImage =
                      pContext.createSingleChannelImage(ImageChannelDataType.Float,
                                                        cNoiseDim,
                                                        cNoiseDim,
                                                        cNoiseDim);

    mPerlinNoiseImage.readFrom(lTexture, true);
  }

  protected void updateBuffers()
  {

    final int lDimension = getTissue().getDimension();
    final int lNumberOfCells = getTissue().getMaxNumberOfParticles();

    getTissue().updateNeighborhoodGrid(mNeighborhoodGrid);

    ElapsedTime.measure("data copy", () -> {

      mNeighboorsMemory.copyFrom(mNeighborhoodGrid.getArray());

      mPositionsMemory.copyFrom(getTissue().getPositions()
                                           .getCurrentArray(),
                                0,
                                0,
                                lDimension * lNumberOfCells);

      if (mHasPolarity)
      {
        HasPolarity lHasPolarity = (HasPolarity) getTissue();
        float[] lPolarityArray = lHasPolarity.getPolarityProperty()
                                             .getArray()
                                             .getCurrentArray();

        mPolaritiesMemory.copyFrom(lPolarityArray,
                                   0,
                                   0,
                                   lDimension * lNumberOfCells);
      }

      mRadiiMemory.copyFrom(getTissue().getRadii().getCurrentArray(),
                            0,
                            0,
                            lNumberOfCells);

      mNeighboorsBuffer.readFrom(mNeighboorsMemory, false);

      mPositionsBuffer.readFrom(mPositionsMemory.subRegion(0,
                                                           lNumberOfCells
                                                              * 3
                                                              * Size.FLOAT),
                                false);

      mPolaritiesBuffer.readFrom(mPolaritiesMemory.subRegion(0,
                                                             lNumberOfCells
                                                                * 3
                                                                * Size.FLOAT),
                                 false);

      mRadiiBuffer.readFrom(mRadiiMemory.subRegion(0,
                                                   lNumberOfCells
                                                      * Size.FLOAT),
                            true);
    });
    /**/
  }

  /**
   * Returns nuclei radius
   * 
   * @return radius
   */
  public float getNucleiRadius()
  {
    return mNucleiRadius;
  }

  /**
   * Sets nuclei radius
   * 
   * @param pNucleiRadius
   *          nuclei radius
   */
  public void setNucleiRadius(float pNucleiRadius)
  {
    if (mNucleiRadius != pNucleiRadius)
    {
      mNucleiRadius = pNucleiRadius;
      requestUpdate();
    }
  }

  /**
   * Return nuclei sharpness
   * 
   * @return nuclei sharpness
   */
  public float getNucleiSharpness()
  {
    return mNucleiSharpness;
  }

  /**
   * Sets nuclei sharpness
   * 
   * @param pNucleiSharpness
   *          nuclei sharpness
   */
  public void setNucleiSharpness(float pNucleiSharpness)
  {
    if (mNucleiSharpness != pNucleiSharpness)
    {
      mNucleiSharpness = pNucleiSharpness;
      requestUpdate();
    }
  }

  /**
   * Returns nuclei roughness.
   * 
   * @return nuclei roughness
   */
  public float getNucleiRoughness()
  {
    return mNucleiRoughness;
  }

  /**
   * Sets nuclei roughness
   * 
   * @param pNucleiRoughness
   *          nuclei roughness
   */
  public void setNucleiRoughness(float pNucleiRoughness)
  {
    if (mNucleiRoughness != pNucleiRoughness)
    {
      mNucleiRoughness = pNucleiRoughness;
      requestUpdate();
    }
  }

  /**
   * Return contrast of nuclei texture: min:0 max:1
   * 
   * @return contrast of nuclei texture
   */
  public float getNucleiTextureContrast()
  {
    return mNucleiTextureContrast;
  }

  /**
   * Sets contrast of nuclei texture: min:0 max:1
   * 
   * @param pNucleiTextureContrast
   *          new contrast of nuclei texture
   */
  public void setNucleiTextureContrast(float pNucleiTextureContrast)
  {
    if (mNucleiTextureContrast != pNucleiTextureContrast)
    {
      mNucleiTextureContrast = pNucleiTextureContrast;
      requestUpdate();
    }
  }

  @Override
  public void clear(boolean pWaitToFinish)
  {
    super.clear(pWaitToFinish);
  }

  @Override
  public void render(boolean pWaitToFinish)
  {
    if (isUpdateNeeded())
      updateBuffers();
    super.render(pWaitToFinish);
  }

  @Override
  public void close()
  {
    super.close();
    mNeighboorsBuffer.close();
    mPositionsBuffer.close();
    mRadiiBuffer.close();
    mPerlinNoiseImage.close();
  }

}

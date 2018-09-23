package simbryo.synthoscopy.microscope.lightsheet;

import static java.lang.Math.min;
import static java.lang.Math.round;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.util.ElapsedTime;
import clearcl.viewer.ClearCLImageViewer;
import coremem.ContiguousMemoryInterface;
import simbryo.synthoscopy.camera.impl.SCMOSCameraRenderer;
import simbryo.synthoscopy.microscope.MicroscopeSimulatorBase;
import simbryo.synthoscopy.microscope.lightsheet.gui.LightSheetMicroscopeSimulatorViewer;
import simbryo.synthoscopy.microscope.parameters.CameraParameter;
import simbryo.synthoscopy.microscope.parameters.DetectionParameter;
import simbryo.synthoscopy.microscope.parameters.IlluminationParameter;
import simbryo.synthoscopy.microscope.parameters.PhantomParameter;
import simbryo.synthoscopy.microscope.parameters.StageParameter;
import simbryo.synthoscopy.microscope.parameters.UnitConversion;
import simbryo.synthoscopy.optics.detection.impl.widefield.WideFieldDetectionOptics;
import simbryo.synthoscopy.optics.illumination.combiner.IlluminationCombiner;
import simbryo.synthoscopy.optics.illumination.impl.lightsheet.LightSheetIllumination;
import simbryo.util.geom.GeometryUtils;

/**
 * Light sheet microscope simulator
 *
 * @author royer
 */
public class LightSheetMicroscopeSimulator extends
                                           MicroscopeSimulatorBase
                                           implements
                                           LightSheetMicroscopeSimulatorInterface
{

  private static final float cDepthOfIlluminationInNormUnits = 1f;

  private static final int cLightMapScaleFactor = 2;

  private ArrayList<LightSheetIllumination> mLightSheetIlluminationList =
                                                                        new ArrayList<>();
  private IlluminationCombiner<LightSheetIllumination> mIlluminationCombiner;

  private ArrayList<WideFieldDetectionOptics> mWideFieldDetectionOpticsList =
                                                                            new ArrayList<>();
  private ArrayList<SCMOSCameraRenderer> mCameraRendererList =
                                                             new ArrayList<>();

  private ConcurrentHashMap<Integer, Matrix4f> mDetectionTransformationMatrixMap =
                                                                                 new ConcurrentHashMap<>();

  /**
   * Instantiates a light sheet microscope simulator given a ClearCL context
   * 
   * @param pContext
   *          ClearCL context
   * @param pMainPhantomDimensions
   *          main phantom dimensions.
   */
  public LightSheetMicroscopeSimulator(ClearCLContext pContext,
                                       long... pMainPhantomDimensions)
  {
    super(pContext, pMainPhantomDimensions);

  }

  @Override
  public LightSheetIllumination addLightSheet(Vector3f pAxisVector,
                                              Vector3f pNormalVector)
  {
    try
    {
      long lWidth = getWidth() / cLightMapScaleFactor;
      long lHeight = getHeight() / cLightMapScaleFactor;
      long lDepth =
                  min(getDepth(),
                      closestOddInteger(getDepth()
                                        * cDepthOfIlluminationInNormUnits));

      LightSheetIllumination lLightSheetIllumination =
                                                     new LightSheetIllumination(mContext,
                                                                                lWidth,
                                                                                lHeight,
                                                                                lDepth);

      lLightSheetIllumination.setLightSheetAxisVector(pAxisVector);
      lLightSheetIllumination.setLightSheetNormalVector(pNormalVector);

      mLightSheetIlluminationList.add(lLightSheetIllumination);
      return lLightSheetIllumination;
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void addDetectionPath(Matrix4f pDetectionTransformMatrix,
                               Vector3f pDownUpVector,
                               int pMaxCameraWidth,
                               int pMaxCameraHeight)
  {
    try
    {
      mDetectionTransformationMatrixMap.put(mWideFieldDetectionOpticsList.size(),
                                            pDetectionTransformMatrix);

      WideFieldDetectionOptics lWideFieldDetectionOptics =
                                                         new WideFieldDetectionOptics(mContext,
                                                                                      getWidth(),
                                                                                      getHeight());

      mWideFieldDetectionOpticsList.add(lWideFieldDetectionOptics);

      SCMOSCameraRenderer lSCMOSCameraRenderer =
                                               new SCMOSCameraRenderer(mContext,
                                                                       pMaxCameraWidth,
                                                                       pMaxCameraHeight);
      lSCMOSCameraRenderer.setDetectionDownUpVector(pDownUpVector);
      lWideFieldDetectionOptics.addUpdateListener(lSCMOSCameraRenderer);

      mCameraRendererList.add(lSCMOSCameraRenderer);
    }
    catch (IOException e)
    {
      throw new RuntimeException();
    }
  }

  /**
   * Must be called after all lightsheets and detection arms have been added.
   * 
   * @throws IOException
   *           thrown if problems while reading kernel sources
   */
  @Override
  public void buildMicroscope() throws IOException
  {
    mIlluminationCombiner =
                          new IlluminationCombiner<>(mContext,
                                                     mLightSheetIlluminationList);

    for (LightSheetIllumination lLightSheetIllumination : mLightSheetIlluminationList)
    {
      lLightSheetIllumination.addUpdateListener(mIlluminationCombiner);

    }

    for (WideFieldDetectionOptics lWideFieldDetectionOptics : mWideFieldDetectionOpticsList)
    {
      mIlluminationCombiner.addUpdateListener(lWideFieldDetectionOptics);
    }
  }

  @Override
  public int getNumberOfLightSheets()
  {
    return mLightSheetIlluminationList.size();
  }

  @Override
  public int getNumberOfDetectionArms()
  {
    return mWideFieldDetectionOpticsList.size();
  }

  @Override
  public LightSheetIllumination getLightSheet(int pIndex)
  {
    return mLightSheetIlluminationList.get(pIndex);
  }

  @Override
  public WideFieldDetectionOptics getDetectionOptics(int pIndex)
  {
    return mWideFieldDetectionOpticsList.get(pIndex);
  }

  /**
   * Returns camera for index
   * 
   * @param pIndex
   *          index
   * @return camera
   */
  @Override
  public SCMOSCameraRenderer getCameraRenderer(int pIndex)
  {
    return mCameraRendererList.get(pIndex);
  }

  private void applyParametersForLightSheet(int pLightSheetIndex,
                                            int pDetectionPathIndex)
  {
    float lLengthConversionfactor =
                                  getNumberParameterWithAberrations(UnitConversion.Length,
                                                                    0).floatValue();
    float lLaserPowerConversionfactor =
                                      getNumberParameterWithAberrations(UnitConversion.LaserIntensity,
                                                                        0).floatValue();

    LightSheetIllumination lLightSheetIllumination =
                                                   mLightSheetIlluminationList.get(pLightSheetIndex);

    lLightSheetIllumination.setDetectionTransformMatrix(getDetectionTransformMatrix(pDetectionPathIndex));
    lLightSheetIllumination.setPhantomTransformMatrix(getStageTransformMatrix());

    lLightSheetIllumination.setScatteringPhantom(getPhantomParameter(PhantomParameter.Scattering));

    float lIntensity =
                     getNumberParameterWithAberrations(IlluminationParameter.Intensity,
                                                       pLightSheetIndex).floatValue()
                       / lLaserPowerConversionfactor;

    float lWaveLength =
                      getNumberParameterWithAberrations(IlluminationParameter.Wavelength,
                                                        pLightSheetIndex).floatValue();

    float xl =
             (getNumberParameterWithAberrations(IlluminationParameter.X,
                                                pLightSheetIndex).floatValue()
              / lLengthConversionfactor) + 0.5f;
    float yl =
             (getNumberParameterWithAberrations(IlluminationParameter.Y,
                                                pLightSheetIndex).floatValue()
              / lLengthConversionfactor) + 0.5f;
    float zl =
             (getNumberParameterWithAberrations(IlluminationParameter.Z,
                                                pLightSheetIndex).floatValue()
              / lLengthConversionfactor) + 0.5f;

    float height =
                 getNumberParameterWithAberrations(IlluminationParameter.Height,
                                                   pLightSheetIndex).floatValue()
                   / lLengthConversionfactor;

    float alpha =
                getNumberParameterWithAberrations(IlluminationParameter.Alpha,
                                                  pLightSheetIndex).floatValue();
    float beta =
               getNumberParameterWithAberrations(IlluminationParameter.Beta,
                                                 pLightSheetIndex).floatValue();
    float gamma =
                getNumberParameterWithAberrations(IlluminationParameter.Gamma,
                                                  pLightSheetIndex).floatValue();
    float theta =
                getNumberParameterWithAberrations(IlluminationParameter.Theta,
                                                  pLightSheetIndex).floatValue();

    lLightSheetIllumination.setIntensity(lIntensity);
    lLightSheetIllumination.setLightWavelength(lWaveLength);
    lLightSheetIllumination.setLightSheetPosition(xl, yl, zl);
    lLightSheetIllumination.setLightSheetHeigth(height);
    lLightSheetIllumination.setOrientationWithAnglesInDegrees(alpha,
                                                              beta,
                                                              gamma);
    lLightSheetIllumination.setLightSheetThetaInDeg(theta);

  }

  private void applyParametersForDetectionPath(int pDetectionPathIndex,
                                               ClearCLImage pLightMapImage)
  {
    float lLengthConversionfactor =
                                  getNumberParameterWithAberrations(UnitConversion.Length,
                                                                    0).floatValue();

    WideFieldDetectionOptics lWideFieldDetectionOptics =
                                                       mWideFieldDetectionOpticsList.get(pDetectionPathIndex);
    SCMOSCameraRenderer lSCMOSCameraRenderer =
                                             mCameraRendererList.get(pDetectionPathIndex);

    ClearCLImage lFluorescencePhantomImage =
                                           getPhantomParameter(PhantomParameter.Fluorescence);
    ClearCLImage lScatteringPhantomImage =
                                         getPhantomParameter(PhantomParameter.Scattering);

    float lIntensity =
                     getNumberParameterWithAberrations(DetectionParameter.Intensity,
                                                       pDetectionPathIndex).floatValue();

    float lWaveLength =
                      getNumberParameterWithAberrations(DetectionParameter.Wavelength,
                                                        pDetectionPathIndex).floatValue();

    Matrix4f lDetectionTransformMatrix =
                                       getDetectionTransformMatrix(pDetectionPathIndex);

    float lDetectionZSign =
                          lDetectionTransformMatrix.getElement(0, 0);

    float lFocusZ = (lDetectionZSign
                     * getNumberParameterWithAberrations(DetectionParameter.Z,
                                                         pDetectionPathIndex).floatValue()
                     / lLengthConversionfactor)
                    + 0.5f;

    long lDetectionImageWidth = lFluorescencePhantomImage.getWidth();
    long lDetectionImageHeight =
                               lFluorescencePhantomImage.getHeight();

    int lROIOffsetX =
                    getNumberParameterWithAberrations(CameraParameter.ROIOffsetX,
                                                      pDetectionPathIndex).intValue();
    int lROIOffsetY =
                    getNumberParameterWithAberrations(CameraParameter.ROIOffsetY,
                                                      pDetectionPathIndex).intValue();

    int lROIWidth =
                  getNumberParameterWithAberrations(CameraParameter.ROIWidth,
                                                    pDetectionPathIndex,
                                                    lSCMOSCameraRenderer.getMaxWidth()).intValue();
    int lROIHeight =
                   getNumberParameterWithAberrations(CameraParameter.ROIHeight,
                                                     pDetectionPathIndex,
                                                     lSCMOSCameraRenderer.getMaxHeight()).intValue();

    lWideFieldDetectionOptics.setFluorescencePhantomImage(lFluorescencePhantomImage);
    lWideFieldDetectionOptics.setScatteringPhantomImage(lScatteringPhantomImage);
    lWideFieldDetectionOptics.setLightMapImage(pLightMapImage);

    lWideFieldDetectionOptics.setIntensity(lIntensity);
    lWideFieldDetectionOptics.setLightWavelength(lWaveLength);
    lWideFieldDetectionOptics.setZFocusPosition(lFocusZ);
    lWideFieldDetectionOptics.setWidth(lDetectionImageWidth);
    lWideFieldDetectionOptics.setHeight(lDetectionImageHeight);

    lWideFieldDetectionOptics.setPhantomTransformMatrix(getStageAndDetectionTransformMatrix(pDetectionPathIndex));

    float lExposureInSeconds =
                             getNumberParameterWithAberrations(CameraParameter.Exposure,
                                                               pDetectionPathIndex).floatValue();

    float lMagnification =
                         getNumberParameterWithAberrations(CameraParameter.Magnification,
                                                           pDetectionPathIndex).floatValue();

    float lShiftX =
                  getNumberParameterWithAberrations(CameraParameter.ShiftX,
                                                    pDetectionPathIndex).floatValue();

    float lShiftY =
                  getNumberParameterWithAberrations(CameraParameter.ShiftY,
                                                    pDetectionPathIndex).floatValue();

    lSCMOSCameraRenderer.setDetectionImage(lWideFieldDetectionOptics.getImage());
    lSCMOSCameraRenderer.setExposure(lExposureInSeconds);

    lSCMOSCameraRenderer.setMagnification(lMagnification);
    lSCMOSCameraRenderer.setShiftX(lShiftX);
    lSCMOSCameraRenderer.setShiftY(lShiftY);

    lSCMOSCameraRenderer.setCenteredROI(lROIOffsetX,
                                        lROIOffsetY,
                                        lROIWidth,
                                        lROIHeight);

  }

  private Matrix4f getStageAndDetectionTransformMatrix(int pDetectionPathIndex)
  {
    Matrix4f lCombinedTransformMatrix =
                                      GeometryUtils.multiply(getStageTransformMatrix(),
                                                             getDetectionTransformMatrix(pDetectionPathIndex));
    return lCombinedTransformMatrix;
  }

  private Matrix4f getDetectionTransformMatrix(int pDetectionPathIndex)
  {
    Matrix4f lDetectionTransformationMatrix =
                                            mDetectionTransformationMatrixMap.get(pDetectionPathIndex);
    return new Matrix4f(lDetectionTransformationMatrix);
  }

  private Matrix4f getStageTransformMatrix()
  {
    float lLengthConversionfactor =
                                  getNumberParameter(UnitConversion.Length,
                                                     0).floatValue();

    float lStageX =
                  getNumberParameterWithAberrations(StageParameter.StageX,
                                                    0,
                                                    0).floatValue()
                    / lLengthConversionfactor;
    float lStageY =
                  getNumberParameterWithAberrations(StageParameter.StageY,
                                                    0,
                                                    0).floatValue()
                    / lLengthConversionfactor;
    float lStageZ =
                  getNumberParameterWithAberrations(StageParameter.StageZ,
                                                    0,
                                                    0).floatValue()
                    / lLengthConversionfactor;

    float lStageRX =
                   getNumberParameterWithAberrations(StageParameter.StageRX,
                                                     0,
                                                     0).floatValue();

    float lStageRY =
                   getNumberParameterWithAberrations(StageParameter.StageRY,
                                                     0,
                                                     0).floatValue();
    float lStageRZ =
                   getNumberParameterWithAberrations(StageParameter.StageRZ,
                                                     0,
                                                     0).floatValue();

    Vector3f lCenter = new Vector3f(0.5f, 0.5f, 0.5f);

    Matrix4f lMatrixRX =
                       GeometryUtils.rotX((float) Math.toRadians(lStageRX),
                                          lCenter);
    Matrix4f lMatrixRY =
                       GeometryUtils.rotY((float) Math.toRadians(lStageRY),
                                          lCenter);
    Matrix4f lMatrixRZ =
                       GeometryUtils.rotZ((float) Math.toRadians(lStageRZ),
                                          lCenter);

    Matrix4f lMatrix = GeometryUtils.multiply(lMatrixRX,
                                              lMatrixRY,
                                              lMatrixRZ);

    GeometryUtils.addTranslation(lMatrix, lStageX, lStageY, lStageZ);

    return lMatrix;
  }

  /**
   * Returns pixel width (after converting into unnormalized spatial units using
   * UnitConversion.Length)
   * 
   * @param pCameraIndex
   *          camera index
   * @return pixel width
   */
  public double getPixelWidth(int pCameraIndex)
  {
    float lLengthConversionfactor =
                                  getNumberParameter(UnitConversion.Length,
                                                     0).floatValue();

    SCMOSCameraRenderer lCameraRenderer =
                                        getCameraRenderer(pCameraIndex);

    long lMaxWidth = lCameraRenderer.getMaxWidth();

    double lNormalizedPixelWidth = 1.0 / lMaxWidth;

    double lPixelWidth = lNormalizedPixelWidth
                         * lLengthConversionfactor;

    return lPixelWidth;
  }

  @Override
  public void render(boolean pWaitToFinish)
  {
    int lNumberOfDetectionPath = mWideFieldDetectionOpticsList.size();

    for (int d = 0; d < lNumberOfDetectionPath; d++)
    {
      render(d, pWaitToFinish && (d == lNumberOfDetectionPath - 1));
    }
  }

  @Override
  public void render(int pDetectionIndex, boolean pWaitToFinish)
  {
    WideFieldDetectionOptics lWideFieldDetectionOptics =
                                                       mWideFieldDetectionOpticsList.get(pDetectionIndex);
    SCMOSCameraRenderer lSCMOSCameraRenderer =
                                             mCameraRendererList.get(pDetectionIndex);

    int lNumberOfLightSheets = mLightSheetIlluminationList.size();
    for (int lLightSheetIndex =
                              0; lLightSheetIndex < lNumberOfLightSheets; lLightSheetIndex++)
    {
      LightSheetIllumination lLightSheetIllumination =
                                                     mLightSheetIlluminationList.get(lLightSheetIndex);
      applyParametersForLightSheet(lLightSheetIndex, pDetectionIndex);

      lLightSheetIllumination.requestUpdate();
      ElapsedTime.measure("renderlightsheet",
                          () -> lLightSheetIllumination.render(true));

    }

    ElapsedTime.measure("rendercombinedlightsheet",
                        () -> mIlluminationCombiner.render(true));
    ClearCLImage lCombinedLightMap = mIlluminationCombiner.getImage();

    applyParametersForDetectionPath(pDetectionIndex,
                                    lCombinedLightMap);

    ElapsedTime.measure("renderdetection",
                        () -> lWideFieldDetectionOptics.render(false));

    ElapsedTime.measure("rendercameraimage",
                        () -> lSCMOSCameraRenderer.render(pWaitToFinish));/**/

    lWideFieldDetectionOptics.clearUpdate();
    lSCMOSCameraRenderer.clearUpdate();
  }

  @Override
  public ClearCLImage getCameraImage(int pIndex)
  {
    return mCameraRendererList.get(pIndex).getImage();
  }

  @Override
  public ClearCLImageViewer openViewerForCameraImage(int pIndex)
  {
    SCMOSCameraRenderer lSCMOSCameraRenderer =
                                             mCameraRendererList.get(pIndex);

    final ClearCLImageViewer lViewImage =
                                        lSCMOSCameraRenderer.openViewer();
    return lViewImage;
  }

  @Override
  public ClearCLImageViewer openViewerForLightMap(int pIndex)
  {
    return getLightSheet(pIndex).openViewer();
  }

  @Override
  public void openViewerForAllLightMaps()
  {
    int lNumberOfLightSheets = getNumberOfLightSheets();
    for (int l = 0; l < lNumberOfLightSheets; l++)
      getLightSheet(l).openViewer();
  }

  @Override
  public LightSheetMicroscopeSimulatorViewer openViewerForControls()
  {
    LightSheetMicroscopeSimulatorViewer lViewer =
                                                new LightSheetMicroscopeSimulatorViewer(this,
                                                                                        "LightSheetSimulator");

    return lViewer;
  }

  @Override
  public void copyTo(int pCameraIndex,
                     ContiguousMemoryInterface pContiguousMemory,
                     long pOffsetInContiguousMemory,
                     boolean pBlocking)
  {
    ClearCLImage lCameraImage = getCameraImage(pCameraIndex);
    ContiguousMemoryInterface lImagePlane =
                                          pContiguousMemory.subRegion(pOffsetInContiguousMemory,
                                                                      lCameraImage.getSizeInBytes());
    lCameraImage.writeTo(lImagePlane, pBlocking);
  }

  private int closestOddInteger(float pValue)
  {
    return round((pValue - 1) / 2) * 2 + 1;
  }

  @Override
  public void close() throws Exception
  {
    for (LightSheetIllumination lLightSheetIllumination : mLightSheetIlluminationList)
    {
      lLightSheetIllumination.close();
    }

    for (WideFieldDetectionOptics lWideFieldDetectionOptics : mWideFieldDetectionOpticsList)
    {
      lWideFieldDetectionOptics.close();
    }

    for (SCMOSCameraRenderer lScmosCameraRenderer : mCameraRendererList)
    {
      lScmosCameraRenderer.close();
    }

  }

}

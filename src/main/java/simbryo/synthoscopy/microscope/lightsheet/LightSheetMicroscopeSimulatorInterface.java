package simbryo.synthoscopy.microscope.lightsheet;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import simbryo.synthoscopy.microscope.MicroscopeSimulatorInterface;
import simbryo.synthoscopy.optics.detection.impl.widefield.WideFieldDetectionOptics;
import simbryo.synthoscopy.optics.illumination.impl.lightsheet.LightSheetIllumination;

/**
 *
 *
 * @author royer
 */
public interface LightSheetMicroscopeSimulatorInterface extends
                                                        MicroscopeSimulatorInterface
{

  /**
   * Adds a lightsheet with given axis and normal vectors.
   * 
   * @param pAxisVector
   *          axis vector
   * @param pNormalVector
   *          normal vector
   * @return lightsheet illumination
   */
  LightSheetIllumination addLightSheet(Vector3f pAxisVector,
                                       Vector3f pNormalVector);

  /**
   * Adds detection path. This includes widefield detection optics and a sCMOS
   * camera.
   * 
   * @param pDetectionTransformMatrix
   *          detection transform matrix
   * @param pDownUpVector
   *          updown vector
   * @param pMaxCameraWidth
   *          max camera width
   * @param pMaxCameraHeight
   *          max camera height
   */
  void addDetectionPath(Matrix4f pDetectionTransformMatrix,
                        Vector3f pDownUpVector,
                        int pMaxCameraWidth,
                        int pMaxCameraHeight);

  /**
   * Returns the number of lightsheets
   * 
   * @return number of lightsheets
   */
  int getNumberOfLightSheets();

  /**
   * Returns the number of detection paths
   * 
   * @return number of detection paths
   */
  int getNumberOfDetectionArms();

  /**
   * Returns lightsheet for index
   * 
   * @param pIndex
   *          index
   * @return lightsheet
   */
  LightSheetIllumination getLightSheet(int pIndex);

  /**
   * Returns detection optics for index
   * 
   * @param pIndex
   *          index
   * @return detection optics
   */
  WideFieldDetectionOptics getDetectionOptics(int pIndex);

}

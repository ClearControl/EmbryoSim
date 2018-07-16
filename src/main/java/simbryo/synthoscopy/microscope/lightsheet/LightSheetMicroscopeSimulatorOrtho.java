package simbryo.synthoscopy.microscope.lightsheet;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import java.io.IOException;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import clearcl.ClearCLContext;
import simbryo.util.geom.GeometryUtils;

/**
 * This class knows how to build orthogonal simultaneous multi-view lightsheet
 * microscope simulators with different number of illumination and detection
 * arms
 *
 * @author royer
 */
public class LightSheetMicroscopeSimulatorOrtho extends
                                                LightSheetMicroscopeSimulator
{

  /**
   * Instanciates a simulator with given ClearCL context, number of detection
   * and illumination arms as well as main phantom dimensions
   * 
   * @param pContext
   *          ClearCL context
   * @param pNumberOfDetectionArms
   *          number of detection arms
   * @param pNumberOfIlluminationArms
   *          number of illuination arms
   * @param pMaxCameraResolution
   *          max width and height of camera images
   * @param pMainPhantomDimensions
   *          phantom main dimensions
   */
  public LightSheetMicroscopeSimulatorOrtho(ClearCLContext pContext,
                                            int pNumberOfDetectionArms,
                                            int pNumberOfIlluminationArms,
                                            int pMaxCameraResolution,
                                            long... pMainPhantomDimensions)
  {
    this(pContext,
         GeometryUtils.getIdentity(),
         pNumberOfDetectionArms,
         pNumberOfIlluminationArms,
         pMaxCameraResolution,
         pMainPhantomDimensions);
  }

  /**
   * Instantiates a simulator with given ClearCL context, number of detection
   * and illumination arms as well as main phantom dimensions
   * 
   * @param pContext
   *          ClearCL context
   * @param pCameraMisalignmentMatrix
   *          camera misalignment matrix
   * @param pNumberOfDetectionArms
   *          number of detection arms
   * @param pNumberOfIlluminationArms
   *          number of illumination arms
   * @param pMaxCameraResolution
   *          max width and height of camera images
   * @param pMainPhantomDimensions
   *          phantom main dimensions
   */
  public LightSheetMicroscopeSimulatorOrtho(ClearCLContext pContext,
                                            Matrix4f pCameraMisalignmentMatrix,
                                            int pNumberOfDetectionArms,
                                            int pNumberOfIlluminationArms,
                                            int pMaxCameraResolution,
                                            long... pMainPhantomDimensions)
  {
    super(pContext, pMainPhantomDimensions);

    if (pMainPhantomDimensions.length != 3)
      throw new IllegalArgumentException("Phantom dimensions must have 3 components: (width,height,depth).");

    if (pNumberOfIlluminationArms == 1)
    {
      Vector3f lIlluminationAxisVector = new Vector3f(1, 0, 0);
      Vector3f lIlluminationNormalVector = new Vector3f(0, 0, 1);

      addLightSheet(lIlluminationAxisVector,
                    lIlluminationNormalVector);
    }
    else if (pNumberOfIlluminationArms == 2)
    {
      Vector3f lIlluminationAxisVector0 = new Vector3f(1, 0, 0);
      Vector3f lIlluminationNormalVector0 = new Vector3f(0, 0, 1);

      addLightSheet(lIlluminationAxisVector0,
                    lIlluminationNormalVector0);

      Vector3f lIlluminationAxisVector1 = new Vector3f(-1, 0, 0);
      Vector3f lIlluminationNormalVector1 = new Vector3f(0, 0, -1);

      addLightSheet(lIlluminationAxisVector1,
                    lIlluminationNormalVector1);
    }
    else if (pNumberOfIlluminationArms == 4)
    {
      double gammazero = toRadians(30);
      float ax = (float) cos(gammazero);
      float ay = (float) sin(gammazero);

      Vector3f lIlluminationAxisVector0 = new Vector3f(ax, ay, 0);
      Vector3f lIlluminationAxisVector1 = new Vector3f(ax, -ay, 0);
      Vector3f lIlluminationAxisVector2 = new Vector3f(-ax, ay, 0);
      Vector3f lIlluminationAxisVector3 = new Vector3f(-ax, -ay, 0);
      Vector3f lIlluminationNormalVector01 = new Vector3f(0, 0, 1);
      Vector3f lIlluminationNormalVector23 = new Vector3f(0, 0, -1);

      addLightSheet(lIlluminationAxisVector0,
                    lIlluminationNormalVector01);

      addLightSheet(lIlluminationAxisVector1,
                    lIlluminationNormalVector01);

      addLightSheet(lIlluminationAxisVector2,
                    lIlluminationNormalVector23);

      addLightSheet(lIlluminationAxisVector3,
                    lIlluminationNormalVector23);

    }
    else
    {

      for (int i = 0; i < pNumberOfIlluminationArms; i++)
      {
        double gammazero = toRadians(i * 360
                                     / pNumberOfIlluminationArms);
        System.out.println("gammazero: " + gammazero);

        float ax = (float) cos(gammazero);
        float ay = (float) sin(gammazero);

        // Vector3f lIlluminationAxisVector0 = new Vector3f(ax, ay, 0);
        // Vector3f lIlluminationNormalVector01 = new Vector3f(0, 0, ax * ay);
        // lIlluminationNormalVector01.normalize();

        Vector3f lIlluminationAxisVector0 = new Vector3f(ax, ay, 0);
        Vector3f lIlluminationNormalVector01 = new Vector3f(0, 0, 1);

        addLightSheet(lIlluminationAxisVector0,
                      lIlluminationNormalVector01);
      }
    }

    int lMaxCameraImageWidth = pMaxCameraResolution;
    int lMaxCameraImageHeight = pMaxCameraResolution;

    if (pNumberOfDetectionArms >= 1)
    {
      Matrix4f lDetectionMatrix = new Matrix4f();
      lDetectionMatrix.setIdentity();

      Vector3f lDetectionUpDownVector = new Vector3f(0, 1, 0);

      addDetectionPath(lDetectionMatrix,
                       lDetectionUpDownVector,
                       lMaxCameraImageWidth,
                       lMaxCameraImageHeight);
    }

    if (pNumberOfDetectionArms >= 2)
    {
      Matrix4f lDetectionMatrix =
                                GeometryUtils.rotY((float) Math.PI,
                                                   new Vector3f(0.5f,
                                                                0.5f,
                                                                0.5f));

      lDetectionMatrix =
                       GeometryUtils.multiply(pCameraMisalignmentMatrix,
                                              lDetectionMatrix);

      Vector3f lDetectionUpDownVector = new Vector3f(0, 1, 0);

      addDetectionPath(lDetectionMatrix,
                       lDetectionUpDownVector,
                       lMaxCameraImageWidth,
                       lMaxCameraImageHeight);
    }

    try
    {
      buildMicroscope();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

  }

}

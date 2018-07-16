package simbryo.synthoscopy.microscope.parameters;

import simbryo.synthoscopy.camera.ClearCLCameraRendererBase;

/**
 * Camera Parameters
 *
 * @author royer
 */
@SuppressWarnings("javadoc")
public enum CameraParameter implements ParameterInterface<Number>
{
 // unit: seconds,longest exposure is 1 hour...
 Exposure(ClearCLCameraRendererBase.cNormalExposure, 0, 60 * 60),

 ROIXMin(0, 0, 2048),
 ROIXMax(0, 0, 2048),
 ROIWidth(1024, 0, 2048),
 ROIHeight(1024, 0, 2048),
 ROIOffsetX(0, -1024, 1024),
 ROIOffsetY(0, -1024, 1024),

 Magnification(1, 0, 10),
 ShiftX(0, -1, 1),
 ShiftY(0, -1, 1);

  Number mDefaultValue, mMinValue, mMaxValue;

  private CameraParameter(Number pDefaultValue,
                          Number pMinValue,
                          Number pMaxValue)
  {
    mDefaultValue = pDefaultValue;
    mMinValue = pMinValue;
    mMaxValue = pMaxValue;
  }

  @Override
  public Number getDefaultValue()
  {
    return mDefaultValue;
  }

  @Override
  public Number getMinValue()
  {
    return mMinValue;
  }

  @Override
  public Number getMaxValue()
  {
    return mMaxValue;
  }
}

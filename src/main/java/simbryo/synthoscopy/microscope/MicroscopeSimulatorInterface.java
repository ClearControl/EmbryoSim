package simbryo.synthoscopy.microscope;

import java.io.IOException;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import clearcl.viewer.ClearCLImageViewer;
import coremem.ContiguousMemoryInterface;
import simbryo.SimulationInterface;
import simbryo.synthoscopy.camera.impl.SCMOSCameraRenderer;
import simbryo.synthoscopy.microscope.aberration.AberrationInterface;
import simbryo.synthoscopy.microscope.lightsheet.gui.LightSheetMicroscopeSimulatorViewer;
import simbryo.synthoscopy.microscope.parameters.ParameterInterface;

/**
 * Microscope Optics simulator
 *
 * @author royer
 */
public interface MicroscopeSimulatorInterface extends
                                              SimulationInterface,
                                              AutoCloseable
{

  /**
   * Returns the ClearCL context used by this microscope simulator.
   * 
   * @return context
   */
  ClearCLContext getContext();

  /**
   * Called afetr all optical components have been added.
   * 
   * @throws IOException
   *           thrown if problems while reading kernel sources
   */
  void buildMicroscope() throws IOException;

  /**
   * Sets a given type of phantom image
   * 
   * @param pParameter
   *          phatom type
   * @param pPhantom
   *          phantom image
   */
  void setPhantomParameter(ParameterInterface<Void> pParameter,
                           ClearCLImage pPhantom);

  /**
   * Returns a phantom parameter of given type.
   * 
   * @param pParameter
   *          phantom type
   * @return phantom parameter
   */
  ClearCLImage getPhantomParameter(ParameterInterface<Void> pParameter);

  /**
   * Sets a microscope parameter of given type and index.
   * 
   * @param pParameter
   *          parameter type
   * @param pIndex
   *          index of parameter (usually device/component index)
   * @param pValue
   *          value
   */
  void setNumberParameter(ParameterInterface<Number> pParameter,
                          int pIndex,
                          Number pValue);

  /**
   * Returns the value of a given parameter type and index.
   * 
   * @param pParameter
   *          parameter type
   * @param pIndex
   *          parameter index
   * @return parameter value
   */
  Number getNumberParameter(ParameterInterface<Number> pParameter,
                            int pIndex);

  /**
   * Returns the value of a given parameter type and index. The value is then
   * transformed according to the abberations added to this microscope.
   * 
   * @param pParameter
   *          parameter type
   * @param pIndex
   *          parameter index
   * @return parameter value
   */
  Number getNumberParameterWithAberrations(ParameterInterface<Number> pParameter,
                                           int pIndex);

  /**
   * Returns the value of a given parameter type and index. The parameter's
   * default value is overridden by a provided value.
   * 
   * @param pParameter
   *          parameter type
   * @param pIndex
   *          parameter index
   * @param pDefaultOverideValue
   *          value overriding the parameter defaults
   * @return parameter value
   */
  Number getNumberParameter(ParameterInterface<Number> pParameter,
                            int pIndex,
                            Number pDefaultOverideValue);

  /**
   * Returns the value of a given parameter type and index. The parameter's
   * default value is overridden by a provided value. The value is then
   * transformed according to the abberations added to this microscope
   * 
   * @param pParameter
   *          parameter type
   * @param pIndex
   *          parameter index
   * @param pDefaultOverideValue
   *          value overriding the parameter defaults
   * @return parameter value
   */
  Number getNumberParameterWithAberrations(ParameterInterface<Number> pParameter,
                                           int pIndex,
                                           Number pDefaultOverideValue);

  /**
   * Adds aberrations to this microscope.
   * 
   * @param pAberrations
   */
  void addAbberation(AberrationInterface... pAberrations);

  /**
   * Renders all nescessary intermediate as well as final images for all
   * detetion paths and cameras.
   * 
   * @param pWaitToFinish
   *          true -> wait to finish GPU computations
   */
  void render(boolean pWaitToFinish);

  /**
   * Renders all nescessary intermediate as well as final images for a given
   * detection path and camera.
   * 
   * @param pDetectionIndex
   * @param pWaitToFinish
   */
  void render(int pDetectionIndex, boolean pWaitToFinish);

  /**
   * Returns camera renderer of given index.
   * 
   * @param pCameraIndex
   *          camera renderer index
   * @return camera renderer
   */
  SCMOSCameraRenderer getCameraRenderer(int pCameraIndex);

  /**
   * Returns camera image of given index.
   * 
   * @param pCameraIndex
   *          camera index
   * @return camera image
   */
  ClearCLImage getCameraImage(int pCameraIndex);

  /**
   * Copies the contents of the camera image of given camera index to a
   * contiguous memory buffer.
   * 
   * @param pIndex
   *          camera index
   * @param pContiguousMemory
   *          contiguous memory
   * @param pOffsetInContiguousMemory
   *          offset in bytes into the contiguous memory buffer
   * @param pWaitToFinish
   *          true -> waitsfor copy to finish
   */
  void copyTo(int pIndex,
              ContiguousMemoryInterface pContiguousMemory,
              long pOffsetInContiguousMemory,
              boolean pWaitToFinish);

  /**
   * Opens a viewer for the camera image for a given camera index.
   * 
   * @param pCameraIndex
   *          camera index
   * @return viewer
   */
  ClearCLImageViewer openViewerForCameraImage(int pCameraIndex);

  /**
   * Opens viewer for lightmap of given index.
   * 
   * @param pIndex
   *          lightmap index
   * @return viewer
   */
  ClearCLImageViewer openViewerForLightMap(int pIndex);

  /**
   * Opens viewer for all lightmaps.
   */
  void openViewerForAllLightMaps();

  /**
   * Opens a viewer for the microscope control parameters.
   * 
   * @return viewer
   */
  LightSheetMicroscopeSimulatorViewer openViewerForControls();

}

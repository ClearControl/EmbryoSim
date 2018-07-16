package simbryo.synthoscopy.phantom.scatter;

import java.io.IOException;

import clearcl.ClearCLContext;
import clearcl.ClearCLImage;
import simbryo.dynamics.tissue.TissueDynamics;
import simbryo.dynamics.tissue.TissueDynamicsInterface;
import simbryo.synthoscopy.phantom.PhantomRendererBase;
import simbryo.synthoscopy.phantom.PhantomRendererInterface;

/**
 * This renders a scattering phantom.
 *
 * @author royer
 */
public abstract class ScatteringPhantom extends PhantomRendererBase
                                        implements
                                        PhantomRendererInterface<ClearCLImage>
{

  /**
   * Instantiates a scattering phantom renderer for a given OpenCL context,
   * tissue dynamics, and stack dimensions.
   * 
   * @param pContext
   *          OpenCL context
   * @param pMaxParticlesPerGridCell
   *          max number of particles per grid cell
   * @param pTissueDynamics
   *          tissue dynamics
   * @param pStackDimensions
   *          stack dimensions
   * @throws IOException
   *           thrown in case kernel code cannot be read.
   */
  public ScatteringPhantom(ClearCLContext pContext,
                           int pMaxParticlesPerGridCell,
                           TissueDynamics pTissueDynamics,
                           long... pStackDimensions) throws IOException
  {
    this(pContext,
         pMaxParticlesPerGridCell,
         pTissueDynamics,
         1e-2f,
         pStackDimensions);
  }

  /**
   * Instantiates a scattering phantom renderer for a given OpenCL context,
   * tissue dynamics, and stack dimensions.
   * 
   * @param pContext
   *          OpenCL context
   * @param pMaxParticlesPerGridCell
   *          max number of particles per grid cell
   * @param pTissueDynamics
   *          tissue dynamics
   * @param pNoiseOverSignalRatio
   *          noise over signal ratio
   * @param pStackDimensions
   *          stack dimensions
   * @throws IOException
   *           thrown in OpenCL kernels cannot be read.
   */
  public ScatteringPhantom(ClearCLContext pContext,
                           int pMaxParticlesPerGridCell,
                           TissueDynamicsInterface pTissueDynamics,
                           float pNoiseOverSignalRatio,
                           long... pStackDimensions) throws IOException
  {
    super(pContext,
          pMaxParticlesPerGridCell,
          pTissueDynamics,
          pStackDimensions);

    setNoiseOverSignalRatio(pNoiseOverSignalRatio);
  }

}

package simbryo.particles;

import java.io.Serializable;

import simbryo.particles.forcefield.ForceFieldInterface;
import simbryo.particles.neighborhood.NeighborhoodGrid;
import simbryo.util.DoubleBufferingFloatArray;

/**
 * Particle systems interface
 *
 * @author royer
 */
public interface ParticleSystemInterface extends Serializable
{

  /**
   * Returns the positions arrays
   * 
   * @return positions arrays
   */
  DoubleBufferingFloatArray getPositions();

  /**
   * Returns the velocities arrays
   * 
   * @return velocities arrays
   */
  DoubleBufferingFloatArray getVelocities();

  /**
   * Returns the radii arrays
   * 
   * @return radii arrays
   */
  DoubleBufferingFloatArray getRadii();

  /**
   * Returns the internal neighborhood grid object.
   * 
   * @return neighborhood grid
   */
  NeighborhoodGrid getNeighborhoodGrid();

  /**
   * Returns current number of particles.
   * 
   * @return number of particles
   */
  int getNumberOfParticles();

  /**
   * Returns max number of particles.
   * 
   * @return max number of particles
   */
  int getMaxNumberOfParticles();

  /**
   * Returns the max number of particles per grid cell.
   * 
   * @return max number of particles per grid cell.
   */
  int getMaxNumberOfParticlesPerGridCell();

  /**
   * Returns dimension
   * 
   * @return dimension
   */
  int getDimension();

  /**
   * Returns grid dimensions
   * 
   * @return grid dimensions
   */
  int[] getGridDimensions();

  /**
   * Returns a particle radius.
   * 
   * @param pParticleId
   *          particle id.
   * @return radius
   */
  float getRadius(int pParticleId);

  /**
   * Adds a particle to this particle system at a given position. The particle
   * id is returned.
   * 
   * @param pPosition
   * @return particle id.
   */
  int addParticle(float... pPosition);

  /**
   * Removes a particle to this particle system.
   * 
   * @param pParticleId
   */
  void removeParticle(int pParticleId);

  /**
   * Copies a source particle parameters to a destination particle parameters.
   * 
   * @param pSourceParticleId
   *          source id
   * @param pDestinationParticleId
   *          destination id
   */
  void copyParticle(int pSourceParticleId,
                    int pDestinationParticleId);

  /**
   * Clones a particle and adds some noise to its position.
   * 
   * @param pSourceParticleId
   *          source id
   * @param pNoiseFactor
   *          noise factor
   * @return new particle id.
   */
  int cloneParticle(int pSourceParticleId, float pNoiseFactor);

  /**
   * Adds noise to a particle position, velocity, and radius.
   * 
   * @param pParticleId
   *          particle id
   * @param pPositionNoise
   *          position noise
   * @param pVelocityNoise
   *          velocity noise
   * @param pRadiusNoise
   *          radius noise
   */
  void addNoiseToParticle(int pParticleId,
                          float pPositionNoise,
                          float pVelocityNoise,
                          float pRadiusNoise);

  /**
   * Sets the position of a particle
   * 
   * @param pParticleId
   *          particle id
   * @param pParticlePosition
   *          particle new position.
   */
  void setPosition(int pParticleId, float... pParticlePosition);

  /**
   * Sets the velocity of a particle
   * 
   * @param pParticleId
   *          particle id
   * @param pVelocity
   *          new velocity
   */
  void setVelocity(int pParticleId, float... pVelocity);

  /**
   * Sets the radius of a particle
   * 
   * @param pParticleId
   *          particle id
   * @param pRadius
   *          new radius
   */
  void setRadius(int pParticleId, float pRadius);

  /**
   * Updates builtin default neighborhood grid. Important: make sure that the
   * particles are entirely contained within [0,1]^d.
   */
  void updateNeighborhoodGrid();

  /**
   * Updates neighborhood grid with particles in this particle system.
   * Important: make sure that the particles are entirely contained within
   * [0,1]^d.
   * 
   * @param pNeighborhoodGrid
   *          neighborhood grid
   */
  public void updateNeighborhoodGrid(NeighborhoodGrid pNeighborhoodGrid);

  /**
   * Apply a centrifugal force around a point (X,Y)
   * 
   * @param pCenterX
   *          center x coordinate
   * @param pCenterY
   *          center y coordinate
   * @param pFactor
   *          force factor.
   */
  void repelAround(float pFactor, float pCenterX, float pCenterY);

  /**
   * Enforces bounds [0,1]^d by bouncing the particles elastically.
   *
   * @param pDampening
   *          how much should velocity be dampened.
   * 
   */
  void enforceBounds(float pDampening);

  /**
   * Enforces bounds [0,1]^d by bouncing the particles elastically.
   *
   * @param pDampening
   *          how much should velocity be dampened.
   * @param pNoise
   *          amount of noise to add to prevent perfect particle overlapp after
   *          enforcing bounds (typically at the corners).
   * 
   */
  void enforceBounds(float pDampening, float pNoise);

  /**
   * Adds Brownian motion.
   *
   * @param pIntensity
   *          intensity (force) of the brownian motion.
   */
  void addBrownianMotion(float pIntensity);

  /**
   * Applies a spatially invariant force to all particles.
   * 
   * @param pForce
   *          force.
   */
  void applyForce(float... pForce);

  /**
   * Applies a spatially invariant force to a range of particles.
   * 
   * @param pBeginId
   * @param pEndId
   * @param pForce
   *          force.
   */
  void applyForce(int pBeginId, int pEndId, float... pForce);

  /**
   * Applies a given force field to all particles.
   * 
   * @param pForceField
   *          force field
   */
  void applyForceField(ForceFieldInterface pForceField);

  /**
   * Applies a given force field to a range of particle ids.
   * 
   * @param pForceField
   *          force field
   * @param pBeginId
   *          begin id
   * @param pEndId
   *          end id
   * @param pForceFactor
   *          array of force factors to modulate application of the force field
   *          per particle.
   */
  void applyForceField(ForceFieldInterface pForceField,
                       int pBeginId,
                       int pEndId,
                       float[] pForceFactor);

  /**
   * Euler integration
   */
  void intergrateEuler();

  /**
   * Trapezoidal integration
   */
  void intergrateTrapezoidal();

  /**
   * Copies the positions to this array.
   * 
   * @param pPositionsCopy
   *          array to copy positions to
   * @return number of particles copied.
   */
  int copyPositions(float[] pPositionsCopy);

  /**
   * Copies the velocities to this array.
   * 
   * @param pVelocitiesCopy
   *          array to copy velocities to
   * @return number of particles copied.
   */
  int copyVelocities(float[] pVelocitiesCopy);

  /**
   * Copies the radii to this array.
   * 
   * @param pRadiiCopy
   * @return number of particles copied.
   */
  int copyRadii(float[] pRadiiCopy);

}

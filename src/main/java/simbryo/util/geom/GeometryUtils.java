package simbryo.util.geom;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

/**
 * Geometry utils
 *
 * @author royer
 */
public class GeometryUtils
{

  /**
   * Returns a newly allocated identity matrix.
   * 
   * @return newly allocated identity matrix
   */
  public static Matrix4f getIdentity()
  {
    Matrix4f lIdentity = new Matrix4f();
    lIdentity.setIdentity();
    return lIdentity;
  }

  /**
   * Adds translation to the given matrix
   * 
   * @param pMatrix
   *          matrix
   * @param pDeltaX
   *          translation along x
   * @param pDeltaY
   *          translation along y
   * @param pDeltaZ
   *          translation along z
   */
  public static void addTranslation(Matrix4f pMatrix,
                                    float pDeltaX,
                                    float pDeltaY,
                                    float pDeltaZ)
  {
    pMatrix.m03 += pDeltaX;
    pMatrix.m13 += pDeltaY;
    pMatrix.m23 += pDeltaZ;
  }

  /**
   * Returns a new matrix that is the product of the given matrices.
   * 
   * @param pMatrices
   *          matrices to multiply
   * @return newly allocated product matrix
   */
  public static Matrix4f multiply(Matrix4f... pMatrices)
  {
    Matrix4f lMatrix = new Matrix4f();
    lMatrix.setIdentity();

    for (int i = 0; i < pMatrices.length; i++)
    {
      Matrix4f lMatrixToMultiply = pMatrices[i];
      lMatrix.mul(lMatrixToMultiply);
    }

    return lMatrix;
  }

  /**
   * Multiplies a 4x4 matrix to a point represented in homogenous coordinates.
   * This function is meant to transform the 3D coordinates of the given point
   * with the affine transformation represented bythe matrix.
   * 
   * @param pMatrix
   *          4x4 matrix
   * @param pPoint
   *          point in 3D space representd in homogenous coordinates (x,y,z,1)
   * @return transformed point (newly allocated)
   */
  public static Vector4f pointMultiplication(Matrix4f pMatrix,
                                             Vector4f pPoint)
  {
    Matrix4f lMatrixForVector = new Matrix4f();
    lMatrixForVector.setColumn(0, pPoint);

    Matrix4f lProduct = new Matrix4f();

    lProduct.mul(pMatrix, lMatrixForVector);

    Vector4f lVectorResult = new Vector4f();
    lProduct.getColumn(0, lVectorResult);
    return lVectorResult;
  }

  /**
   * Multiplies the given matrix to a vector representing a _direction_ in 3D
   * space. Only the rotation/scaling/sheering components of the matrix
   * (excluding the translation) are used. This function is meant to transform a
   * direction in 3D space.
   * 
   * @param pMatrix
   *          matrix
   * @param pVector
   *          vector representing a direction in 3D space (x,y,z,1)
   * @return transformed direction vector
   */
  public static Vector4f directionMultiplication(Matrix4f pMatrix,
                                                 Vector4f pVector)
  {
    Matrix4f lMatrixWithoutTranslation = new Matrix4f(pMatrix);
    lMatrixWithoutTranslation.setTranslation(new Vector3f(0f,
                                                          0f,
                                                          0f));
    return pointMultiplication(lMatrixWithoutTranslation, pVector);
  }

  /**
   * Dot product between two 3D vectors represented with homogenous coordinates.
   * 
   * @param pVectorA
   *          vector A
   * @param pVectorB
   *          vector B
   * @return dot poduct (excludes w from calculation)
   */
  public static float homogenousDot(Vector4f pVectorA,
                                    Vector4f pVectorB)
  {
    return pVectorA.x * pVectorB.x + pVectorA.y * pVectorB.y
           + pVectorA.z * pVectorB.z;
  }

  /**
   * Normalizes a 3D vector represented in homogenous coordinates
   * 
   * @param pVector
   *          vector to ormalize, w is untouched.
   */
  public static void homogenousNormalize(Vector4f pVector)
  {
    float lNorm = (float) Math.sqrt(homogenousDot(pVector, pVector));
    pVector.x /= lNorm;
    pVector.y /= lNorm;
    pVector.z /= lNorm;
  }

  /**
   * Computes the cross product between two 3D vectors represented in homogenous
   * coordinates.
   * 
   * @param pVectorA
   *          vector A
   * @param pVectorB
   *          vector B
   * @return cross product
   */
  public static Vector4f homogenousCross(Vector4f pVectorA,
                                         Vector4f pVectorB)
  {
    Vector3f lVectorA = new Vector3f(pVectorA.x,
                                     pVectorA.y,
                                     pVectorA.z);
    Vector3f lVectorB = new Vector3f(pVectorB.x,
                                     pVectorB.y,
                                     pVectorB.z);
    Vector3f lCrossVector = new Vector3f();
    lCrossVector.cross(lVectorA, lVectorB);
    return new Vector4f(lCrossVector.x,
                        lCrossVector.y,
                        lCrossVector.z,
                        1.0f);
  }

  /**
   * Returns a 3D rotation matrix around the X axis of a given rotation angle.
   * 
   * @param pAngle
   *          angle in radians
   * @param pRotationCenter
   *          rotation center
   * @return rotation matrix
   */
  public static Matrix4f rotX(float pAngle, Vector3f pRotationCenter)
  {
    Matrix4f lRotationMatrix = new Matrix4f();
    lRotationMatrix.setIdentity();
    lRotationMatrix.rotX(pAngle);
    return rotAroundCenter(lRotationMatrix, pRotationCenter);
  }

  /**
   * Returns a 3D rotation matrix around the Y axis of a given rotation angle.
   * 
   * @param pAngle
   *          angle in radians
   * @param pRotationCenter
   *          rotation center
   * @return rotation matrix
   */
  public static Matrix4f rotY(float pAngle, Vector3f pRotationCenter)
  {
    Matrix4f lRotationMatrix = new Matrix4f();
    lRotationMatrix.setIdentity();
    lRotationMatrix.rotY(pAngle);
    return rotAroundCenter(lRotationMatrix, pRotationCenter);
  }

  /**
   * Returns a 3D rotation matrix around the Z axis of a given rotation angle.
   * 
   * @param pAngle
   *          angle in radians
   * @param pRotationCenter
   *          rotation center
   * @return rotation matrix
   */
  public static Matrix4f rotZ(float pAngle, Vector3f pRotationCenter)
  {
    Matrix4f lRotationMatrix = new Matrix4f();
    lRotationMatrix.setIdentity();
    lRotationMatrix.rotZ(pAngle);
    return rotAroundCenter(lRotationMatrix, pRotationCenter);
  }

  /**
   * Returns a rotation matrix around a new rotation center
   * 
   * @param pRotationMatrix
   *          original rotation matrix
   * @param pRotationCenter
   *          new rotation center
   * @return new rotation matrix
   */
  public static Matrix4f rotAroundCenter(Matrix4f pRotationMatrix,
                                         Vector3f pRotationCenter)
  {
    Matrix4f lTranslationMatrix = new Matrix4f();
    lTranslationMatrix.setIdentity();
    lTranslationMatrix.setTranslation(pRotationCenter);

    Matrix4f lTranslationMatrixInverse =
                                       new Matrix4f(lTranslationMatrix);
    lTranslationMatrixInverse.invert();

    Matrix4f lRotationMatrixAroundCenter =
                                         multiply(lTranslationMatrix,
                                                  pRotationMatrix,
                                                  lTranslationMatrixInverse);

    return lRotationMatrixAroundCenter;
  }

  /**
   * Computes distance between two vectors stored in one contiguous array.
   * 
   * @param pDimension
   *          vector dimension
   * @param pPositions
   *          array
   * @param pIdu
   *          first vector id
   * @param pIdv
   *          second vector id
   * @return distance
   */
  public static float computeDistance(int pDimension,
                                      float[] pPositions,
                                      int pIdu,
                                      int pIdv)
  {
    return (float) Math.sqrt(computeSquaredDistance(pDimension,
                                                    pPositions,
                                                    pIdu,
                                                    pIdv));
  }

  /**
   * Computes the squared distance between two vectors stored in one contiguous
   * array.
   * 
   * @param pDimension
   *          vector dimension
   * @param pPositions
   *          array
   * @param pIdu
   *          first vector id
   * @param pIdv
   *          second vector id
   * @return distance
   */
  public static float computeSquaredDistance(int pDimension,
                                             float[] pPositions,
                                             int pIdu,
                                             int pIdv)
  {

    final int u = pIdu * pDimension;
    final int v = pIdv * pDimension;

    float lDistance = 0;

    for (int d = 0; d < pDimension; d++)
    {
      float lAxisDistance = pPositions[u + d] - pPositions[v + d];
      lDistance += lAxisDistance * lAxisDistance;
    }

    return lDistance;
  }

  /**
   * Detects bounding box collisions.
   * 
   * @param pDimension
   *          vector dimensions
   * @param pPositions
   *          array of vectors
   * @param pR1
   *          first radius
   * @param pR2
   *          second radius
   * @param pIdu
   *          first vector id
   * @param pIdv
   *          second vector id
   * @return true if bounding boxes collide
   */
  public static boolean detectBoundingBoxCollision(int pDimension,
                                                   float[] pPositions,
                                                   float pR1,
                                                   float pR2,
                                                   int pIdu,
                                                   int pIdv)
  {

    final int u = pIdu * pDimension;
    final int v = pIdv * pDimension;

    for (int d = 0; d < pDimension; d++)
    {
      float lAxisDistance = Math.abs(pPositions[u + d]
                                     - pPositions[v + d]);
      float lAxisGap = lAxisDistance - pR1 - pR2;

      if (lAxisGap > 0)
        return false;
    }

    return true;
  }

}

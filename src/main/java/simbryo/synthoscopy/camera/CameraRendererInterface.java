package simbryo.synthoscopy.camera;

import simbryo.synthoscopy.SynthoscopyInterface;
import simbryo.synthoscopy.interfaces.ImageWidthHeightInterface;

/**
 * Camera model interface
 *
 * @param <I>
 *          type of images to store and manipulate camera images
 * @author royer
 */
public interface CameraRendererInterface<I> extends
                                        SynthoscopyInterface<I>,
                                        ImageWidthHeightInterface
{

}

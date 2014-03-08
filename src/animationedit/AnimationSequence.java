package animationedit;

import java.util.ArrayList;

import framemodel.AnimationFrame;
import graphicsutils.ImageStore;

/**
 * Represents the animation sequence in one directory.
 */
public class AnimationSequence {

	ImageStore imageStore;
	ArrayList<AnimationFrame> animationFrames;
	
	public AnimationSequence(String workingDirectory, String animationSequenceFile) {
		imageStore = new ImageStore(workingDirectory);
		animationFrames = AnimationFrameSequenceCreator.createAnimtionFrameSequenceFromXml(animationSequenceFile);
	}

	public ImageStore getImageStore() {
		return imageStore;
	}
	
	public ArrayList<AnimationFrame> getAnimationFrames() {
		return animationFrames;
	}
	
}

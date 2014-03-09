package animationedit;

import java.util.ArrayList;

import framemodel.AnimationFrame;
import graphicsutils.ImageStore;

/**
 * Represents the animation sequence in one directory.
 */
public class AnimationFrameSequence {

	ImageStore imageStore;
	ArrayList<AnimationFrame> animationFrames;
	ArrayList<AnimationFrameSequenceChangedListener> listeners = new ArrayList<AnimationFrameSequenceChangedListener>();
	
	public AnimationFrameSequence(String workingDirectory, String animationSequenceFile) {
		imageStore = new ImageStore(workingDirectory);
		animationFrames = AnimationFrameSequenceCreator.createAnimtionFrameSequenceFromXml(animationSequenceFile);
	}
	
	public void addChangeListener(AnimationFrameSequenceChangedListener listener) {
		listeners.add(listener);
	}

	public boolean writeToFile(String path) {
		return AnimationFrameSequenceCreator.writeAnimtionFrameSequenceToXml(path, animationFrames);
	}
	
	public ImageStore getImageStore() {
		return imageStore;
	}
	
	public ArrayList<AnimationFrame> getAnimationFrames() {
		return animationFrames;
	}
	
	private void notifyChangeListeners() {
		for (AnimationFrameSequenceChangedListener listener : listeners) {
			listener.onAnimationFrameSequenceChanged();
		}
	}
	
	public void addAnimationFrame(String image) {
		if (image == null || image.isEmpty()) return;
		animationFrames.add(new AnimationFrame(image));
		notifyChangeListeners();
	}
	
	public boolean moveAnimationFrameLater(AnimationFrame frame) {
		int index = animationFrames.indexOf(frame);
		if (index == -1) return false;
		if (animationFrames.size() <= 1) return false;
		if (index > animationFrames.size() - 2) return false;
		AnimationFrame temp = animationFrames.get(index);
		animationFrames.set(index, animationFrames.get(index+1));
		animationFrames.set(index+1, temp);
		notifyChangeListeners();
		return true;
	}
	
	public boolean moveAnimationFrameEarlier(AnimationFrame frame) {
		int index = animationFrames.indexOf(frame);
		if (index == -1) return false;
		if (animationFrames.size() <= 1) return false;
		if (index < 1) return false;
		AnimationFrame temp = animationFrames.get(index);
		animationFrames.set(index, animationFrames.get(index-1));
		animationFrames.set(index-1, temp);
		notifyChangeListeners();
		return true;
	}
 }

package animationeditgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import drawingtools.CurrentBrushSelector;
import drawingtools.CurrentColorSelector;
import drawingtools.DrawingTool;
import drawingtools.DrawingToolSelector;
import drawingtools.EraseDrawingTool;
import drawingtools.PenDrawingTool;
import drawingtools.PickupColorDrawingTool;

import animationeditgui.DirectoryChangeWatcher.DirectoryChangedListener;
import animationeditgui.FrameListButtonBar.FrameListButtonBarListener;
import animationframesequence.AnimationFrame;
import animationframesequence.AnimationFrameSequence;
import animationframesequence.AnimationFrameSequenceFile;
import animationframesequence.AnimationFrameSequenceInfoProvider;


import graphicsutils.ImageStore;
import graphicsutils.ImageStore.ImageStoreMaxSizeChangedListener;


/**
 * Main window.
 * 
 */
public class AnimationEditApplication extends JFrame 
	implements ImageStoreProvider,
				AnimationFrameSequenceInfoProvider,
				ImageStoreMaxSizeChangedListener,
				DrawingToolSelector,
				CurrentColorSelector, 
				CurrentBrushSelector,
				DirectoryChangedListener,
				FrameListButtonBarListener
	{

	private AnimationFrameSelector animationFrameSelector;
	private ApplicationMenu menu;
	private ApplicationConfig config;
	private AnimationFrameView animationFrameView;
	private AnimationPreview animationPreview;
	private AnimationFrameSequence animationSequence = null;
	private ColorSelector colorSelector;
	private DrawingToolSelectionMenu drawingToolSelectionMenu;
	private BrushPropertiesMenu selectBrushSizeField;
	private CurrentDocument currentDocument;
	private DirectoryChangeWatcher directoryChangeWatcher;
	
	/**
	 * Setup app.
	 * 
	 * @param configFilePath Config file path.
	 * @param animationSequenceFile File to open.
	 */
	public AnimationEditApplication(String configFilePath, String animationSequenceFile) {
		super("AnimationEdit");

		config = new ApplicationConfig(configFilePath);	

		directoryChangeWatcher = new DirectoryChangeWatcher(this, ".png");
		
		currentDocument = new CurrentDocument(config.projectPath);
		
		animationFrameView = new AnimationFrameView(this, this, this, 
				config.frameViewTransperentAlphaColor);
		animationPreview = new AnimationPreview(this, this, config.previewBackgroundColor);
		animationFrameSelector = new AnimationFrameSelector(animationFrameView);
		
		Container container = getContentPane();
		createGui(container);

		if (animationSequenceFile != null) {
			currentDocument.openDocument(animationSequenceFile);
			animationSequence = loadAnimationSequence(animationSequenceFile);
		}
		
		animationFrameView.revalidate();
		animationFrameView.repaint();
		
		animationPreview.revalidate();
		animationPreview.repaint();
	}

	
	/**
	 * Create the main window GUI.
	 * 
	 * @param container Container object.
	 */
	private void createGui(Container container) {
		AnimationEditHandler handler = new AnimationEditHandler();

		menu = new ApplicationMenu(handler);
		setJMenuBar(menu);

		JPanel panel = new JPanel(new BorderLayout());

		JPanel animationFrameListPanel = new JPanel(new BorderLayout());
		JToolBar animationFrameSelectorToolBar = new JToolBar();
		animationFrameListPanel.add(animationFrameSelector, BorderLayout.CENTER);
		animationFrameListPanel.add(new FrameListButtonBar(this), BorderLayout.NORTH);
		animationFrameSelectorToolBar.add(animationFrameListPanel);
		panel.add(animationFrameSelectorToolBar, BorderLayout.WEST);
		
		JPanel editFramePanel = new JPanel(new BorderLayout());
		
		animationFrameView.addMouseListener(animationFrameView);
		animationFrameView.addMouseMotionListener(animationFrameView);
		
		JScrollPane editFrameScrollPane = new JScrollPane(animationFrameView);
		editFramePanel.add(editFrameScrollPane, BorderLayout.CENTER);
		
		JToolBar drawingToolsToolBar = new JToolBar();
		
		JPanel colorSelectorPanel = new JPanel();
		colorSelector = new ColorSelector(60, 40);
		colorSelector.addMouseListener(colorSelector);
		colorSelectorPanel.add(colorSelector);
		drawingToolsToolBar.add(colorSelectorPanel);
		
		drawingToolSelectionMenu = new DrawingToolSelectionMenu(
				new PenDrawingTool(this, this), 
				new EraseDrawingTool(this), 
				new PickupColorDrawingTool(this));
		drawingToolsToolBar.add(drawingToolSelectionMenu);
		selectBrushSizeField = new BrushPropertiesMenu();
		drawingToolsToolBar.add(selectBrushSizeField);
		panel.add(drawingToolsToolBar, BorderLayout.NORTH);
		
		panel.add(editFramePanel, BorderLayout.CENTER);
		
		panel.add(animationPreview, BorderLayout.EAST);
		container.add(panel);
		
		// init main window
		setSize(Toolkit.getDefaultToolkit().getScreenSize().width - 40, 
				Toolkit.getDefaultToolkit().getScreenSize().height - 60);
		setLocation(20, 20);
		setVisible(true);
		setResizable(true);
		
		animationPreview.setPreferredSize(new Dimension(getWidth()/4, getHeight()));
	}


	public void showHelp() {
		HelpWindow.show();
	}
	
	public void editCurrentFrame() {
		ArrayList<PropertiesInputDialog.PropertyItem> propertyList = new ArrayList<PropertiesInputDialog.PropertyItem>();
		propertyList.add(new PropertiesInputDialog.PropertyItem("Tics", 
				Integer.toString(animationFrameSelector.getSelected().getTics())));
		propertyList.add(new PropertiesInputDialog.PropertyItem("Tag", animationFrameSelector.getSelected().getTag()));
		propertyList.add(new PropertiesInputDialog.PropertyItem("Next tag", animationFrameSelector.getSelected().getNext()));
		propertyList.add(new PropertiesInputDialog.PropertyItem("Offset x", 
				Integer.toString(animationFrameSelector.getSelected().getOffsetX())));
		propertyList.add(new PropertiesInputDialog.PropertyItem("Offset y", 
				Integer.toString(animationFrameSelector.getSelected().getOffsetY())));
		
		new PropertiesInputDialog(this, "Edit frame " + animationFrameSelector.getSelected().getImage(), propertyList);
		
		for (PropertiesInputDialog.PropertyItem property : propertyList) {
			int intValue = 0;
			try {
				intValue = Integer.parseInt(property.value);
			} catch (Exception e) {}
			
			if (property.name.equals("Tics")) {
				animationFrameSelector.getSelected().setTics(intValue);
			}
			if (property.name.equals("Tag")) {
				animationFrameSelector.getSelected().setTag(property.value);
			}
			if (property.name.equals("Next tag")) {
				animationFrameSelector.getSelected().setNext(property.value);
			}
			if (property.name.equals("Offset x")) {
				animationFrameSelector.getSelected().setOffsetX(intValue);
			}
			if (property.name.equals("Offset y")) {
				animationFrameSelector.getSelected().setOffsetY(intValue);
			}
		}
	}

	private void newFrameUseCurrentImage() {
		String name = animationFrameSelector.getSelected().getImage();
		if (animationSequence.getImageStore().getImage(name) == null) {
			JOptionPane.showMessageDialog(null, "No png file named " + name, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		animationSequence.addAnimationFrame(name);
		animationFrameSelector.setAnimationFrames(animationSequence.getAnimationFrames());
	}
	
	private void newFrameCopyImage() {
		String newImageName = JOptionPane.showInputDialog(
				"File name for copied image? \n(png image in the working directory, example \"myimagecopy\", \"myimagecopy.png\")",
				"");
		if (animationSequence.getImageStore().getImage(newImageName) != null) {
			JOptionPane.showMessageDialog(null, "png file named " + newImageName + " already exists!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (!animationSequence.getImageStore().copyImage(animationFrameSelector.getSelected().getImage(), newImageName)) {
			JOptionPane.showMessageDialog(null, "Error copying image.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		animationSequence.addAnimationFrame(newImageName);
		animationFrameSelector.setAnimationFrames(animationSequence.getAnimationFrames());
	}

	private void newFrameNewImage() {
		ArrayList<PropertiesInputDialog.PropertyItem> propertyList = new ArrayList<PropertiesInputDialog.PropertyItem>();
		propertyList.add(new PropertiesInputDialog.PropertyItem("Name (example \"mynewimage\", \"mynewimage.png\")", ""));
		propertyList.add(new PropertiesInputDialog.PropertyItem("Width (pixels)", "100"));
		propertyList.add(new PropertiesInputDialog.PropertyItem("Height (pixels)", "100"));
		new PropertiesInputDialog(this, "Create new image", propertyList);
		String newImageName = null;
		int width = 100;
		int height = 100;
		for (PropertiesInputDialog.PropertyItem property : propertyList) {
			int intValue = 0;
			try {
				intValue = Integer.parseInt(property.value);
			} catch (Exception e) {}
			
			if (property.name.equals("Name (example \"mynewimage\", \"mynewimage.png\")")) {
				newImageName = property.value;
			}
			if (property.name.equals("Width (pixels)")) {
				width = intValue;
			}
			if (property.name.equals("Height (pixels)")) {
				height = intValue;
			}
		}
		
		if (animationSequence.getImageStore().getImage(newImageName) != null) {
			JOptionPane.showMessageDialog(null, "png file named " + newImageName + " already exists!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		animationSequence.getImageStore().createNewImage(newImageName, width, height);
		animationSequence.addAnimationFrame(newImageName);
		animationFrameSelector.setAnimationFrames(animationSequence.getAnimationFrames());
	}

	private void newFrameFromWorkingDirectory() {
		String name = JOptionPane.showInputDialog(
				"Image for frame? \n(Should be a png image in the working directory, example \"myimage\", \"myimage.png\")",
				"");
		if (animationSequence.getImageStore().getImage(name) == null) {
			JOptionPane.showMessageDialog(null, "No png file named " + name, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		animationSequence.addAnimationFrame(name);
		animationFrameSelector.setAnimationFrames(animationSequence.getAnimationFrames());
	}
	
	/**
	 * Handle all actions in main window.
	 */
	private class AnimationEditHandler implements ActionListener {			
		@Override
		public void actionPerformed(ActionEvent event) {
			
			if (event.getSource() == menu.undoItem) {
				animationFrameView.undo();
			}
			
			if (event.getSource() == menu.zoomInItem) {
				animationFrameView.zoom(2.0f);
			}
			if (event.getSource() == menu.zoomOutItem) {
				animationFrameView.zoom(0.5f);
			}

			if (event.getSource() == menu.newFrameUseCurrentImageItem) {
				newFrameUseCurrentImage();
			}
			
			if (event.getSource() == menu.newFrameCopyImageItem) {
				newFrameCopyImage();
			}
			
			if (event.getSource() == menu.newFrameNewImageItem) {
				newFrameNewImage();
			}
			
			if (event.getSource() == menu.newFrameFromWorkingDirectoryItem) {
				newFrameFromWorkingDirectory();
			}
			
			if (event.getSource() == menu.deleteFrameItem) {
				animationSequence.deleteAnimationFrame(animationFrameSelector.getSelected());
			}
			if (event.getSource() == menu.moveFrameUpItem) {
				animationSequence.moveAnimationFrameEarlier(animationFrameSelector.getSelected());
				animationFrameSelector.setAnimationFrames(animationSequence.getAnimationFrames());
			}
			if (event.getSource() == menu.moveFrameDownItem) {
				animationSequence.moveAnimationFrameLater(animationFrameSelector.getSelected());
				animationFrameSelector.setAnimationFrames(animationSequence.getAnimationFrames());
			}
			
			if (event.getSource() == menu.preview1FpsItem) {
				animationPreview.setUpdateSpeed(100, 1000);
			}
			if (event.getSource() == menu.preview2FpsItem) {
				animationPreview.setUpdateSpeed(100, 1000/2);
			}
			if (event.getSource() == menu.preview10FpsItem) {
				animationPreview.setUpdateSpeed(100, 1000/10);
			}
			if (event.getSource() == menu.preview30FpsItem) {
				animationPreview.setUpdateSpeed(100, 1000/30);
			}
			if (event.getSource() == menu.preview60FpsItem) {
				animationPreview.setUpdateSpeed(100, 1000/60);
			}
			if (event.getSource() == menu.previewStopItem) {
				animationPreview.setUpdateSpeed(100000000, 100000000);
			}
			if (event.getSource() == menu.previewNextFrameItem) {
				animationPreview.setUpdateSpeed(100000000, 100000000);
				animationPreview.nextFrame();
			}
			if (event.getSource() == menu.previewPreviousFrameItem) {
				animationPreview.setUpdateSpeed(100000000, 100000000);
				animationPreview.previousFrame();
			}
			
			if (event.getSource() == menu.offsetXPlus1PixItem) {
				animationFrameSelector.getSelected().addToOffsetX(1);
				animationFrameSelector.onAnimationFrameSequenceChanged();
			}
			if (event.getSource() == menu.offsetXMinus1PixItem) {
				animationFrameSelector.getSelected().addToOffsetX(-1);
				animationFrameSelector.onAnimationFrameSequenceChanged();
			}
			if (event.getSource() == menu.offsetYPlus1PixItem) {
				animationFrameSelector.getSelected().addToOffsetY(1);
				animationFrameSelector.onAnimationFrameSequenceChanged();
			}
			if (event.getSource() == menu.offsetYMinus1PixItem) {
				animationFrameSelector.getSelected().addToOffsetY(-1);
				animationFrameSelector.onAnimationFrameSequenceChanged();
			}
			
			if (event.getSource() == menu.offsetXPlus10PixItem) {
				animationFrameSelector.getSelected().addToOffsetX(10);
				animationFrameSelector.onAnimationFrameSequenceChanged();
			}
			if (event.getSource() == menu.offsetXMinus10PixItem) {
				animationFrameSelector.getSelected().addToOffsetX(-10);
				animationFrameSelector.onAnimationFrameSequenceChanged();
			}
			if (event.getSource() == menu.offsetYPlus10PixItem) {
				animationFrameSelector.getSelected().addToOffsetY(10);
				animationFrameSelector.onAnimationFrameSequenceChanged();
			}
			if (event.getSource() == menu.offsetYMinus10PixItem) {
				animationFrameSelector.getSelected().addToOffsetY(-10);
				animationFrameSelector.onAnimationFrameSequenceChanged();
			}
			
			if (event.getSource() == menu.onionSkinOffItem) {
				animationFrameView.setOnionSkinDepth(0);
			}
			if (event.getSource() == menu.onionSkin1DepthItem) {
				animationFrameView.setOnionSkinDepth(1);
			}
			if (event.getSource() == menu.onionSkin2DepthItem) {
				animationFrameView.setOnionSkinDepth(2);
			}
			if (event.getSource() == menu.onionSkin3DepthItem) {
				animationFrameView.setOnionSkinDepth(3);
			}
			if (event.getSource() == menu.onionSkin4DepthItem) {
				animationFrameView.setOnionSkinDepth(4);
			}
			if (event.getSource() == menu.onionSkin5DepthItem) {
				animationFrameView.setOnionSkinDepth(5);
			}
			
			if (event.getSource() == menu.editFrameItem) {
				editCurrentFrame();
			}
			
			// MENU -> "Save" (AnimationEdit format)
			if (event.getSource() == menu.saveItem) {
				String path = currentDocument.saveDocument(true, true);
				animationSequence.writeToFile(path);
				if (path != null) {
					animationSequence.getImageStore().writeModifiedImagesToDisk();
					setTitle("AnimationEdit - " + currentDocument.getDocumentTitle()
							+ " | Last save: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
				}
			}

			// MENU -> "Save as" AnimationEdit format
			if (event.getSource() == menu.saveAsItem) {
				String path = currentDocument.saveDocument(false, true);
				animationSequence.writeToFile(path);
				if (path != null) {
					setTitle("AnimationEdit - " + currentDocument.getDocumentTitle()
							+ " | Last save: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
				}
			}

			// MENU -> New
			if (event.getSource() == menu.newItem) {
				String path = currentDocument.saveDocument(false, true);
				AnimationFrameSequenceFile.generateNewAnimationFrameSequenceXmlFile(path);
				animationSequence = loadAnimationSequence(path);
				setTitle("AnimationEdit - " + path);
			}

			if (event.getSource() == menu.reloadImagesItem) {
				animationSequence.getImageStore().reloadAll();
			}

			if (event.getSource() == menu.quitItem) {
				System.exit(0);
			}

			if (event.getSource() == menu.helpItem) {
				showHelp();
			}

			if (event.getSource() == menu.openItem) {
				String path = currentDocument.openDocument();
				animationSequence = loadAnimationSequence(path);
				setTitle("AnimationEdit - " + path);
			}

			animationFrameView.repaint();
		}
	}
	
	@Override
	public void onDirectoryChanged(ArrayList<String> changedFiles) {
		for (String fileName : changedFiles) {
			animationSequence.getImageStore().reloadImage(fileName);
		}
	}
	

	private AnimationFrameSequence loadAnimationSequence(String path) {
		if (path == null) return null;
		File file = new File(path);
		String dir = file.getParent();
		AnimationFrameSequence animationSequence = new AnimationFrameSequence(dir, path);
		animationSequence.addChangeListener(animationFrameSelector);
		animationFrameSelector.setAnimationFrames(animationSequence.getAnimationFrames());
		animationSequence.getImageStore().addMaxSizeChangedListener(this);
		if (currentDocument.hasOpenDocument()) {
			directoryChangeWatcher.setCurrentDirectory(currentDocument.getParentDirectoryOfOpenDocument());
		}
		return animationSequence;
	}
 	
	
	/**
	 * App main entry.
	 * 
	 * Args: 
	 *   arg0: path to a config file.
	 *   arg1: path to animation sequence to open.
	 * 
	 * @param args 
	 */
	public static void main(String[] args) {
		String configFile;
		String animationSequenceFile = null;
		
		if (args.length < 1) {
			System.out.println("No config specified, using default.");
			configFile = "config.xml";
		} else {
			configFile = args[0];
		}
		
		if (args.length >= 2) {
			animationSequenceFile = args[1];
		}
		
		try {
			AnimationEditApplication app = new AnimationEditApplication(configFile, animationSequenceFile);
			app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error while initializing app.");
			System.exit(1);
		}
	}


	@Override
	public ImageStore getImageStore() {
		if (animationSequence == null) return null;
		return animationSequence.getImageStore();
	}

	@Override
	public AnimationFrame getSelectedAnimationFrame() {
		return animationFrameSelector.getSelected();
	}


	@Override
	public int getNumAnimationFrames() {
		if (animationSequence.getAnimationFrames() == null) return 0;
		return animationSequence.getAnimationFrames().size();
	}


	@Override
	public AnimationFrame getAnimationFrame(int i) {
		return animationSequence.getAnimationFrame(i);
	}


	@Override
	public int getIndexOfAnimationFrameWithTag(String tag) {
		return animationSequence.getFrameIndexOfTag(tag);
	}


	@Override
	public int getSelectedAnimationFrameIndex() {
		return animationFrameSelector.getSelectedIndex();
	}


	@Override
	public void maxSizeChanged(int maxX, int maxY) {
		animationFrameView.maxSizeChanged(maxX, maxY);
	}


	@Override
	public DrawingTool getTool() {
		return drawingToolSelectionMenu.getTool();
	}


	@Override
	public Color getColor() {
		return colorSelector.getColor();
	}

	@Override
	public void setColor(Color color) {
		colorSelector.setColor(color);
	}
	
	@Override
	public boolean getBrushIsSmooth() {
		return selectBrushSizeField.isSmooth();
	}

	@Override
	public float getBrushWidth() {
		return selectBrushSizeField.getBrushSize();
	}

	@Override
	public void onMoveFrameUpButton() {
		animationSequence.moveAnimationFrameEarlier(animationFrameSelector.getSelected());
		animationFrameSelector.setAnimationFrames(animationSequence.getAnimationFrames());
	}

	@Override
	public void onMoveFrameDownButton() {
		animationSequence.moveAnimationFrameLater(animationFrameSelector.getSelected());
		animationFrameSelector.setAnimationFrames(animationSequence.getAnimationFrames());
	}

	@Override
	public void onEditFrameInfoButton() {
		editCurrentFrame();
	}

	@Override
	public void onDeleteFrameButton() {
		animationSequence.deleteAnimationFrame(animationFrameSelector.getSelected());
	}

	@Override
	public void onCopyFrameNewImageButton() {
		newFrameCopyImage();

	}

	@Override
	public void onCopyFrameSameImageButton() {
		newFrameUseCurrentImage();
	}
}
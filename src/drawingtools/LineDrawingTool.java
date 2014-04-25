package drawingtools;


import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Draws straight lines.
 */
public class LineDrawingTool implements DrawingTool {

	private int lastMouseDownX = -1;
	private int lastMouseDownY = -1;
	private CurrentColorSelector colorSelector;
	private CurrentBrushSelector brushSelector;
	
	public LineDrawingTool(CurrentColorSelector colorSelector, CurrentBrushSelector brushSelector) {
		this.colorSelector = colorSelector;
		this.brushSelector = brushSelector;
	}
	
	@Override
	public void onMouseDown(BufferedImage image, int x, int y) {
		if (lastMouseDownX != -1) {
			Graphics2D g = (Graphics2D)image.getGraphics();
			g.setColor(colorSelector.getColor());
			if (brushSelector.getBrushIsSmooth()) {
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			} else {
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			}
			g.setStroke(new BasicStroke(brushSelector.getBrushWidth()));
			g.drawLine(lastMouseDownX, lastMouseDownY, x, y);
		}
		lastMouseDownX = x;
		lastMouseDownY = y;
	}

	@Override
	public void onMouseRelease(BufferedImage image, int x, int y) {
	}

	@Override
	public void onMouseMoveWhileDown(BufferedImage image, int x, int y) {
	}
	
	@Override
	public void onSelectAnotherTool() {
		lastMouseDownX = -1;
		lastMouseDownY = -1;
	}

}

package util;

import java.awt.*;
import java.awt.font.*;
import java.text.*;
import java.util.Hashtable;

public class StringGraphics {
	
	public static void drawString (
			Graphics2D g,
			Rectangle rc,
			String s,
			Color textcolor,
			Font font,
			StringGraphicsAlignment hAlign,
			StringGraphicsAlignment vAlign,
			boolean multiLine)
	{
		if (s.trim().equals("")) {
			return;
		}
	
		if (rc.width <= 0 || rc.height <= 0) {
			return;
		}
	
		g.setColor(textcolor); //文字颜色
		g.setFont(font);
	
		LineBreakMeasurer lineMeasurer; //The LineBreakMeasurer used to line-break the paragraph.
		int paragraphStart; // The index in the LineBreakMeasurer of the first character	 in the paragraph.
		int paragraphEnd; // The index in the LineBreakMeasurer of the first character after the end of the paragraph.
	
		Dimension size = rc.getSize();
	
		//保存旧的剪裁区
		Rectangle oc = g.getClipBounds();
		//设置新得剪裁区
		g.setClip(rc.x, rc.y, rc.width, rc.height);
	
		Hashtable map = new Hashtable();
		map.put(TextAttribute.FONT, font);
		//map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_LIGHT);
		
		AttributedString ss = new AttributedString(s, map);
		AttributedCharacterIterator text = ss.getIterator();
		FontRenderContext frc = new FontRenderContext(g.getTransform(), true, false);
	
		paragraphStart = text.getBeginIndex();
		paragraphEnd = text.getEndIndex();
	
		// Create a new LineBreakMeasurer from the paragraph.
		lineMeasurer = new LineBreakMeasurer(text, frc);
		// Set formatting width to width of Component.
	
		float formatWidth = (float) size.width;
		float drawPosY = rc.y;
	
		// 多行时，计算文本总高度
		if (multiLine && vAlign != StringGraphicsAlignment.Top) {
			double gg = 0;
			lineMeasurer.setPosition(paragraphStart);
			while (lineMeasurer.getPosition() < paragraphEnd) {
				TextLayout layout = lineMeasurer.nextLayout(formatWidth);
				gg += layout.getDescent() + layout.getLeading() + layout.getAscent();
			}
		
			if (gg < rc.height) {
				if (vAlign == StringGraphicsAlignment.Center)
					drawPosY += (rc.height - gg) / 2;
				if (vAlign == StringGraphicsAlignment.Bottom)
					drawPosY += (rc.height - gg);
			}
		}
		// Get lines from lineMeasurer until the entire
		// paragraph has been displayed.
		lineMeasurer.setPosition(paragraphStart);
		while (lineMeasurer.getPosition() < paragraphEnd) {
			// Retrieve next layout.
			TextLayout layout = lineMeasurer.nextLayout(formatWidth);
			// Move y-coordinate by the ascent of the layout.
			switch (vAlign) {
			case Bottom: //底边
				if (!multiLine) {
					drawPosY += (rc.height - layout.getDescent() - layout.getLeading());
					break;
				}
			case Center: //居中
				if (!multiLine) {
					drawPosY += layout.getAscent() + (rc.height - layout.getAscent() - layout.getDescent() - layout.getLeading()) / 2;
					break;
				}
			default:
				drawPosY += layout.getAscent();
				break;
			}
		
			float drawPosX = rc.x;
			switch (hAlign) {
			case Right : //右对齐
				drawPosX = rc.x + formatWidth - layout.getAdvance();
				break;
			case Center : // 居中对齐
				drawPosX = rc.x + (formatWidth - layout.getAdvance()) / 2;
				break;
			default :
				drawPosX = rc.x;
				break;
			}
		
			layout.draw(g, drawPosX, drawPosY);
			// Move y-coordinate in preparation for next layout.
			drawPosY += layout.getDescent() + layout.getLeading();
			//如果多行文本超出了显示区域，那么退出
			if (drawPosY >= rc.y + rc.height)
				break;
		
			if (!multiLine) //如果不是多行，那么退出
				break;
		}
		//恢复旧的裁减区域
		g.setClip(oc.x, oc.y, oc.width, oc.height);

	}

}

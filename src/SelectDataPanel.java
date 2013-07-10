
import javax.swing.*;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

enum SelectDataType {
	SAVE, READ, LEVEL
}

public class SelectDataPanel extends ButtonItemPanel {
	
	static final int ID_BUTTON_BACK = 1000;
	static final int ID_BUTTON_SAVEDATA0 = 2000;
	
	SelectDataType type = SelectDataType.SAVE;
	
	Font titleFont;
	String titleString;
	ArrayList<Integer> availableSave;
	
	public SelectDataPanel() {
		super(TitlePanel.WIDTH, TitlePanel.HEIGHT);
	}
	
	public void initFont() {
		super.initFont();
		
		titleFont = CableBattle.cb.resource.baseFont.deriveFont(Font.BOLD, 42);
	}
	
	public ButtonStyle getButtonStyle() {
		return TitlePanel.bs;
	}
	
	public void drawBackground(Graphics g) {
		super.drawBackground(g);

		if (titleString != null) {
			
			FontMetrics fm = graphics.getFontMetrics();
			int textAscent = fm.getAscent(), textDescent = fm.getDescent();
			int textLeft = 36;
			int textTop = 36 + textAscent + textDescent;
			
			g.setFont(titleFont);
			g.setColor(Color.BLACK);
			g.drawString(titleString, textLeft, textTop);
			
			g.setFont(buttonFont);
		}
	}
	
	public void onButtonClick(int index) {
		int id = list.get(index).id;
		if (id == ID_BUTTON_BACK) {
			//return
			switch (type) {
			case READ:
			case LEVEL:
				CableBattle.mainWindow.showTitle();
				break;
			case SAVE:
				CableBattle.mainWindow.showStage(false);
				break;
			}
		} else {
			int iData = id - ID_BUTTON_SAVEDATA0;
			switch (type) {
			case READ:
				if (availableSave.contains((Integer)iData)) {
					if (CableBattle.stage.load(0, iData)) {
						CableBattle.mainWindow.showStage(true);
					} else {
						CableBattle.mainWindow.showLoadFailed();
					}
				}
				break;
			case LEVEL:
				CableBattle.mainWindow.showLevel(iData + 1);
				break;
			case SAVE:
				CableBattle.stage.save(iData);
				CableBattle.mainWindow.showStage(false);
				break;
			}
		}
	}
	
	public void onButtonSelect(int index) {
		CableBattle.getSound().playEffect(SoundEffect.MENU);
	}
	
	public void setType(SelectDataType type) {
		this.type = type;
		
		setTitle();
		setButton();
	}
	
	public void setButton() {
		clearButton();
		switch (type) {
		case READ:
		case SAVE:
			availableSave = new ArrayList<Integer>();
			SaveInfo si = new SaveInfo();
			for (int i = 0; i < SaveData.SAVE_DATA_COUNT; i++) {
				if (CableBattle.cb.sd.loadGame(i, si)) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					String date = sdf.format(new Date(si.time));
					
					addButton(String.format("Level %d (%s)", si.level, date), ID_BUTTON_SAVEDATA0 + i);
				
					availableSave.add(i);
				} else {
					addButton("-", ID_BUTTON_SAVEDATA0 + i);
				}
			}
			break;
		case LEVEL:
			for (int i = 0; i < CableBattle.cb.sd.unlockLevel && i < CableBattle.getLevel().count; i++) {
				addButton(String.format("Level %d", i + 1), ID_BUTTON_SAVEDATA0 + i);
			}
			break;
		}
		
		addButton("返回", ID_BUTTON_BACK);
		calcButton();
	}
	
	public void setTitle() {
		switch (type) {
		case READ:
			titleString = "读取";
			break;
		case SAVE:
			titleString = "保存";
			break;
		case LEVEL:
			titleString = "选择关卡";
			break;
		}
	}
}

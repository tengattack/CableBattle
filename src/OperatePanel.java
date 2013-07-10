
import javax.swing.*;
import java.awt.*;

public class OperatePanel extends ButtonItemPanel {
	
	static final int WIDTH = 192;
	static final int HEIGHT = PropsBarPanel.HEIGHT;
	
	static final ButtonStyle bs = new ButtonStyle(PropsItemPanel.ITEM_WIDTH, PropsItemPanel.ITEM_HEIGHT, PropsItemPanel.SPACE, Font.PLAIN, 18);

	static final int ID_BUTTON_UNDO = 1000, ID_BUTTON_SAVE = 1001, ID_BUTTON_TITLE = 1002;
	
	public OperatePanel() {
		super(WIDTH, HEIGHT);
		setOperateButton();
	}

	public void setOperateButton() {
		clearButton();

		addButton("撤销", ID_BUTTON_UNDO);
		addButton("保存", ID_BUTTON_SAVE);
		addButton("标题", ID_BUTTON_TITLE);
		
		calcButton();
	}
	
	public ButtonStyle getButtonStyle() {
		return bs;
	}
	
	public void calcButton() {
		ButtonStyle bs = getButtonStyle();
		
		int left = (int)((this.width - (bs.width + PropsItemPanel.SPACE_WIDTH) * list.size() + PropsItemPanel.SPACE_WIDTH) / 2);
		int top = PropsItemPanel.SPACE;
		
		for (int i = 0; i < list.size(); i++) {
			list.get(i).setPosition(left, top, bs.width, bs.height);
			left += bs.width + PropsItemPanel.SPACE_WIDTH;
		}
	}
	
	public void onButtonClick(int index) {
		int id = list.get(index).id;
		switch (id) {
		case ID_BUTTON_UNDO:
			if (CableBattle.stage.undo()) {
				CableBattle.getSound().playEffect(SoundEffect.USE);
			} else {
				CableBattle.getSound().playEffect(SoundEffect.DISABLED);
			}
			break;
		case ID_BUTTON_SAVE:
			CableBattle.mainWindow.showSelect(SelectDataType.SAVE);
			break;
		case ID_BUTTON_TITLE:
			CableBattle.mainWindow.showTitle();
			break;
		}
	}
	
	public void onButtonSelect(int index) {
		CableBattle.getSound().playEffect(SoundEffect.MENU);
	}
}

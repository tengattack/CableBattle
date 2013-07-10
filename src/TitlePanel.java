
import javax.swing.*;
import java.awt.*;

public class TitlePanel extends ButtonItemPanel {

	static final int WIDTH = MainWindow.WIDTH;
	static final int HEIGHT = MainWindow.HEIGHT + 10;
	
	static final int BUTTON_WIDTH = WIDTH / 3, BUTTON_HEIGHT = 48;
	static final int BUTTON_SPACE = 5;
	
	static final int ID_BUTTON_START = 1000, ID_BUTTON_CONTINUE = 1001, ID_BUTTON_LOAD = 1002, ID_BUTTON_EXIT = 1003;
	static final int ID_BUTTON_LEVEL0 = 2000;
	
	static final ButtonStyle bs = new ButtonStyle(BUTTON_WIDTH, BUTTON_HEIGHT, BUTTON_SPACE, Font.BOLD, 26);

	int t_unlockLevel;
	boolean t_saved;
	
	public TitlePanel() {
		super(WIDTH, HEIGHT);
		
		t_unlockLevel = CableBattle.cb.sd.unlockLevel;
		t_saved = CableBattle.cb.sd.saved;
		
		setTitleButton();
	}
	
	public void drawBackground(Graphics g) {
		g.drawImage(CableBattle.cb.resource.titleImage, 0, 0, null);
	}

	public void setTitleButton() {
		clearButton();
		
		addButton("开始", ID_BUTTON_START);
		
		if (t_unlockLevel > 1) {
			addButton("继续", ID_BUTTON_CONTINUE);
		}
		if (t_saved) {
			addButton("读取", ID_BUTTON_LOAD);
		}
		addButton("退出", ID_BUTTON_EXIT);
		
		calcButton();
	}
	
	public ButtonStyle getButtonStyle() {
		return this.bs;
	}

	public void onButtonClick(int index) {
		int id = list.get(index).id;
		switch (id) {
		case ID_BUTTON_START:
			CableBattle.mainWindow.showLevel(1);
			break;
		case ID_BUTTON_CONTINUE:
			CableBattle.mainWindow.showSelect(SelectDataType.LEVEL);
			break;
		case ID_BUTTON_LOAD:
			CableBattle.mainWindow.showSelect(SelectDataType.READ);
			break;
		case ID_BUTTON_EXIT:
			CableBattle.mainWindow.dispose();
			break;
		}
	}
	
	public void onButtonSelect(int index) {
		CableBattle.getSound().playEffect(SoundEffect.MENU);
	}
	
	public void checkButton() {
		boolean needReset = false;
		if (t_unlockLevel <= 1 && CableBattle.cb.sd.unlockLevel > 1) {
			needReset = true;
		}
		if (!t_saved && CableBattle.cb.sd.saved) {
			needReset = true;
		}

		t_unlockLevel = CableBattle.cb.sd.unlockLevel;
		t_saved = CableBattle.cb.sd.saved;
		
		if (needReset) {
			setTitleButton();
		}
	}
}

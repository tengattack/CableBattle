
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.util.TimerTask;
import java.util.Timer;

public class MainWindow extends JFrame {

	protected JPanel menuPanel = new JPanel();
	
	protected LevelTitlePanel levelTitlePanel = new LevelTitlePanel();
	protected StagePanel stagePanel = new StagePanel();
	protected PropsBarPanel propsBarPanel = new PropsBarPanel();
	protected TitlePanel titlePanel = new TitlePanel();
	protected SelectDataPanel saveDataPanel = new SelectDataPanel();
	protected LevelMessagePanel levelMessagePanel = new LevelMessagePanel();
	protected JPanel gamePanel = new JPanel();
	
	static final int WIDTH = StagePanel.WIDTH;
	static final int HEIGHT = LevelTitlePanel.HEIGHT + StagePanel.HEIGHT + PropsBarPanel.HEIGHT;
	
	Color backgroundColor;
	boolean smallScreen = false;
	
	public void showTitle() {
		CableBattle.getSound().playBgm("title");
		
		saveDataPanel.setStopDraw(true);
		titlePanel.setStopDraw(false);

		gamePanel.setVisible(false);
		saveDataPanel.setVisible(false);
		levelMessagePanel.setVisible(false);
		
		titlePanel.checkButton();
		titlePanel.setVisible(true);
	}
	
	public void showStage(boolean showmessage) {
		CableBattle.getSound().stopBgm("title");
		
		titlePanel.setStopDraw(true);
		saveDataPanel.setStopDraw(true);
		
		titlePanel.setVisible(false);
		saveDataPanel.setVisible(false);
		
		if (showmessage) {
			gamePanel.setVisible(false);
			
			levelMessagePanel.setVisible(true);
			
			//java.util.Timer
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
			  @Override
			  public void run() {
				  CableBattle.mainWindow.showStage(false);
			  }
			}, 2000);
		} else {
			levelMessagePanel.setVisible(false);

			gamePanel.setVisible(true);
		}
	}
	
	public void showLoadFailed() {
		CableBattle.getSound().playEffect(SoundEffect.DISABLED);
		
		showMessage();
		
		//java.util.Timer
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
		  @Override
		  public void run() {
			  CableBattle.mainWindow.showTitle();
		  }
		}, 5000);
	}
	
	public void showSelect(SelectDataType type) {
		titlePanel.setStopDraw(true);
		
		saveDataPanel.setType(type);
		saveDataPanel.setStopDraw(false);
		
		titlePanel.setVisible(false);
		gamePanel.setVisible(false);
		levelMessagePanel.setVisible(false);

		saveDataPanel.setVisible(true);
	}
	
	public void showLevel(int level) {
		if (CableBattle.getStage().setLevel(level)) {
			showStage(true);
		} else {
			showLoadFailed();
		}
	}
	
	public void showNextLevel() {
		int level = CableBattle.stage.getLevel();
		if (level >= CableBattle.cb.resource.level.count) {
			showTitle();
		} else {
			showLevel(level + 1);
		}
	}
	
	public void showWin(int level) {
		CableBattle.getSound().playEffect(SoundEffect.WIN);
		
		showMessage();

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
		  @Override
		  public void run() {
			  CableBattle.mainWindow.showNextLevel();
		  }
		}, 3000);
	}
	
	private void showMessage() {
		titlePanel.setStopDraw(true);
		saveDataPanel.setStopDraw(true);
		
		gamePanel.setVisible(false);
		titlePanel.setVisible(false);
		saveDataPanel.setVisible(false);

		levelMessagePanel.setVisible(true);
	}
	
	public void onCreate() {
		
		//setSize(WIDTH, HEIGHT);
		int initHeight = HEIGHT;
		int screenHeight = ((int)java.awt.Toolkit.getDefaultToolkit().getScreenSize().height);
		if (screenHeight <= 800) {
			initHeight -= LevelTitlePanel.HEIGHT;
			smallScreen = true;
		}
		
		Container container = getContentPane();
		backgroundColor = container.getBackground();

		//System.out.printf("screenHeight:%d %dx%d\n", screenHeight, WIDTH, HEIGHT);
		setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setPreferredSize(new Dimension(WIDTH - 10, initHeight));
		pack();
		
		gamePanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.Y_AXIS));
		if (smallScreen) {
			levelTitlePanel.setVisible(false);
		} else {
			gamePanel.add(levelTitlePanel);
		}
		gamePanel.add(stagePanel);
		gamePanel.add(propsBarPanel);
		
		showTitle();
		
		container.add(levelMessagePanel);
		container.add(gamePanel);
		container.add(titlePanel);
		container.add(saveDataPanel);
	}
	
	public void setWindowIcon() {
		if (CableBattle.cb.resource.favicon != null) {
			this.setIconImage(CableBattle.cb.resource.favicon);
		}
	}
	
	public MainWindow() {
		super("CableBattle");
		
		setWindowIcon();
		onCreate();

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
		   public void windowClosing(WindowEvent e) {
			   //JOptionPane.showMessageDialog(null, "close really?");
		   }
		   public void windowClosed(WindowEvent e) {
			   //super.windowClosed(e);
			   CableBattle.cb.closed();
		   }
		});
		
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}
}

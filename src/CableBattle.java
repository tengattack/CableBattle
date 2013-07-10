
public class CableBattle {

	/**
	 * @param args
	 */
	static Stage stage = new Stage();
	static PropsBar propsBar = new PropsBar();
	static MainWindow mainWindow;
	
	static CableBattle cb = new CableBattle();

	String path;
	Resource resource;
	SaveData sd;
	
	public static void main(String[] args) {
		mainWindow = new MainWindow();
	}
	
	public static Stage getStage() {
		return stage;
	}
	
	public static Sound getSound() {
		return cb.resource.sound;
	}
	
	public static Level getLevel() {
		return cb.resource.level;
	}

	public CableBattle() {
		path = System.getProperty("user.dir");
		
		char lastCh = path.charAt(path.length() - 1);
		if (lastCh != '\\' && lastCh != '/') {
			path += '/';
		}
		
		resource = new Resource(path + "resource/");
		sd = new SaveData(path + "save/");
	}
	
	public void closed() {
		mainWindow.stagePanel.stopAnimation();
		getSound().stop();
		sd.save();
		
		System.gc();
		System.exit(0);
	}
}

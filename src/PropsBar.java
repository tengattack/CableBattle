
import java.util.ArrayList;

class PropsItem {
	
	PropsType type;
	int amount;
	
	public PropsItem(PropsType type, int amount) {
		this.type = type;
		this.amount = amount;
	}
}

public class PropsBar {

	int selectIndex = -1;
	ArrayList<PropsItem> list = new ArrayList<PropsItem>();
	
	static PropsItem itemNone = new PropsItem(PropsType.NONE, -1);
	
	public PropsBar() {
	}
	
	public ArrayList<PropsItem> getList() {
		return list;
	}
	
	public void setList(ArrayList<PropsItem> list) {
		clear();
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				add(list.get(i).type, list.get(i).amount);
			}
		}
	}
	
	/*public void updateLeft() {
		CableBattle.mainWindow.propsBarPanel.controls.updateLeft();
	}*/
	
	public void add(PropsType type, int amount) {
		list.add(new PropsItem(type, amount));
	}

	public void clear() {
		list.clear();
		selectIndex = -1;
	}

	public boolean use(PropsType type) {
		int index = getItemIndex(type);
		if (index != -1) {
			PropsItem item = getItem(index);
			if (item.amount == 0) {
				return false;
			} else if (item.amount > 0) {
				item.amount--;
				// need update
				CableBattle.mainWindow.propsBarPanel.controls.updateItem(index);
			}
		}
		return true;
	}
	
	public boolean recyle(PropsType type) {
		int index = getItemIndex(type);
		if (index != -1) {
			PropsItem item = getItem(index);
			if (item.amount >= 0) {
				item.amount++;
				// need update
				CableBattle.mainWindow.propsBarPanel.controls.updateItem(index);
			}
		}
		return true;
	}
	
	public boolean isSelect(int index) {
		return selectIndex == index;
	}
	
	public boolean isSelect(PropsType type) {
		return getSelectType() == type;
	}
	
	public PropsItem getItem(int index) {
		if (index == -1) {
			return itemNone;
		} else {
			return list.get(index);
		}
	}
	
	public PropsItem getItem(PropsType type) {
		int index = getItemIndex(type);
		return getItem(index);
	}
	
	public int getItemIndex(PropsType type) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).type == type) {
				return i;
			}
		}
		return -1;
	}
	
	public PropsItem getSelect() {
		return getItem(selectIndex);
	}
	
	public PropsType getSelectType() {
		return getSelect().type;
	}
	
	public boolean select(int index) {
		selectIndex = index;
		return true;
	}
}

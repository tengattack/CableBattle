
public class MapBlock {
	// implements Cloneable
	
	Block block;
	MapProps mapprops;
	
	public MapBlock(BlockType blocktype, PropsType propstype) {
		block = new Block(blocktype);
		mapprops = new MapProps(propstype);
	}
	
	public MapBlock(BlockType blocktype, MapProps mapprops) {
		this.block = new Block(blocktype);
		this.mapprops = mapprops;
	}
	
	public Props getProps() {
		return mapprops.props;
	}
	
	public void setSystemProps(boolean b) {
		if (mapprops != null && mapprops.props.type != PropsType.NONE) {
			mapprops.props.allow(!b);
		}
	}
	
	public boolean animation() {
		if (mapprops.props.type == PropsType.PIG) {
			int c = (mapprops.range.w + mapprops.range.h - 2) * 2;
			if (c > 0) {
				mapprops.state++;
				if (mapprops.state % (c * PigClip.WALK_STEP) == 0) {
					mapprops.state = 0;
				}
			}
			return true;
		}
		return false;
	}
	
	protected MapBlock clone() {
		return new MapBlock(block.type, mapprops.clone());
    }
}

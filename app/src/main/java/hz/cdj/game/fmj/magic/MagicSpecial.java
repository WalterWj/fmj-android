package hz.cdj.game.fmj.magic;

import hz.cdj.game.fmj.characters.FightingCharacter;

/**
 * 05特殊型 妙手空空
 * 数据布局与 MagicAttack 相同：
 *   offset+0x12: HP 值（正=从敌人身上偷取生命的基数，负=从自己转移到敌人）
 *   offset+0x14: MP 值（正=从敌人身上偷取真气的基数，负=从自己转移到敌人）
 * @author Chen
 *
 */
public class MagicSpecial extends BaseMagic {

	private int mHp; // 偷取 HP 基数
	private int mMp; // 偷取 MP 基数

	@Override
	protected void setOtherData(byte[] buf, int offset) {
		mHp = get2BytesSInt(buf, offset + 0x12);
		mMp = get2BytesSInt(buf, offset + 0x14);
	}

	/**
	 * 妙手空空：从敌人身上偷取 HP/MP
	 */
	@Override
	public void use(FightingCharacter src, FightingCharacter dst) {
		// 消耗真气
		src.setMP(src.getMP() - getCostMp());

		// 偷取 HP：敌人损失 HP，自己回复 HP
		if (mHp > 0) {
			dst.setHP(Math.max(0, dst.getHP() - mHp));
			src.setHP(Math.min(src.getMaxHP(), src.getHP() + mHp));
		}

		// 偷取 MP：敌人损失 MP，自己回复 MP
		if (mMp > 0) {
			dst.setMP(Math.max(0, dst.getMP() - mMp));
			src.setMP(Math.min(src.getMaxMP(), src.getMP() + mMp));
		}
	}

}

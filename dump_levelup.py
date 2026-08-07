"""Dump the level-up chain magic learning data for character index 1 (柳清风)."""
import struct
import sys

DAT_PATH = "/Volumes/Mestor/SoftWare/WorkBuddy/fmj-android/app/src/main/assets/DAT.LIB"

with open(DAT_PATH, "rb") as f:
    data = f.read()

RES_MLR = 12
TYPE_LEVELUP = 2

def get_key(res_type, tp, idx):
    return (res_type << 16) | (tp << 8) | idx

target_key = get_key(RES_MLR, TYPE_LEVELUP, 1)
print(f"Searching for key 0x{target_key:06X} (RES_MLR type=2 index=1)")

# Parse index table at offset 0x10
found_offset = None
i = 0x10
j = 0x2000
while i < len(data) and data[i] != 0xFF:
    rt = data[i]
    tp = data[i + 1]
    idx = data[i + 2] & 0xFF
    key = (rt << 16) | (tp << 8) | idx

    block = data[j] & 0xFF
    low = data[j + 1] & 0xFF
    high = data[j + 2] & 0xFF
    value = block * 0x4000 | (high << 8 | low)

    if key == target_key:
        found_offset = value
        print(f"  Found at offset 0x{value:06X}")

    i += 3
    j += 3

if found_offset is None:
    print("ERROR: Level-up chain for index 1 not found in DAT.LIB!")
    sys.exit(1)

offset = found_offset

# Parse ResLevelupChain data
mType = data[offset] & 0xFF
mIndex = data[offset + 1] & 0xFF
mMaxLevel = data[offset + 2] & 0xFF

print(f"\nLevel-up chain: type={mType}, index={mIndex}, maxLevel={mMaxLevel}")
print(f"\nLevel | LearnMagicNum (next level magic learning trigger)")
print("-" * 50)

# mLevelData starts at offset + 4, each level is 20 bytes
level_data_start = offset + 4

for level in range(1, mMaxLevel + 1):
    base = level_data_start + (level - 1) * 20
    learn_magic = data[base + 19] & 0xFF
    next_exp = (data[base + 14] & 0xFF) | ((data[base + 15] & 0xFF) << 8)
    marker = " ← 新学魔法!" if learn_magic > 0 else ""
    if learn_magic > 0:
        print(f"  {level:3d} | {learn_magic:5d} {marker}")
    elif level == mMaxLevel:
        # Print the last few levels that are 0 too, for context
        pass

# Also print summary: levels where magic count changes
print(f"\n--- Magic learning milestones ---")
prev = -1
milestones = []
for level in range(1, mMaxLevel + 1):
    base = level_data_start + (level - 1) * 20
    learn_magic = data[base + 19] & 0xFF
    if learn_magic != prev:
        milestones.append((level, learn_magic))
        prev = learn_magic

print(f"  Level -> Magic count:")
for lv, cnt in milestones:
    print(f"    Lv.{lv:3d} → {cnt} 个魔法")

if milestones:
    highest = milestones[-1]
    if highest[1] == 0:
        # Find last non-zero level
        non_zero = [(lv, cnt) for lv, cnt in milestones if cnt > 0]
        if non_zero:
            last_lv, last_cnt = non_zero[-1]
            print(f"\n  结论：主角在 Lv.{last_lv} 学会最多 {last_cnt} 个魔法后，后续等级不再自动学习。")
            print(f"  当前等级 44（或 52）若魔法为 0，是存档污染导致，非等级不足。")
            print(f"  刷等级不会解决问题 — 必须重开新档或修复存档。")
        else:
            print(f"\n  结论：主角等级链中从未自动学习任何魔法！")
            print(f"  主角的魔法可能全由脚本/事件触发学习。")
    else:
        print(f"\n  最高学到 Lv.{highest[0]}，共 {highest[1]} 个魔法。")
